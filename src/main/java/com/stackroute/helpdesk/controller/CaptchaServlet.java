package com.stackroute.helpdesk.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;

@WebServlet("/captcha")
public class CaptchaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int width = 160;
        int height = 50;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Gradient background
        Color bgStart = new Color(240, 243, 246);
        Color bgEnd = new Color(220, 227, 235);
        GradientPaint gp = new GradientPaint(0, 0, bgStart, width, height, bgEnd);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        // Draw background noise lines
        for (int i = 0; i < 8; i++) {
            g2d.setColor(new Color(180 + RANDOM.nextInt(50), 180 + RANDOM.nextInt(50), 180 + RANDOM.nextInt(50)));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Generate 5 random characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        String captchaCode = sb.toString();

        // Save to session
        HttpSession session = request.getSession(true);
        session.setAttribute("CAPTCHA_CODE", captchaCode);

        // Draw text with individual jitter and rotation
        Font font = new Font("Arial", Font.BOLD, 30);
        g2d.setFont(font);

        Color[] textColors = {
            new Color(13, 110, 253), // Blue
            new Color(111, 66, 193), // Purple
            new Color(25, 135, 84),  // Green
            new Color(220, 53, 69),  // Red
            new Color(33, 37, 41)    // Dark
        };

        int x = 20;
        for (int i = 0; i < captchaCode.length(); i++) {
            char ch = captchaCode.charAt(i);
            g2d.setColor(textColors[RANDOM.nextInt(textColors.length)]);

            AffineTransform orig = g2d.getTransform();
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4; // -0.2 to +0.2 rads
            g2d.rotate(angle, x, 35);
            g2d.drawString(String.valueOf(ch), x, 36);
            g2d.setTransform(orig);

            x += 24 + RANDOM.nextInt(6);
        }

        // Add small dot noise
        for (int i = 0; i < 60; i++) {
            g2d.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200), 120));
            g2d.fillOval(RANDOM.nextInt(width), RANDOM.nextInt(height), 2, 2);
        }

        g2d.dispose();

        // Response headers: no cache
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "png", out);
        }
    }
}