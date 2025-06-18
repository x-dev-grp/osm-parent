package com.xdev.mailSender.services;

import com.xdev.mailSender.models.MailRequest;
import jakarta.mail.MessagingException;

public interface MailService {
    void sendEmail(MailRequest request) throws MessagingException;
}

