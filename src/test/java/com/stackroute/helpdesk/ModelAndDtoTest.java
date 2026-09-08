package com.stackroute.helpdesk;

import com.stackroute.helpdesk.dto.SecurityQuestionResetDTO;
import com.stackroute.helpdesk.dto.TicketSearchCriteria;
import com.stackroute.helpdesk.dto.TicketStatisticsDTO;
import com.stackroute.helpdesk.dto.UserRegistrationDTO;
import com.stackroute.helpdesk.exception.*;
import com.stackroute.helpdesk.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndDtoTest {

    @Test
    @DisplayName("User Model - Builder, Getters, Setters, and Role Predicates")
    public void testUserModel() {
        User user = User.builder()
            .userId(101)
            .name("Alice Smith")
            .email("alice@example.com")
            .password("secret123")
            .role("TECHNICIAN")
            .securityQuestion("What was the name of your first pet?")
            .securityAnswer("fluffy")
            .build();

        assertEquals(101, user.getUserId());
        assertEquals("Alice Smith", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("secret123", user.getPassword());
        assertEquals("TECHNICIAN", user.getRole());
        assertEquals("What was the name of your first pet?", user.getSecurityQuestion());
        assertEquals("fluffy", user.getSecurityAnswer());
        assertTrue(user.isTechnician());
        assertFalse(user.isEndUser());

        // Test mutating setters
        user.setRole("USER");
        user.setSecurityQuestion("What city were you born in?");
        user.setSecurityAnswer("Dallas");
        assertEquals("What city were you born in?", user.getSecurityQuestion());
        assertEquals("Dallas", user.getSecurityAnswer());
        assertFalse(user.isTechnician());
        assertTrue(user.isEndUser());

        user.setRole("USER");

        // Equals, HashCode, ToString
        User sameUser = User.builder()
            .userId(101)
            .name("Alice Smith")
            .email("alice@example.com")
            .password("secret123")
            .role("USER")
            .securityQuestion("What city were you born in?")
            .securityAnswer("Dallas")
            .build();
        User diffUser = User.builder()
            .userId(102)
            .name("Bob Jones")
            .email("bob@example.com")
            .password("pass456")
            .role("TECHNICIAN")
            .securityQuestion("What was the name of your first pet?")
            .securityAnswer("rex")
            .build();
        assertEquals(user, sameUser);
        assertEquals(user.hashCode(), sameUser.hashCode());
        assertNotEquals(user, diffUser);
        assertTrue(user.toString().contains("Alice Smith"));

        // No-arg constructor
        User emptyUser = new User();
        assertNull(emptyUser.getName());
    }

    @Test
    @DisplayName("Ticket Model - SLA calculations, badge styling, and predicates")
    public void testTicketModelSLA() {
        long now = System.currentTimeMillis();
        Timestamp pastTime = new Timestamp(now - (5 * 3600 * 1000L)); // 5 hours ago
        Timestamp futureTime = new Timestamp(now - (1 * 3600 * 1000L)); // 1 hour ago

        // High priority ticket created 5 hours ago (SLA is 4 hours) -> Breached!
        Ticket breachedTicket = Ticket.builder()
            .ticketId(201)
            .userId(1)
            .title("Network switch offline")
            .description("Switch failed")
            .priority("HIGH")
            .status("OPEN")
            .createdAt(pastTime)
            .build();

        assertTrue(breachedTicket.isSlaBreached(), "High priority ticket past 4 hours must be breached");
        assertEquals("badge bg-danger", breachedTicket.getSlaBadgeClass());
        assertTrue(breachedTicket.getSlaTimeRemaining().toLowerCase().contains("overdue"));

        // Medium priority ticket created 1 hour ago (SLA is 24 hours) -> Not Breached!
        Ticket activeTicket = Ticket.builder()
            .ticketId(202)
            .priority("MEDIUM")
            .status("IN_PROGRESS")
            .createdAt(futureTime)
            .build();

        assertFalse(activeTicket.isSlaBreached());
        assertFalse(activeTicket.getSlaTimeRemaining().contains("OVERDUE"));

        // Closed/Resolved ticket -> Never breached
        Ticket resolvedTicket = Ticket.builder()
            .priority("HIGH")
            .status("RESOLVED")
            .createdAt(pastTime)
            .build();
        assertFalse(resolvedTicket.isSlaBreached(), "Resolved ticket should not report SLA breach");
        assertEquals("badge bg-secondary", resolvedTicket.getSlaBadgeClass());

        Ticket closedTicket = Ticket.builder()
            .priority("CRITICAL")
            .status("CLOSED")
            .createdAt(pastTime)
            .build();
        assertFalse(closedTicket.isSlaBreached());

        // Low priority (72h) ticket
        Ticket lowTicket = Ticket.builder()
            .priority("LOW")
            .status("OPEN")
            .createdAt(new Timestamp(now))
            .build();
        assertFalse(lowTicket.isSlaBreached());
        assertTrue(lowTicket.getSlaTimeRemaining().contains("left"));

        // Equals, HashCode, ToString
        Ticket sameTicket = Ticket.builder()
            .ticketId(201)
            .userId(1)
            .title("Network switch offline")
            .description("Switch failed")
            .priority("HIGH")
            .status("OPEN")
            .createdAt(pastTime)
            .build();
        assertEquals(breachedTicket, sameTicket);
        assertEquals(breachedTicket.hashCode(), sameTicket.hashCode());
        assertTrue(breachedTicket.toString().contains("Network switch offline"));
    }

    @Test
    @DisplayName("TicketComment and TicketFeedback Models")
    public void testCommentAndFeedbackModels() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        TicketComment comment = TicketComment.builder()
            .commentId(10)
            .ticketId(201)
            .authorId(5)
            .authorName("Technician Bob")
            .commentText("Investigating issue")
            .createdAt(now)
            .build();

        assertEquals(10, comment.getCommentId());
        assertEquals(201, comment.getTicketId());
        assertEquals(5, comment.getAuthorId());
        assertEquals("Technician Bob", comment.getAuthorName());
        assertEquals("Investigating issue", comment.getCommentText());
        assertEquals(now, comment.getCreatedAt());

        TicketFeedback feedback = TicketFeedback.builder()
            .feedbackId(1)
            .ticketId(201)
            .rating(5)
            .notes("Superb assistance!")
            .createdAt(now)
            .build();

        assertEquals(1, feedback.getFeedbackId());
        assertEquals(5, feedback.getRating());
        assertEquals("Superb assistance!", feedback.getNotes());

        StatusCount sc = new StatusCount("OPEN", 12);
        assertEquals("OPEN", sc.getStatus());
        assertEquals(12, sc.getCount());
        sc.setStatus("RESOLVED");
        sc.setCount(20);
        assertEquals("RESOLVED", sc.getStatus());
        assertEquals(20, sc.getCount());
    }

    @Test
    @DisplayName("DTO - TicketSearchCriteria Sanitization and Defaults")
    public void testTicketSearchCriteriaDTO() {
        TicketSearchCriteria criteria = TicketSearchCriteria.of("  network  ", "OPEN", "HIGH", 1, true);
        assertEquals("network", criteria.keyword());
        assertEquals("OPEN", criteria.status());
        assertEquals("HIGH", criteria.priority());
        assertEquals(1, criteria.userId());
        assertTrue(criteria.isTechnician());

        // Default / Blank checks
        TicketSearchCriteria defaultCriteria = TicketSearchCriteria.of("   ", null, "ALL", null, false);
        assertNull(defaultCriteria.keyword(), "Blank keyword must be sanitized to null");
        assertEquals("ALL", defaultCriteria.status());
        assertEquals("ALL", defaultCriteria.priority());
        assertFalse(defaultCriteria.isTechnician());
    }

    @Test
    @DisplayName("DTO - TicketStatisticsDTO and UserRegistrationDTO Validations")
    public void testTicketStatisticsAndUserRegistrationDTO() {
        TicketStatisticsDTO stats = new TicketStatisticsDTO(19, 5, 2, 8, 4, 3, 2);

        assertEquals(19, stats.totalCount());
        assertEquals(5, stats.openCount());
        assertEquals(2, stats.inProgressCount());
        assertEquals(8, stats.resolvedCount());
        assertEquals(4, stats.closedCount());
        assertEquals(3, stats.breachedCount());
        assertEquals(2, stats.unassignedCount());

        // UserRegistrationDTO
        UserRegistrationDTO validDto = new UserRegistrationDTO(
            "Jane Doe", "jane@company.com", "pass123", "USER", "XY789", "XY789"
        );
        assertFalse(validDto.hasEmptyFields());
        assertTrue(validDto.isCaptchaValid());

        // Case-insensitive captcha
        UserRegistrationDTO lowerCaptcha = new UserRegistrationDTO(
            "Jane Doe", "jane@company.com", "pass123", "USER", "xy789", "XY789"
        );
        assertTrue(lowerCaptcha.isCaptchaValid());

        // Mismatch captcha
        UserRegistrationDTO badCaptcha = new UserRegistrationDTO(
            "Jane Doe", "jane@company.com", "pass123", "USER", "WRONG", "XY789"
        );
        assertFalse(badCaptcha.isCaptchaValid());

        // Missing session captcha fallback
        UserRegistrationDTO noSessionCaptcha = new UserRegistrationDTO(
            "Jane Doe", "jane@company.com", "pass123", "USER", null, null
        );
        assertTrue(noSessionCaptcha.isCaptchaValid(), "Absence of session captcha allows fallback in test/dev");

        // Empty field permutations
        assertTrue(new UserRegistrationDTO(null, "j@c.com", "pass", "USER", "A", "A").hasEmptyFields());
        assertTrue(new UserRegistrationDTO("  ", "j@c.com", "pass", "USER", "A", "A").hasEmptyFields());
        assertTrue(new UserRegistrationDTO("Jane", null, "pass", "USER", "A", "A").hasEmptyFields());
        assertTrue(new UserRegistrationDTO("Jane", "j@c.com", null, "USER", "A", "A").hasEmptyFields());
        assertTrue(new UserRegistrationDTO("Jane", "j@c.com", "pass", "USER", "A", "A", null, "ans").hasEmptyFields());
        assertTrue(new UserRegistrationDTO("Jane", "j@c.com", "pass", "USER", "A", "A", "q", "").hasEmptyFields());

        // SecurityQuestionResetDTO
        SecurityQuestionResetDTO validReset = new SecurityQuestionResetDTO("john@example.com", "fluffy", "pass123", "pass123");
        assertEquals("john@example.com", validReset.email());
        assertEquals("fluffy", validReset.securityAnswer());
        assertEquals("pass123", validReset.newPassword());
        assertEquals("pass123", validReset.confirmPassword());
        assertFalse(validReset.hasEmptyFields());

        assertTrue(new SecurityQuestionResetDTO("", "fluffy", "pass123", "pass123").hasEmptyFields());
        assertTrue(new SecurityQuestionResetDTO("john@example.com", null, "pass123", "pass123").hasEmptyFields());
        assertTrue(new SecurityQuestionResetDTO("john@example.com", "fluffy", "  ", "pass123").hasEmptyFields());
        assertTrue(new SecurityQuestionResetDTO("john@example.com", "fluffy", "pass123", null).hasEmptyFields());
    }

    @Test
    @DisplayName("Exception Hierarchy - Message and Cause Propagation")
    public void testExceptionHierarchy() {
        Throwable cause = new RuntimeException("Underlying root failure");

        AppException appEx = new AppException("App err", cause);
        assertEquals("App err", appEx.getMessage());
        assertEquals(cause, appEx.getCause());

        DatabaseException dbEx = new DatabaseException("DB err", cause);
        assertEquals("DB err", dbEx.getMessage());

        ValidationException valEx = new ValidationException("Validation err");
        assertEquals("Validation err", valEx.getMessage());

        ResourceNotFoundException notFound = new ResourceNotFoundException("Ticket", 101);
        assertTrue(notFound.getMessage().contains("Ticket") && notFound.getMessage().contains("101"));

        AuthenticationException authEx = new AuthenticationException("Auth err");
        assertEquals("Auth err", authEx.getMessage());

        UnauthorizedAccessException unauthEx = new UnauthorizedAccessException("Forbidden");
        assertEquals("Forbidden", unauthEx.getMessage());
    }
}
