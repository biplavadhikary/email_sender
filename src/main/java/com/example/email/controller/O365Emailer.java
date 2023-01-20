package com.example.email.controller;

import com.example.email.services.O365MailSender;
import com.example.email.services.O365MailSenderV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class O365Emailer {

    @Autowired
    private O365MailSenderV2 o356MailSenderV2;

    @GetMapping(path = "/email-0365", produces = {"application/json"})
    public Map<String, Object> email(@RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> emailDetails = payload == null? new HashMap<>() : payload;

//        String toAddress = emailDetails.getOrDefault("to", "testuser1@highradiusdmz.onmicrosoft.com,biplav.adhikary@highradius.com").toString();
        String toAddress = emailDetails.getOrDefault("to", "testuser1@highradiusdmz.onmicrosoft.com").toString();
        String ccAddress = emailDetails.getOrDefault("cc", "").toString();
        String subject = emailDetails.getOrDefault("subject", "Test Subject").toString();
        String body = emailDetails.getOrDefault("body", "Test Body").toString();

        String fromAddress = "testuser2@highradiusdmz.onmicrosoft.com";

        Map<String, Object> responseMap = o356MailSenderV2.sendMessage(fromAddress, toAddress, ccAddress, subject, body);

        return responseMap;
    }

}