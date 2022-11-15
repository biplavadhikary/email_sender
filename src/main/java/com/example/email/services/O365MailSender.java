package com.example.email.services;

import javax.mail.internet.MimeMessage;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.http.CustomRequest;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserSendMailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Component
public class O365MailSender implements IMailSender {
    private static Logger LOGGER = LoggerFactory.getLogger(O365MailSender.class);

    public static boolean isMIME = false;
    public static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;
    private IAuthenticationProvider provider;
    private GraphServiceClient client;

    public void initializeProvider() {
        String clientId = "";
        List<String> scopes = Arrays.asList("");
        String clientSecret = "";
        String tenantId = "";

//        NationalCloud nationalCloud = NationalCloud.Global;
//
//        String userName = "testuser2@highradiusdmz.onmicrosoft.com";
//        String password = "Aut2dot0_testing2";

        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);

        this.provider = tokenCredentialAuthProvider;
    }

    public void buildClient() {
        GraphServiceClient client = GraphServiceClient
                .builder()
                .authenticationProvider(this.provider)
                .buildClient();

        this.client = client;
    }

    public O365MailSender() {
        LOGGER.info("Constructor");
        this.initializeProvider();
        this.buildClient();
    }

    public Message buildMessage(String from, String to, String cc, String subject, String body) {
        Message message = new Message();

        message.id = UUID.randomUUID().toString() + SDF.format(new Timestamp(System.currentTimeMillis()));
        message.subject = subject;

        message.body = new ItemBody();
        message.body.contentType = BodyType.TEXT;
        message.body.content = body;

        message.from = new Recipient();
        message.from.emailAddress = new EmailAddress();
        message.from.emailAddress.address = from;

        message.toRecipients = new ArrayList<>();
        for (String toRecipient :  to.split(",")) {
            Recipient recipient = new Recipient();
            recipient.emailAddress = new EmailAddress();
            recipient.emailAddress.address = toRecipient;
            message.toRecipients.add(recipient);
        }

        LOGGER.info("Required Message Object - " + message);
        return message;
    }

    public Map<String, Object> sendMailUsingSDK(String from, String to, String cc, String subject, String body) {
        Map<String, Object> responseMap = new HashMap<>();

        try {

            LOGGER.info("Building the message");
            Message message = this.buildMessage(from, to, cc, subject, body);
            UserSendMailRequest sendMailRequest = client.users(from).sendMail(
                    UserSendMailParameterSet
                    .newBuilder()
                    .withMessage(message)
                    .withSaveToSentItems(true)
                    .build()
            ).buildRequest();

            LOGGER.info(String.format("Sending the email as %s", from));
            sendMailRequest.post();

            LOGGER.info(String.format("Email sent to %s", to));


            responseMap.put("status", String.format("Email sent to %s", to));
            responseMap.put("success", true);

        } catch (Exception exception) {
            LOGGER.error("Error in sendMessage - ", exception);
            responseMap.put("success", false);
        }

        return responseMap;
    }

    public Map<String, Object> sendMailUsingCustomMIME(String from, String to, String cc, String subject, String body) {
        Map<String, Object> responseMap = new HashMap<>();

        try {

            LOGGER.info("Building the message");

            MimeMessage message = javaMailSender.createMimeMessage();

            message.setFrom(from);
            message.setRecipients(javax.mail.Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setText(body);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            message.writeTo(byteArrayOutputStream);
            String encodedMIME = Base64.getMimeEncoder().encodeToString(byteArrayOutputStream.toByteArray());

            String requestUrl = "https://graph.microsoft.com/v1.0/me/sendMail";
            List<HeaderOption> requestOptions = List.of(new HeaderOption("Content-Type", "text/plain"));
            CustomRequest<String> sendMailRequest = new CustomRequest<>(requestUrl, this.client, requestOptions, String.class);

            LOGGER.info(String.format("Sending the email as %s", from));
            String response = sendMailRequest.post(encodedMIME);

            LOGGER.info(String.format("Response %s", response));
            LOGGER.info(String.format("Email sent to %s", to));


            responseMap.put("status", String.format("Email sent to %s", to));
            responseMap.put("success", true);

        } catch (Exception exception) {
            LOGGER.error("Error in sendMessage - ", exception);
            responseMap.put("success", false);
        }

        return responseMap;
    }

    @Override
    public Map<String, Object> sendMessage(String from, String to, String cc, String subject, String body) {
        if (isMIME) {
            return this.sendMailUsingCustomMIME(from, to, cc, subject, body);
        } else {
            return this.sendMailUsingSDK(from, to, cc, subject, body);
        }
    }
}