package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendEmail(String to, String subject, String body) {
        // High fidelity mock logging mimicking a real mail send operation.
        // This is robust, compiles immediately, and doesn't require setting up SMTP configurations.
        logger.info("---------------- SMTP MAIL CLIENT ----------------");
        logger.info("TO: {}", to);
        logger.info("SUBJECT: {}", subject);
        logger.info("BODY: \n{}", body);
        logger.info("-------------------------------------------------");
        logger.info("Status: Email dispatched successfully through Mock SMTP Gateway.");
    }
}
