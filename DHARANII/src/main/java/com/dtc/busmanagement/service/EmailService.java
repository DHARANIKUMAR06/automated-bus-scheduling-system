package com.dtc.busmanagement.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
