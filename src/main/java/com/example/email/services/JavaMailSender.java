package com.example.email.services;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JavaMailSender implements IMailSender {
	private static Logger LOGGER = LoggerFactory.getLogger(JavaMailSender.class);

	@Autowired
	private org.springframework.mail.javamail.JavaMailSender emailSender;

	private MimeMessage mimeMessageSenderWithHelper(String from, String to, String cc, String subject, String body) throws Exception {
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setTo(to);
		helper.setCc(cc);
		helper.setFrom(new InternetAddress(to));
		helper.setSubject(subject);
		helper.setText(body);

		return helper.getMimeMessage();
	}

	private MimeMessage mimeMessageSender(String from, String to, String cc, String subject, String body) throws Exception {
		MimeMessage message = emailSender.createMimeMessage();

		message.setFrom(from);
		message.setRecipients(Message.RecipientType.TO, to);
		message.setSubject(subject);
		message.setText(body);

		return message;
	}

	private SimpleMailMessage simpleMessageSender(String from, String to, String cc, String subject, String body) throws Exception {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);

		return message;
	}

	@Override
	public Map<String, Object> sendMessage(String from, String to, String cc, String subject, String body) {
		Map<String, Object> responseMap = new HashMap<>();

		try {
			MimeMessage message = this.mimeMessageSender(from, to, cc, subject, body);

			LOGGER.info(String.format("Sending the email as %s", from));
			emailSender.send(message);
			LOGGER.info(String.format("Email sent to %s", to));

			responseMap.put("status", String.format("Email sent to %s", to));
			responseMap.put("success", true);

		} catch (Exception exception) {
			LOGGER.error("Error in sendMessage - ", exception);
			responseMap.put("success", false);
		}

		return responseMap;
	}
}