package com.stackroute.helpdesk.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final Properties mailProps = new Properties();
    private static volatile boolean smtpConfigured = false;
    private static String fromEmail = "support@helpdesk.local";
    private static String smtpUser = "";
    private static String smtpPass = "";
    private static volatile String lastError = null;

    static {
        reloadMailConfig();
    }

    public static synchronized void reloadMailConfig() {
        smtpConfigured = false;
        lastError = null;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                mailProps.load(is);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load db.properties: " + e.getMessage());
        }

        // Support environment variables & system properties overrides
        String host = getPropOrEnv("mail.smtp.host", "SMTP_HOST");
        String port = getPropOrEnv("mail.smtp.port", "SMTP_PORT");
        String user = getPropOrEnv("mail.smtp.username", "SMTP_USERNAME");
        String pass = getPropOrEnv("mail.smtp.password", "SMTP_PASSWORD");
        String from = getPropOrEnv("mail.smtp.from", "SMTP_FROM");

        if (host != null && !host.trim().isEmpty() && !host.contains("example")) {
            mailProps.put("mail.smtp.host", host.trim());
            mailProps.put("mail.smtp.port", (port != null && !port.trim().isEmpty()) ? port.trim() : "587");
            mailProps.put("mail.smtp.auth", "true");
            mailProps.put("mail.smtp.starttls.enable", "true");
            
            smtpUser = (user != null) ? user.trim() : "";
            smtpPass = (pass != null) ? pass.trim() : "";
            if (from != null && !from.trim().isEmpty()) {
                fromEmail = from.trim();
            }
            smtpConfigured = true;
            LOGGER.info("EmailService: Active SMTP configured -> Host: " + host + ", From: " + fromEmail);
        } else {
            smtpConfigured = false;
            LOGGER.info("EmailService: SMTP not configured. Running in Local Development / On-Screen Link mode.");
        }
    }

    private static String getPropOrEnv(String propKey, String envKey) {
        String val = System.getProperty(propKey);
        if (val != null && !val.trim().isEmpty()) return val;
        val = mailProps.getProperty(propKey);
        if (val != null && !val.trim().isEmpty()) return val;
        val = System.getenv(envKey);
        if (val != null && !val.trim().isEmpty()) return val;
        return null;
    }

    public static boolean isSmtpConfigured() {
        return smtpConfigured;
    }

    public static String getLastError() {
        return lastError;
    }

    public static boolean sendPasswordResetEmail(String recipientEmail, String userName, String resetUrl) {
        lastError = null;
        if (smtpConfigured) {
            try {
                Session session;
                if (!smtpUser.isEmpty()) {
                    session = Session.getInstance(mailProps, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(smtpUser, smtpPass);
                        }
                    });
                } else {
                    session = Session.getInstance(mailProps);
                }

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail, "Helpdesk IT Support"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Helpdesk Portal - Password Reset Request");

                String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                        + "<h2 style='color: #0d6efd;'>Password Reset Request</h2>"
                        + "<p>Hello <b>" + userName + "</b>,</p>"
                        + "<p>We received a request to reset your Helpdesk account password. Click the button below to choose a new password. This link is valid for 30 minutes.</p>"
                        + "<div style='text-align: center; margin: 30px 0;'>"
                        + "<a href='" + resetUrl + "' style='background-color: #0d6efd; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Reset My Password</a>"
                        + "</div>"
                        + "<p style='color: #6c757d; font-size: 13px;'>Or paste this link in your browser: <br><a href='" + resetUrl + "'>" + resetUrl + "</a></p>"
                        + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                        + "<p style='color: #999; font-size: 12px;'>If you did not request a password reset, you can safely ignore this email.</p>"
                        + "</div>";

                message.setContent(htmlContent, "text/html; charset=utf-8");
                Transport.send(message);
                LOGGER.info("Password reset email sent to: " + recipientEmail);
                return true;
            } catch (Exception e) {
                lastError = e.getMessage();
                LOGGER.log(Level.WARNING, "Failed to send SMTP email (" + e.getMessage() + "). Falling back to local dispatch.");
            }
        }

        // Safe Fallback for Local / Development / Classroom environments:
        System.out.println("=================================================================");
        System.out.println(">>> [MAIL API DISPATCH] Password Reset Link Generated");
        System.out.println(">>> Recipient: " + recipientEmail + " (" + userName + ")");
        System.out.println(">>> Subject:   Helpdesk Portal - Password Reset Request");
        System.out.println(">>> Clickable Reset Link: " + resetUrl);
        if (lastError != null) {
            System.out.println(">>> SMTP Note: " + lastError);
        }
        System.out.println("=================================================================");
        return !smtpConfigured || lastError == null;
    }
}