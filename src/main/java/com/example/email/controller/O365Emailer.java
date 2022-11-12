package com.example.email.controller;

import com.example.email.services.IMailSender;
import com.example.email.services.JavaMailSender;

import com.example.email.services.O365MailSender;
import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class O365Emailer {

    @Autowired
    private O365MailSender o356MailSender;

    @GetMapping(path = "/email-0365", produces = {"application/json"})
    public Map<String, Object> email(@RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> emailDetails = payload == null? new HashMap<>() : payload;

        String toAddress = emailDetails.getOrDefault("to", "testuser1@highradiusdmz.onmicrosoft.com").toString();
        String ccAddress = emailDetails.getOrDefault("cc", "").toString();
        String subject = emailDetails.getOrDefault("subject", "Test Subject").toString();
        String body = emailDetails.getOrDefault("body", "Test Body").toString();

        String fromAddress = "testuser2@highradiusdmz.onmicrosoft.com";

        Map<String, Object> responseMap = o356MailSender.sendMessage(fromAddress, toAddress, ccAddress, subject, body);

        return responseMap;
    }

}