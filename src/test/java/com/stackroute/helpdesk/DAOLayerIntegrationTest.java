package com.stackroute.helpdesk;

import com.stackroute.helpdesk.dao.*;
import com.stackroute.helpdesk.model.*;
import com.stackroute.helpdesk.util.DBUtil;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOLayerIntegrationTest {

    private static UserDAO userDAO;
    private static TicketDAO ticketDAO;
    private static CommentDAO commentDAO;
    private static FeedbackDAO feedbackDAO;
    private static PasswordResetDAO passwordResetDAO;

    private static int createdUserId;
    private static int createdTicketId;
    private static String resetToken;

    @BeforeAll
    public static void init() {
        DBUtil.initializeDatabase();
        userDAO = new UserDAO();
        ticketDAO = new TicketDAO();
        commentDAO = new CommentDAO();
        feedbackDAO = new FeedbackDAO();
        passwordResetDAO = new PasswordResetDAO();
    }

    @Test
    @Order(1)
    @DisplayName("UserDAO - Register, Find, Authenticate, and Query Technicians")
    public void testUserDAOOperations() {
        long ts = System.currentTimeMillis();
        String testEmail = "dao_user_" + ts + "@test.com";

        // Register with Security Question
        User user = User.builder()
            .name("DAO Test Subject")
            .email(testEmail)
            .password("passDAO123")
            .role("USER")
            .securityQuestion("What was the name of your first pet?")
            .securityAnswer("fluffy")
            .build();

        boolean registered = userDAO.registerUser(user);
        assertTrue(registered);
        createdUserId = user.getUserId();
        assertTrue(createdUserId > 0);

        // Find by Email
        User byEmail = userDAO.findByEmail(testEmail);
        assertNotNull(byEmail);
        assertEquals("DAO Test Subject", byEmail.getName());
        assertEquals("What was the name of your first pet?", byEmail.getSecurityQuestion());

        // Find by ID
        User byId = userDAO.findById(createdUserId);
        assertNotNull(byId);
        assertEquals(testEmail, byId.getEmail());

        // Security Question query
        String retrievedQuestion = userDAO.getSecurityQuestionByEmail(testEmail);
        assertEquals("What was the name of your first pet?", retrievedQuestion);

        // Verify Security Answer - Wrong Answer Fails
        boolean wrongAnswer = userDAO.verifySecurityAnswerAndUpdatePassword(testEmail, "wrong_answer", "newPassFail");
        assertFalse(wrongAnswer);

        // Verify Security Answer - Correct Answer Updates Password
        boolean resetSuccess = userDAO.verifySecurityAnswerAndUpdatePassword(testEmail, "FLUFFY", "passDAO123");
        assertTrue(resetSuccess);

        // Authenticate - Success
        User authSuccess = userDAO.authenticate(testEmail, "passDAO123");
        assertNotNull(authSuccess);
        assertEquals(createdUserId, authSuccess.getUserId());

        // Authenticate - Fail
        User authFail = userDAO.authenticate(testEmail, "wrongpassword");
        assertNull(authFail);

        // Check Email Taken
        assertTrue(userDAO.isEmailTaken(testEmail));
        assertFalse(userDAO.isEmailTaken("non_existent_" + ts + "@nobody.com"));

        // Get All Technicians
        List<User> techs = userDAO.getAllTechnicians();
        assertNotNull(techs);
        assertFalse(techs.isEmpty());
        assertTrue(techs.stream().allMatch(t -> t.getRole().equals("TECHNICIAN") || t.getRole().equals("ADMIN")));
    }

    @Test
    @Order(2)
    @DisplayName("TicketDAO & CommentDAO - Create Ticket, Post Comments, and Query History")
    public void testTicketAndCommentDAO() throws Exception {
        // Create parent ticket
        Ticket ticket = Ticket.builder()
            .userId(createdUserId)
            .title("VPN Gateway Connection Timeout")
            .description("Unable to establish tunnel via remote portal.")
            .priority("HIGH")
            .status("OPEN")
            .build();

        createdTicketId = ticketDAO.createTicket(ticket, null, 0);
        assertTrue(createdTicketId > 0);

        // CommentDAO: Add comment 1
        TicketComment c1 = TicketComment.builder()
            .ticketId(createdTicketId)
            .authorId(3) // Seeded technician Bhavani
            .commentText("Checking radius server authentication logs.")
            .build();
        boolean c1Added = commentDAO.addComment(c1);
        assertTrue(c1Added);
        assertTrue(c1.getCommentId() > 0);

        // CommentDAO: Add comment 2
        TicketComment c2 = TicketComment.builder()
            .ticketId(createdTicketId)
            .authorId(createdUserId)
            .commentText("Client re-attempted and received error code 800.")
            .build();
        boolean c2Added = commentDAO.addComment(c2);
        assertTrue(c2Added);

        // Retrieve comments
        List<TicketComment> comments = commentDAO.getCommentsByTicketId(createdTicketId);
        assertEquals(2, comments.size());
        assertEquals("Checking radius server authentication logs.", comments.get(0).getCommentText());
        assertNotNull(comments.get(0).getAuthorName());
    }

    @Test
    @Order(3)
    @DisplayName("FeedbackDAO - Submit CSAT Rating, Duplicate Check, and Average Calculation")
    public void testFeedbackDAO() {
        assertFalse(feedbackDAO.hasFeedback(createdTicketId));
        assertNull(feedbackDAO.getFeedbackByTicketId(createdTicketId));

        // Submit 5-star rating
        TicketFeedback feedback = TicketFeedback.builder()
            .ticketId(createdTicketId)
            .rating(5)
            .notes("Technician pinpointed the bad firewall rule rapidly.")
            .build();

        boolean submitted = feedbackDAO.submitFeedback(feedback);
        assertTrue(submitted);

        // Verify feedback retrieval
        assertTrue(feedbackDAO.hasFeedback(createdTicketId));
        TicketFeedback fetched = feedbackDAO.getFeedbackByTicketId(createdTicketId);
        assertNotNull(fetched);
        assertEquals(5, fetched.getRating());
        assertEquals("Technician pinpointed the bad firewall rule rapidly.", fetched.getNotes());

        // Check overall CSAT average
        double avgRating = feedbackDAO.getAverageRating();
        assertTrue(avgRating >= 1.0 && avgRating <= 5.0);
    }

    @Test
    @Order(4)
    @DisplayName("PasswordResetDAO - Token Lifecycle: Create, Validate, Update Password, and Use")
    public void testPasswordResetDAO() {
        resetToken = UUID.randomUUID().toString();
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000L);

        // Create token
        boolean created = passwordResetDAO.createResetToken(createdUserId, resetToken, expiry);
        assertTrue(created);

        // Validate token
        Integer resolvedUserId = passwordResetDAO.getUserIdByValidToken(resetToken);
        assertNotNull(resolvedUserId);
        assertEquals(createdUserId, resolvedUserId);

        // Update user password
        boolean updated = passwordResetDAO.updatePassword(createdUserId, "newSecurePassword2026");
        assertTrue(updated);

        // Verify user can now authenticate with new password
        User user = userDAO.findById(createdUserId);
        User authNew = userDAO.authenticate(user.getEmail(), "newSecurePassword2026");
        assertNotNull(authNew);

        // Mark token as used
        boolean marked = passwordResetDAO.markTokenUsed(resetToken);
        assertTrue(marked);

        // Token should now be invalid/consumed
        Integer reusedUserId = passwordResetDAO.getUserIdByValidToken(resetToken);
        assertNull(reusedUserId, "Used token must not be accepted again");

        // Clean up expired tokens
        assertDoesNotThrow(() -> passwordResetDAO.cleanupExpiredTokens());
    }
}
