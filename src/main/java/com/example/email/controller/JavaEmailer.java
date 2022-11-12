package com.example.email.controller;

import com.example.email.services.IMailSender;

import com.example.email.services.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class JavaEmailer {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@GetMapping(path = "/email", produces = {"application/json"})
	public Map<String, Object> email(@RequestBody(required = false) Map<String, Object> payload) {

		Map<String, Object> emailDetails = payload == null? new HashMap<>() : payload;

		String toAddress = emailDetails.getOrDefault("to", "biplov@live.com").toString();
		String ccAddress = emailDetails.getOrDefault("cc", "").toString();
		String subject = emailDetails.getOrDefault("subject", "Test Subject").toString();
		String body = emailDetails.getOrDefault("body", "Test Body").toString();

		String fromAddress = "abiplov@gmail.com";

		Map<String, Object> responseMap = javaMailSender.sendMessage(fromAddress, toAddress, ccAddress, subject, body);

		return responseMap;
	}
	
}