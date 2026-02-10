package com.visor.school.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Email service for sending emails
 * In production, this would integrate with an email service provider (SendGrid, AWS SES, etc.)
 */
@Service
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Send email verification email
     */
    public void sendVerificationEmail(String email, String firstName, String verificationUrl) {
        logger.info("Sending verification email to: {}", email);
        logger.debug("Verification URL: {}", verificationUrl);
        
        // In production, this would send an actual email
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String firstName, String resetUrl) {
        logger.info("Sending password reset email to: {}", email);
        logger.debug("Reset URL: {}", resetUrl);
        
        // In production, this would send an actual email
    }

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(String email, String firstName) {
        logger.info("Sending welcome email to: {}", email);
        // In production, this would send an actual email
    }
}
