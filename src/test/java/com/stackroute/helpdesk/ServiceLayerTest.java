package com.stackroute.helpdesk;

import com.stackroute.helpdesk.dto.SecurityQuestionResetDTO;
import com.stackroute.helpdesk.dto.TicketSearchCriteria;
import com.stackroute.helpdesk.dto.TicketStatisticsDTO;
import com.stackroute.helpdesk.dto.UserRegistrationDTO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.model.TicketFeedback;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.*;
import com.stackroute.helpdesk.service.impl.*;
import com.stackroute.helpdesk.util.DBUtil;
import com.stackroute.helpdesk.util.EmailService;
import com.stackroute.helpdesk.util.ServiceResult;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceLayerTest {

    private static UserService userService;
    private static TicketService ticketService;
    private static CommentService commentService;
    private static FeedbackService feedbackService;
    private static PasswordResetService passwordResetService;
    private static int testTicketId;

    @BeforeAll
    public static void setup() {
        DBUtil.initializeDatabase();
        userService = new UserServiceImpl();
        ticketService = new TicketServiceImpl();
        commentService = new CommentServiceImpl();
        feedbackService = new FeedbackServiceImpl();
        passwordResetService = new PasswordResetServiceImpl();
    }

    @Test
    @Order(1)
    public void testServiceResultPatternMatching() {
        ServiceResult<String> success = ServiceResult.ok("Payload Data", "Action succeeded");
        assertTrue(success.isSuccess());
        assertFalse(success.isFailure());
        assertEquals("Payload Data", success.getData().orElseThrow());

        // Java 17 Pattern Matching for Sealed Interface
        String outcome;
        if (success instanceof ServiceResult.Success<String> s) {
            outcome = "SUCCESS: " + s.data();
        } else if (success instanceof ServiceResult.Failure<String> f) {
            outcome = "FAILURE: " + f.errorMessage();
        } else {
            outcome = "UNKNOWN";
        }
        assertEquals("SUCCESS: Payload Data", outcome);

        ServiceResult<String> failure = ServiceResult.fail("NOT_FOUND", "Resource missing");
        assertTrue(failure.isFailure());
        assertEquals("Resource missing", failure.getMessage());
    }

    @Test
    @Order(2)
    public void testUserServiceAuthenticationAndValidation() {
        // Authenticate seeded user
        ServiceResult<User> authResult = userService.authenticate("john@example.com", "user123");
        assertTrue(authResult.isSuccess(), "John should authenticate");
        User user = authResult.getData().orElseThrow();
        assertEquals("John Doe", user.getName());

        // Authenticate with wrong password
        ServiceResult<User> badAuth = userService.authenticate("john@example.com", "wrongpass");
        assertTrue(badAuth.isFailure());
        assertEquals("INVALID_CREDENTIALS", ((ServiceResult.Failure<User>) badAuth).errorCode());

        // Test Registration with DTO & Captcha
        long ts = System.currentTimeMillis();
        String newEmail = "svc_user_" + ts + "@company.com";
        UserRegistrationDTO regDTO = new UserRegistrationDTO(
            "Service Test User",
            newEmail,
            "pass1234",
            "USER",
            "ABCDE",
            "ABCDE" // matching captcha
        );

        ServiceResult<User> regResult = userService.register(regDTO);
        assertTrue(regResult.isSuccess(), "Registration with valid captcha should succeed");
        assertTrue(regResult.getData().orElseThrow().getUserId() > 0);

        // Test Registration with 4-char password
        UserRegistrationDTO shortPassDTO = new UserRegistrationDTO(
            "Short Pass User",
            "shortpass_" + ts + "@company.com",
            "pass",
            "USER",
            "ABCDE",
            "ABCDE"
        );
        ServiceResult<User> shortPassResult = userService.register(shortPassDTO);
        assertTrue(shortPassResult.isSuccess(), "Registration with 4-character password should succeed");

        // Test Captcha mismatch failure
        UserRegistrationDTO badCaptchaDTO = new UserRegistrationDTO(
            "Fake User",
            "fake_" + ts + "@company.com",
            "pass1234",
            "USER",
            "WRONG",
            "CORRECT"
        );
        ServiceResult<User> captchaFail = userService.register(badCaptchaDTO);
        assertTrue(captchaFail.isFailure());
        assertEquals("CAPTCHA_MISMATCH", ((ServiceResult.Failure<User>) captchaFail).errorCode());
    }

    @Test
    @Order(3)
    public void testTicketServiceCreationAndAttachment() {
        Ticket ticket = Ticket.builder()
            .userId(1) // John Doe
            .title("Printer Queue Stalled on 4th Floor")
            .description("Multiple print jobs queued with status spooling error.")
            .priority("HIGH")
            .attachmentName("printer_dump.txt")
            .attachmentType("text/plain")
            .build();

        byte[] attachmentData = "Spooler diagnostic logs: error 0x00000057".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(attachmentData);

        ServiceResult<Ticket> createResult = ticketService.createTicket(ticket, bais, attachmentData.length);
        assertTrue(createResult.isSuccess(), "Ticket creation should succeed");

        testTicketId = createResult.getData().orElseThrow().getTicketId();
        assertTrue(testTicketId > 0);

        // Fetch ticket
        Optional<Ticket> fetched = ticketService.getTicketById(testTicketId);
        assertTrue(fetched.isPresent());
        assertEquals("Printer Queue Stalled on 4th Floor", fetched.get().getTitle());
        assertTrue(fetched.get().isHasAttachment());
    }

    @Test
    @Order(4)
    public void testTicketServiceStreamsAndStatistics() {
        // Real-time aggregate statistics computed via Streams
        TicketStatisticsDTO stats = ticketService.getTicketStatistics();
        assertNotNull(stats);
        assertTrue(stats.totalCount() > 0);
        assertTrue(stats.openCount() >= 1);

        // Unassigned queue sorted by urgency (earliest deadline first)
        List<Ticket> unassigned = ticketService.getUnassignedTickets();
        assertNotNull(unassigned);
        assertFalse(unassigned.isEmpty());
        // Verify stream ordering
        for (int i = 0; i < unassigned.size() - 1; i++) {
            assertTrue(unassigned.get(i).getSlaDeadlineMillis() <= unassigned.get(i + 1).getSlaDeadlineMillis());
        }

        // Search Criteria record
        TicketSearchCriteria criteria = TicketSearchCriteria.of("Printer", "ALL", "ALL", null, true);
        List<Ticket> searchResults = ticketService.getTicketsByCriteria(criteria);
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(t -> t.getTicketId() == testTicketId));

        // CSV generation using stream pipelines
        String csv = ticketService.generateCsvReport(criteria);
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFFTicket ID,Title,Priority"));
        assertTrue(csv.contains("Printer Queue Stalled"));
    }

    @Test
    @Order(5)
    public void testCommentServiceAndStatusTransition() {
        TicketComment comment = TicketComment.builder()
            .ticketId(testTicketId)
            .authorId(3) // Bhavani
            .commentText("Cleared stuck spooler service and rebooted print server.")
            .build();

        // Add comment and dynamically update status to RESOLVED
        ServiceResult<TicketComment> commentResult = commentService.addComment(comment, "RESOLVED");
        assertTrue(commentResult.isSuccess(), "Comment should be posted");

        List<TicketComment> comments = commentService.getCommentsForTicket(testTicketId);
        assertFalse(comments.isEmpty());
        assertEquals("Cleared stuck spooler service and rebooted print server.", comments.get(0).getCommentText());

        Optional<Ticket> updatedTicket = ticketService.getTicketById(testTicketId);
        assertTrue(updatedTicket.isPresent());
        assertEquals("RESOLVED", updatedTicket.get().getStatus());
    }

    @Test
    @Order(6)
    public void testFeedbackServiceCSAT() {
        // Submit 5-star feedback
        ServiceResult<TicketFeedback> feedbackResult = feedbackService.submitFeedback(
            testTicketId, 5, "Fast resolution! The printer works perfectly now.", 1 // author John Doe
        );
        assertTrue(feedbackResult.isSuccess(), "CSAT feedback submission should succeed");

        Optional<TicketFeedback> fetched = feedbackService.getFeedbackForTicket(testTicketId);
        assertTrue(fetched.isPresent());
        assertEquals(5, fetched.get().getRating());
        assertEquals("Fast resolution! The printer works perfectly now.", fetched.get().getNotes());

        // Disallow duplicate feedback
        ServiceResult<TicketFeedback> duplicateResult = feedbackService.submitFeedback(
            testTicketId, 4, "Duplicate feedback", 1
        );
        assertTrue(duplicateResult.isFailure());
        assertEquals("ALREADY_EXISTS", ((ServiceResult.Failure<TicketFeedback>) duplicateResult).errorCode());
    }

    @Test
    @Order(7)
    public void testPasswordResetService() {
        ServiceResult<String> initResult = passwordResetService.initiateReset(
            "john@example.com", "/helpdesk", "http", "localhost", 8080
        );
        assertTrue(initResult.isSuccess());
        assertTrue(initResult.getData().isPresent());
        String resetUrl = initResult.getData().get();
        assertTrue(resetUrl.contains("/reset-password?token="));

        // Extract token
        String token = resetUrl.substring(resetUrl.lastIndexOf("token=") + 6);

        // Validate token
        ServiceResult<Integer> valResult = passwordResetService.validateToken(token);
        assertTrue(valResult.isSuccess());

        // Complete reset
        ServiceResult<Void> completeResult = passwordResetService.completeReset(token, "brandNewPass99", "brandNewPass99");
        assertTrue(completeResult.isSuccess());

        // Test login with new password
        ServiceResult<User> loginWithNew = userService.authenticate("john@example.com", "brandNewPass99");
        assertTrue(loginWithNew.isSuccess());

        // Restore original password
        ServiceResult<String> restoreInit = passwordResetService.initiateReset(
            "john@example.com", "/helpdesk", "http", "localhost", 8080
        );
        String restoreToken = restoreInit.getData().get().substring(restoreInit.getData().get().lastIndexOf("token=") + 6);
        passwordResetService.completeReset(restoreToken, "user123", "user123");
    }

    @Test
    @Order(8)
    public void testTicketServiceAssignmentAndQueries() {
        // Assign to technician
        ServiceResult<Void> assignRes = ticketService.assignTicket(testTicketId, 3);
        assertTrue(assignRes.isSuccess());

        // Update status to IN_PROGRESS
        ServiceResult<Void> statusRes = ticketService.updateStatus(testTicketId, "IN_PROGRESS");
        assertTrue(statusRes.isSuccess());

        // Update status to invalid value -> failure
        ServiceResult<Void> badStatus = ticketService.updateStatus(testTicketId, "NON_EXISTENT_STATUS");
        assertTrue(badStatus.isFailure());

        // Query user tickets via search criteria
        TicketSearchCriteria userCriteria = TicketSearchCriteria.of(null, "ALL", "ALL", 1, false);
        List<Ticket> userTickets = ticketService.getTicketsByCriteria(userCriteria);
        assertNotNull(userTickets);
        assertFalse(userTickets.isEmpty());

        // Query technician tickets
        List<Ticket> techTickets = ticketService.getTicketsAssignedToTech(3);
        assertNotNull(techTickets);
        assertFalse(techTickets.isEmpty());
        assertTrue(techTickets.stream().anyMatch(t -> t.getTicketId() == testTicketId));
    }

    @Test
    @Order(9)
    public void testServiceResultFunctionalMethods() {
        ServiceResult<Integer> success = ServiceResult.ok(42, "Computed value");
        ServiceResult<String> mapped = success.map(v -> "Value is " + v);
        assertTrue(mapped.isSuccess());
        assertEquals("Value is 42", mapped.getData().orElseThrow());

        // ifSuccess consumer test
        boolean[] executed = new boolean[]{ false };
        success.ifSuccess(val -> executed[0] = (val == 42));
        assertTrue(executed[0]);

        // ifFailure consumer test
        ServiceResult<Integer> failure = ServiceResult.fail("ERR_CODE", "Something broke");
        boolean[] failExecuted = new boolean[]{ false };
        failure.ifFailure(err -> failExecuted[0] = err.contains("broke"));
        assertTrue(failExecuted[0]);
    }

    @Test
    @Order(10)
    public void testEmailServiceConfigurationAndDispatch() {
        // Verify EmailService configuration reloading
        EmailService.reloadMailConfig();

        // Test sending simulated or live password reset email
        boolean dispatched = EmailService.sendPasswordResetEmail(
            "test_user@example.com", "Test Subject", "http://localhost:8080/helpdesk/reset-password?token=sample-test-token"
        );
        assertTrue(dispatched, "EmailService must succeed in dispatching (via SMTP or dev console)");
    }

    @Test
    @Order(11)
    @DisplayName("PasswordResetService - Security Question Retrieval and Password Reset")
    public void testSecurityQuestionPasswordResetService() {
        // Query security question for existing user
        ServiceResult<String> questionRes = passwordResetService.getSecurityQuestion("john@example.com");
        assertTrue(questionRes.isSuccess());
        assertEquals("What was the name of your first pet?", questionRes.getData().orElseThrow());

        // Query security question for non-existent user
        ServiceResult<String> nonExistentRes = passwordResetService.getSecurityQuestion("not_real_" + System.currentTimeMillis() + "@nobody.com");
        assertTrue(nonExistentRes.isFailure());

        // Empty field validation
        SecurityQuestionResetDTO emptyFields = new SecurityQuestionResetDTO("john@example.com", "", "newpass", "newpass");
        assertTrue(emptyFields.hasEmptyFields());
        ServiceResult<Void> emptyRes = passwordResetService.resetPasswordWithSecurityQuestion(emptyFields);
        assertTrue(emptyRes.isFailure());

        // Mismatched passwords
        SecurityQuestionResetDTO mismatchPasswords = new SecurityQuestionResetDTO("john@example.com", "fluffy", "newpass1", "newpass2");
        ServiceResult<Void> mismatchRes = passwordResetService.resetPasswordWithSecurityQuestion(mismatchPasswords);
        assertTrue(mismatchRes.isFailure());
        assertTrue(mismatchRes.getMessage().contains("match"));

        // Wrong security answer
        SecurityQuestionResetDTO wrongAnswer = new SecurityQuestionResetDTO("john@example.com", "incorrect_answer", "password123", "password123");
        ServiceResult<Void> wrongRes = passwordResetService.resetPasswordWithSecurityQuestion(wrongAnswer);
        assertTrue(wrongRes.isFailure());
        assertTrue(wrongRes.getMessage().toLowerCase().contains("security answer"));

        // Correct security answer resets password successfully
        SecurityQuestionResetDTO correctReset = new SecurityQuestionResetDTO("john@example.com", "fluffy", "user123", "user123");
        ServiceResult<Void> successRes = passwordResetService.resetPasswordWithSecurityQuestion(correctReset);
        assertTrue(successRes.isSuccess());

        // Authenticate with user service to confirm password was updated
        ServiceResult<User> authRes = userService.authenticate("john@example.com", "user123");
        assertTrue(authRes.isSuccess());
    }

    @Test
    @Order(12)
    @DisplayName("TicketService - Delete Ticket and Purge Permissions")
    public void testTicketServiceDeleteAndPurgePermissions() {
        User regularUser1 = User.builder().userId(1).name("John Doe").role("USER").build();
        User regularUser2 = User.builder().userId(2).name("Jane Smith").role("USER").build();
        User technician = User.builder().userId(3).name("Bhavani").role("TECHNICIAN").build();

        Ticket t = Ticket.builder()
            .userId(1)
            .title("Test Ticket For Service Layer Delete")
            .description("Description for delete test")
            .priority("LOW")
            .build();
        ServiceResult<Ticket> createResult = ticketService.createTicket(t, null, 0);
        assertTrue(createResult.isSuccess());
        int ticketId = createResult.getData().orElseThrow().getTicketId();

        // User 2 cannot delete User 1's ticket
        ServiceResult<Void> forbiddenDelete = ticketService.deleteTicket(ticketId, regularUser2);
        assertTrue(forbiddenDelete.isFailure());
        assertEquals("FORBIDDEN", forbiddenDelete.getErrorCode());

        // Regular user cannot purge all tickets
        ServiceResult<Integer> forbiddenPurge = ticketService.deleteAllTickets(regularUser1);
        assertTrue(forbiddenPurge.isFailure());
        assertEquals("FORBIDDEN", forbiddenPurge.getErrorCode());

        // Owner can delete their own ticket
        ServiceResult<Void> ownerDelete = ticketService.deleteTicket(ticketId, regularUser1);
        assertTrue(ownerDelete.isSuccess());
        assertTrue(ticketService.getTicketById(ticketId).isEmpty());

        // Technician can purge tickets
        ServiceResult<Integer> techPurge = ticketService.deleteAllTickets(technician);
        assertTrue(techPurge.isSuccess());
    }
}