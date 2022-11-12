package com.example.email.services;

import java.util.Map;

public interface IMailSender {
    public Map<String, Object> sendMessage(String from, String to, String cc, String subject, String body);
}
