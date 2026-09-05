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
        User tech = userDAO.authenticate("alex.tech@company.com", "tech123");
        assertNotNull(tech, "Technician Alex should authenticate");
        assertTrue(tech.isTechnician(), "Alex should have technician privileges");

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
        // Alex Tech user_id = 3
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
        techComment.setAuthorId(3); // Alex Tech
        techComment.setCommentText("Checked firewall logs. Certificate expired on the gateway. Renewing cert now.");
        boolean commentAdded = commentDAO.addComment(techComment);
        assertTrue(commentAdded, "Tech comment should be saved");

        // 2. Add discussion comment from User
        TicketComment userReply = new TicketComment();
        userReply.setTicketId(createdTicketId);
        userReply.setAuthorId(1); // John Doe
        userReply.setCommentText("Thanks Alex! Confirmed VPN connected successfully now.");
        commentDAO.addComment(userReply);

        // 3. Verify comments in thread
        List<TicketComment> thread = commentDAO.getCommentsByTicketId(createdTicketId);
        assertEquals(2, thread.size(), "Thread should have 2 comments");
        assertEquals("Alex Tech", thread.get(0).getAuthorName());
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
}