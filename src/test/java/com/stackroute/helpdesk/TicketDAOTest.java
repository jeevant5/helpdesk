package com.stackroute.helpdesk;

import com.stackroute.helpdesk.dao.CommentDAO;
import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.dao.UserDAO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.util.DBUtil;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketDAOTest {

    private static TicketDAO ticketDAO;
    private static CommentDAO commentDAO;
    private static UserDAO userDAO;
    private static int createdTicketId;

    @BeforeAll
    public static void setup() {
        DBUtil.initializeDatabase();
        ticketDAO = new TicketDAO();
        commentDAO = new CommentDAO();
        userDAO = new UserDAO();
    }

    @Test
    @Order(1)
    public void testUserAuthentication() {
        // Test seeded user authentication
        User tech = userDAO.authenticate("bhavani.tech@company.com", "tech123");
        assertNotNull(tech, "Technician Bhavani should authenticate");
        assertTrue(tech.isTechnician(), "Bhavani should have technician privileges");

        User user = userDAO.authenticate("john@example.com", "user123");
        assertNotNull(user, "User John should authenticate");
        assertEquals("USER", user.getRole());
    }

    @Test
    @Order(2)
    public void testTicketSubmissionWithBlobAttachment() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setUserId(1); // John Doe
        ticket.setTitle("Network VPN Connection Failure");
        ticket.setDescription("VPN client disconnects with Error 800 during TLS handshake.");
        ticket.setPriority("HIGH");
        ticket.setStatus("OPEN");
        ticket.setAttachmentName("vpn_error.log");
        ticket.setAttachmentType("text/plain");

        byte[] logContent = "2026-09-05 ERROR [vpn-client] Connection reset by peer at 10.0.0.1".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(logContent);

        int ticketId = ticketDAO.createTicket(ticket, stream, logContent.length);
        assertTrue(ticketId > 0, "Ticket ID should be generated and positive");
        createdTicketId = ticketId;

        // Verify ticket retrieval
        Ticket fetched = ticketDAO.getTicketById(ticketId);
        assertNotNull(fetched);
        assertEquals("Network VPN Connection Failure", fetched.getTitle());
        assertEquals("HIGH", fetched.getPriority());
        assertEquals("OPEN", fetched.getStatus());
        assertTrue(fetched.isHasAttachment(), "Ticket should report having BLOB attachment");

        // Verify reading BLOB stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean readSuccess = ticketDAO.writeAttachment(ticketId, out);
        assertTrue(readSuccess, "Attachment should be read from BLOB");
        assertEquals(new String(logContent, StandardCharsets.UTF_8), out.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    @Order(3)
    public void testTechnicianDashboardAggregateQuery() {
        // Requirement: Real-time summary using aggregate queries (COUNT(*) GROUP BY status)
        Map<String, Integer> counts = ticketDAO.getStatusCounts();
        assertNotNull(counts, "Status counts map should not be null");
        assertTrue(counts.containsKey("OPEN"));
        assertTrue(counts.containsKey("IN_PROGRESS"));
        assertTrue(counts.containsKey("RESOLVED"));
        assertTrue(counts.get("OPEN") >= 1, "There should be at least 1 OPEN ticket");
    }

    @Test
    @Order(4)
    public void testTechnicianSelfAssignment() {
        // Requirement: Technicians assign tickets to themselves
        // Bhavani user_id = 3
        boolean assigned = ticketDAO.assignTicket(createdTicketId, 3);
        assertTrue(assigned, "Ticket assignment to tech should succeed");

        Ticket updated = ticketDAO.getTicketById(createdTicketId);
        assertEquals(3, updated.getTechId().intValue(), "Technician ID should be set to 3");
        assertEquals("IN_PROGRESS", updated.getStatus(), "Status should transition to IN_PROGRESS upon assignment");
    }

    @Test
    @Order(5)
    public void testTicketCommentThreadAndStatusTransition() {
        // Requirement: TicketCommentServlet handles adding updates to a ticket, 
        // dynamically changing ticket status from OPEN to IN_PROGRESS or RESOLVED.

        // 1. Add discussion comment from Tech
        TicketComment techComment = new TicketComment();
        techComment.setTicketId(createdTicketId);
        techComment.setAuthorId(3); // Bhavani
        techComment.setCommentText("Checked firewall logs. Certificate expired on the gateway. Renewing cert now.");
        boolean commentAdded = commentDAO.addComment(techComment);
        assertTrue(commentAdded, "Tech comment should be saved");

        // 2. Add discussion comment from User
        TicketComment userReply = new TicketComment();
        userReply.setTicketId(createdTicketId);
        userReply.setAuthorId(1); // John Doe
        userReply.setCommentText("Thanks Bhavani! Confirmed VPN connected successfully now.");
        commentDAO.addComment(userReply);

        // 3. Verify comments in thread
        List<TicketComment> thread = commentDAO.getCommentsByTicketId(createdTicketId);
        assertEquals(2, thread.size(), "Thread should have 2 comments");
        assertEquals("Bhavani", thread.get(0).getAuthorName());
        assertEquals("John Doe", thread.get(1).getAuthorName());

        // 4. Update status dynamically to RESOLVED
        boolean statusUpdated = ticketDAO.updateStatus(createdTicketId, "RESOLVED");
        assertTrue(statusUpdated, "Status update to RESOLVED should succeed");

        Ticket resolvedTicket = ticketDAO.getTicketById(createdTicketId);
        assertEquals("RESOLVED", resolvedTicket.getStatus());

        // Check aggregate counts update
        Map<String, Integer> countsAfter = ticketDAO.getStatusCounts();
        assertTrue(countsAfter.get("RESOLVED") >= 1, "Resolved count should be at least 1");
    }

    @Test
    @Order(6)
    public void testUserAndTechnicianRegistration() {
        // Test checking existing email
        assertTrue(userDAO.isEmailTaken("john@example.com"), "John's email should be taken");
        
        long ts = System.currentTimeMillis();
        String techEmail = "tech_" + ts + "@company.com";
        String userEmail = "user_" + ts + "@company.com";
        assertFalse(userDAO.isEmailTaken(techEmail), "New email should not be taken");

        // Test registering a new Technician
        User newTech = new User();
        newTech.setName("Marcus Vance");
        newTech.setEmail(techEmail);
        newTech.setPassword("securePass123");
        newTech.setRole("TECHNICIAN");

        boolean regSuccess = userDAO.registerUser(newTech);
        assertTrue(regSuccess, "Registration should succeed");
        assertTrue(newTech.getUserId() > 0, "Generated user ID should be positive");

        // Verify login with newly registered technician
        User authenticated = userDAO.authenticate(techEmail, "securePass123");
        assertNotNull(authenticated);
        assertEquals("Marcus Vance", authenticated.getName());
        assertTrue(authenticated.isTechnician(), "Should have technician privileges");

        // Test registering a new End-User
        User newUser = new User();
        newUser.setName("Emily Rose");
        newUser.setEmail(userEmail);
        newUser.setPassword("userPass456");
        newUser.setRole("USER");

        assertTrue(userDAO.registerUser(newUser));
        User authUser = userDAO.authenticate(userEmail, "userPass456");
        assertNotNull(authUser);
        assertFalse(authUser.isTechnician());
    }

    @Test
    @Order(7)
    public void testPasswordResetDAO() {
        com.stackroute.helpdesk.dao.PasswordResetDAO resetDAO = new com.stackroute.helpdesk.dao.PasswordResetDAO();
        
        // Find seeded user
        User user = userDAO.findByEmail("john@example.com");
        assertNotNull(user, "User john should exist");

        // Create token
        String token = java.util.UUID.randomUUID().toString();
        java.sql.Timestamp expiry = new java.sql.Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);
        boolean created = resetDAO.createResetToken(user.getUserId(), token, expiry);
        assertTrue(created, "Reset token should be saved to database");

        // Validate token
        Integer validUserId = resetDAO.getUserIdByValidToken(token);
        assertNotNull(validUserId, "Token should validate");
        assertEquals(user.getUserId(), validUserId.intValue());

        // Reset password
        boolean passUpdated = resetDAO.updatePassword(validUserId, "newSecret999");
        assertTrue(passUpdated, "Password update should succeed");
        resetDAO.markTokenUsed(token);

        // Verify login with new password
        User newAuth = userDAO.authenticate("john@example.com", "newSecret999");
        assertNotNull(newAuth, "Should authenticate with new password");

        // Token should now be invalid (marked used)
        assertNull(resetDAO.getUserIdByValidToken(token), "Used token should no longer validate");

        // Restore original password
        resetDAO.updatePassword(user.getUserId(), "user123");
    }

    @Test
    @Order(8)
    public void testFeedbackDAO() {
        com.stackroute.helpdesk.dao.FeedbackDAO feedbackDAO = new com.stackroute.helpdesk.dao.FeedbackDAO();
        
        com.stackroute.helpdesk.model.TicketFeedback feedback = new com.stackroute.helpdesk.model.TicketFeedback();
        feedback.setTicketId(createdTicketId);
        feedback.setRating(5);
        feedback.setNotes("Great fast resolution on the VPN issue!");

        boolean saved = feedbackDAO.addFeedback(feedback);
        assertTrue(saved, "Feedback should be saved successfully");

        com.stackroute.helpdesk.model.TicketFeedback retrieved = feedbackDAO.getFeedbackByTicketId(createdTicketId);
        assertNotNull(retrieved, "Feedback should be retrievable");
        assertEquals(5, retrieved.getRating());
        assertEquals("Great fast resolution on the VPN issue!", retrieved.getNotes());
    }

    @Test
    @Order(9)
    public void testTicketSearchAndFiltering() {
        // Search by keyword "VPN" as technician
        List<Ticket> results = ticketDAO.searchTickets("VPN", "ALL", "ALL", 0, true);
        assertFalse(results.isEmpty(), "Search should find tickets matching 'VPN'");
        assertTrue(results.stream().anyMatch(t -> t.getTicketId() == createdTicketId));

        // Filter by status RESOLVED
        List<Ticket> resolvedList = ticketDAO.searchTickets(null, "RESOLVED", "ALL", 0, true);
        assertFalse(resolvedList.isEmpty());
        assertTrue(resolvedList.stream().allMatch(t -> "RESOLVED".equals(t.getStatus())));

        // Filter by user ID as non-tech
        List<Ticket> userTickets = ticketDAO.searchTickets(null, "ALL", "HIGH", 1, false);
        assertFalse(userTickets.isEmpty());
        assertTrue(userTickets.stream().allMatch(t -> t.getUserId() == 1));
    }

    @Test
    @Order(10)
    public void testSlaCalculations() {
        Ticket highTicket = new Ticket();
        highTicket.setPriority("HIGH");
        highTicket.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis() - 1000L * 60 * 60 * 5)); // 5 hours ago
        assertEquals(4, highTicket.getSlaHours());
        assertTrue(highTicket.isSlaBreached(), "5 hours > 4 hours SLA should be breached");
        assertTrue(highTicket.getSlaStatusText().startsWith("SLA Breached"));
        assertEquals("badge bg-danger", highTicket.getSlaBadgeClass());

        Ticket lowTicket = new Ticket();
        lowTicket.setPriority("LOW");
        lowTicket.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis())); // now
        assertEquals(72, lowTicket.getSlaHours());
        assertFalse(lowTicket.isSlaBreached());
        assertTrue(lowTicket.getSlaStatusText().contains("left"));
        assertEquals("badge bg-success", lowTicket.getSlaBadgeClass());
    }
}