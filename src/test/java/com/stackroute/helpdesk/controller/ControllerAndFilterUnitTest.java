package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.filter.AuthenticationFilter;
import com.stackroute.helpdesk.filter.CharacterEncodingFilter;
import com.stackroute.helpdesk.filter.GlobalExceptionFilter;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.util.DBUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class ControllerAndFilterUnitTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private FilterChain filterChain;

    @BeforeAll
    public static void setupDB() {
        DBUtil.initializeDatabase();
    }

    @Test
    @DisplayName("LoginServlet - GET forwards to login.jsp")
    public void testLoginServletDoGet() throws Exception {
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        LoginServlet servlet = new LoginServlet();
        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("LoginServlet - POST successful authentication redirects based on role")
    public void testLoginServletDoPostSuccess() throws Exception {
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("password")).thenReturn("user123");
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/helpdesk");

        LoginServlet servlet = new LoginServlet();
        servlet.doPost(request, response);

        verify(session).setAttribute(eq("user"), any(User.class));
        verify(response).sendRedirect("/helpdesk/tickets");
    }

    @Test
    @DisplayName("LoginServlet - POST invalid credentials forwards back with error")
    public void testLoginServletDoPostFailure() throws Exception {
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("password")).thenReturn("badpassword");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        LoginServlet servlet = new LoginServlet();
        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("RegisterServlet - GET forwards to register.jsp and starts session")
    public void testRegisterServletDoGet() throws Exception {
        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        RegisterServlet servlet = new RegisterServlet();
        servlet.doGet(request, response);

        verify(request).getSession(true);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("RegisterServlet - POST captcha mismatch retains inputs and forwards")
    public void testRegisterServletDoPostCaptchaMismatch() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("CAPTCHA_CODE")).thenReturn("CORRECT");
        when(request.getParameter("name")).thenReturn("Test Person");
        when(request.getParameter("email")).thenReturn("testperson@company.com");
        when(request.getParameter("password")).thenReturn("pass123");
        when(request.getParameter("role")).thenReturn("USER");
        when(request.getParameter("captcha")).thenReturn("WRONG");
        when(request.getParameter("securityQuestion")).thenReturn("What was the name of your first pet?");
        when(request.getParameter("securityAnswer")).thenReturn("fluffy");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        RegisterServlet servlet = new RegisterServlet();
        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("CAPTCHA"));
        verify(request).setAttribute("name", "Test Person");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("ForgotPasswordServlet - POST missing/invalid captcha handles error")
    public void testForgotPasswordServletBadCaptcha() throws Exception {
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("captcha")).thenReturn("BAD");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("CAPTCHA_CODE")).thenReturn("GOOD");
        when(request.getRequestDispatcher("/forgot-password.jsp")).thenReturn(dispatcher);

        ForgotPasswordServlet servlet = new ForgotPasswordServlet();
        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("CAPTCHA"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("ForgotPasswordServlet - POST valid captcha step 1 retrieves security question and advances to step 2")
    public void testForgotPasswordServletSuccess() throws Exception {
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("captcha")).thenReturn("GOOD");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("CAPTCHA_CODE")).thenReturn("GOOD");
        when(request.getContextPath()).thenReturn("/helpdesk");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getRequestDispatcher("/forgot-password.jsp")).thenReturn(dispatcher);

        ForgotPasswordServlet servlet = new ForgotPasswordServlet();
        servlet.doPost(request, response);

        verify(request).setAttribute("step", 2);
        verify(request).setAttribute(eq("securityQuestion"), anyString());
        verify(request).setAttribute(eq("resetUrl"), contains("reset-password?token="));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("ForgotPasswordServlet - POST reset_with_question success redirects to login")
    public void testForgotPasswordServletResetWithQuestionSuccess() throws Exception {
        when(request.getParameter("action")).thenReturn("reset_with_question");
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("securityAnswer")).thenReturn("fluffy");
        when(request.getParameter("newPassword")).thenReturn("user123");
        when(request.getParameter("confirmPassword")).thenReturn("user123");
        when(request.getContextPath()).thenReturn("/helpdesk");

        ForgotPasswordServlet servlet = new ForgotPasswordServlet();
        servlet.doPost(request, response);

        verify(response).sendRedirect("/helpdesk/login?msg=password_reset");
    }

    @Test
    @DisplayName("ForgotPasswordServlet - POST reset_with_question wrong answer forwards with error")
    public void testForgotPasswordServletResetWithQuestionWrongAnswer() throws Exception {
        when(request.getParameter("action")).thenReturn("reset_with_question");
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("securityAnswer")).thenReturn("wronganswer");
        when(request.getParameter("newPassword")).thenReturn("newPassword123");
        when(request.getParameter("confirmPassword")).thenReturn("newPassword123");
        when(request.getRequestDispatcher("/forgot-password.jsp")).thenReturn(dispatcher);

        ForgotPasswordServlet servlet = new ForgotPasswordServlet();
        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(request).setAttribute("step", 2);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("ResetPasswordServlet - GET with invalid token forwards with error")
    public void testResetPasswordServletDoGetInvalidToken() throws Exception {
        when(request.getParameter("token")).thenReturn("invalid-uuid-token");
        when(request.getRequestDispatcher("/reset-password.jsp")).thenReturn(dispatcher);

        ResetPasswordServlet servlet = new ResetPasswordServlet();
        servlet.doGet(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("ResetPasswordServlet - POST password mismatch shows error")
    public void testResetPasswordServletDoPostMismatch() throws Exception {
        when(request.getParameter("token")).thenReturn("dummy-token");
        when(request.getParameter("password")).thenReturn("passOne123");
        when(request.getParameter("confirmPassword")).thenReturn("passTwo456");
        when(request.getRequestDispatcher("/reset-password.jsp")).thenReturn(dispatcher);

        ResetPasswordServlet servlet = new ResetPasswordServlet();
        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("do not match"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("LogoutServlet - Invalidates session and redirects to login")
    public void testLogoutServlet() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/helpdesk");

        LogoutServlet servlet = new LogoutServlet();
        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/helpdesk/login?msg=logged_out");
    }

    @Test
    @DisplayName("CaptchaServlet - Generates image, sets headers, stores code in session")
    public void testCaptchaServlet() throws Exception {
        when(request.getSession(true)).thenReturn(session);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
            @Override public void write(int b) throws IOException { baos.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        CaptchaServlet servlet = new CaptchaServlet();
        servlet.doGet(request, response);

        verify(response).setContentType("image/png");
        verify(response).setHeader(eq("Cache-Control"), contains("no-cache"));
        verify(session).setAttribute(eq("CAPTCHA_CODE"), anyString());
        assertTrue(baos.size() > 0, "PNG image bytes must be output");
    }

    @Test
    @DisplayName("ExportTicketsServlet - Streams RFC-4180 CSV with UTF-8 BOM headers")
    public void testExportTicketsServlet() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User techUser = User.builder().userId(3).name("Tech Alex").role("TECHNICIAN").build();
        when(session.getAttribute("user")).thenReturn(techUser);
        when(request.getParameter("search")).thenReturn("");
        when(request.getParameter("status")).thenReturn("ALL");
        when(request.getParameter("priority")).thenReturn("ALL");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        ExportTicketsServlet servlet = new ExportTicketsServlet();
        servlet.doGet(request, response);

        verify(response).setContentType("text/csv; charset=UTF-8");
        verify(response).setHeader(eq("Content-Disposition"), contains("attachment; filename="));
        String output = sw.toString();
        assertTrue(output.contains("Ticket ID") && output.contains("Title"));
    }

    @Test
    @DisplayName("ExportTicketsServlet - Rejects regular users with 403 Forbidden")
    public void testExportTicketsServletDeniedForRegularUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User regularUser = User.builder().userId(1).name("John Doe").role("USER").build();
        when(session.getAttribute("user")).thenReturn(regularUser);

        ExportTicketsServlet servlet = new ExportTicketsServlet();
        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("TicketDetailServlet - Redirects regular user trying to access another user's ticket")
    public void testTicketDetailServletUnauthorizedAccess() throws Exception {
        Ticket existingTicket = new TicketDAO().getAllTickets().stream().findFirst().orElseThrow();
        when(request.getParameter("id")).thenReturn(String.valueOf(existingTicket.getTicketId()));
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/helpdesk");
        User otherUser = User.builder().userId(existingTicket.getUserId() + 9999).name("Other User").role("USER").build();
        when(session.getAttribute("user")).thenReturn(otherUser);

        TicketDetailServlet servlet = new TicketDetailServlet();
        servlet.doGet(request, response);

        verify(response).sendRedirect("/helpdesk/tickets?error=unauthorized_ticket");
    }

    @Test
    @DisplayName("AttachmentDownloadServlet - Denies regular user downloading another user's attachment")
    public void testAttachmentDownloadServletUnauthorizedAccess() throws Exception {
        Ticket existingTicket = new TicketDAO().getAllTickets().stream().findFirst().orElseThrow();
        when(request.getParameter("id")).thenReturn(String.valueOf(existingTicket.getTicketId()));
        when(request.getSession(false)).thenReturn(session);
        User otherUser = User.builder().userId(existingTicket.getUserId() + 9999).name("Other User").role("USER").build();
        when(session.getAttribute("user")).thenReturn(otherUser);

        AttachmentDownloadServlet servlet = new AttachmentDownloadServlet();
        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("AuthenticationFilter - Blocks regular users from accessing /export-tickets")
    public void testAuthenticationFilterBlocksExportForRegularUser() throws Exception {
        when(request.getServletPath()).thenReturn("/export-tickets");
        when(request.getSession(false)).thenReturn(session);
        User regularUser = User.builder().userId(1).name("John Doe").role("USER").build();
        when(session.getAttribute("user")).thenReturn(regularUser);

        AuthenticationFilter filter = new AuthenticationFilter();
        filter.doFilter(request, response, filterChain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthenticationFilter - Allows public assets and login paths")
    public void testAuthenticationFilterPublicPath() throws Exception {
        when(request.getServletPath()).thenReturn("/login");

        AuthenticationFilter filter = new AuthenticationFilter();
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthenticationFilter - Redirects unauthenticated access to protected path")
    public void testAuthenticationFilterProtectedPathNoAuth() throws Exception {
        when(request.getContextPath()).thenReturn("/helpdesk");
        when(request.getServletPath()).thenReturn("/tickets");
        when(request.getSession(false)).thenReturn(null);

        AuthenticationFilter filter = new AuthenticationFilter();
        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect("/helpdesk/login?error=unauthenticated");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("CharacterEncodingFilter - Sets UTF-8 encoding on request and response")
    public void testCharacterEncodingFilter() throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.doFilter(request, response, filterChain);

        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("GlobalExceptionFilter - Catches runtime exception and routes to error.jsp")
    public void testGlobalExceptionFilter() throws Exception {
        when(request.getContextPath()).thenReturn("/helpdesk");
        when(request.getRequestDispatcher("/error.jsp")).thenReturn(dispatcher);
        doThrow(new RuntimeException("Simulated unexpected catastrophe"))
            .when(filterChain).doFilter(request, response);

        GlobalExceptionFilter filter = new GlobalExceptionFilter();
        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute(eq("incidentId"), anyString());
        verify(request).setAttribute(eq("exceptionMessage"), contains("Simulated unexpected catastrophe"));
        verify(dispatcher).forward(request, response);
    }
}
