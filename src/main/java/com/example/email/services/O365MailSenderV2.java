package com.example.email.services;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.example.util.FileUtil;
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
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Component
public class O365MailSenderV2 implements IMailSender {
    private static Logger LOGGER = LoggerFactory.getLogger(O365MailSenderV2.class);

    public static boolean isMIME = false;
    public static boolean isMIMEMultipart = true;
    public static boolean isSuccessScenario = true;
    public static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
    public static String actionableMessageBodyFilePathSuccess = "actionableMessages/initialEmail.html";
    public static String actionableMessageBodyFilePathFail = "actionableMessagesOld/testActionableMessage2_fail.html";
    public static String ampBodyFilePath = "actionableMessagesOld/testAMP1.html";

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;
    private IAuthenticationProvider provider;
    private GraphServiceClient<?> client;

    public void initializeProvider() {
        String clientId = "";
        List<String> scopes = Arrays.asList("");
        String clientSecret = "";
        String tenantId = "";

        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);

        this.provider = tokenCredentialAuthProvider;
    }

    public void buildClient() {
        GraphServiceClient<?> client = GraphServiceClient
                .builder()
                .authenticationProvider(this.provider)
                .buildClient();

        this.client = client;
    }

    public O365MailSenderV2() {
        LOGGER.info("Constructor");
        this.initializeProvider();
        this.buildClient();
    }

    public Message buildMessage(String from, String to, String cc, String subject, String body) throws IOException {
        Message message = new Message();

        message.id = UUID.randomUUID().toString() + SDF.format(new Timestamp(System.currentTimeMillis()));
        message.subject = "[2.0] Through GRAPH SDK - " + (isSuccessScenario? " SUCCESS SCENARIO " : " FAIL SCENARIO ") + subject;

        message.body = new ItemBody();
        message.body.contentType = BodyType.HTML;

        String bodyContent = FileUtil.readAsStringFromFile(isSuccessScenario? actionableMessageBodyFilePathSuccess : actionableMessageBodyFilePathFail, true);
        LOGGER.info("actionableBody - " + bodyContent);
        message.body.content = bodyContent;

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

    public MimeMessage getMultiPartMimeMessage(String from, String to, String cc, String subject, String body) throws MessagingException, IOException {
        LOGGER.info("Using Multipart MIME");
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        mimeMessage.setFrom(from);
        mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, to);
        mimeMessage.setSubject("MIME - MULTIPART - " + subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(String.format("Text Part - " + body), "text/plain; charset=UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(String.format("HTML Part - " + body), "text/html; charset=UTF-8");

        MimeBodyPart ampPart = new MimeBodyPart();
        String ampBody = FileUtil.readAsStringFromFile(ampBodyFilePath, true);
        LOGGER.info("ampBody - " + ampBody);
        ampPart.setContent(ampBody, "text/x-amp-html; charset=UTF-8");

        MimeMultipart mimeMultiPartAlt = new MimeMultipart("alternative");
        mimeMultiPartAlt.addBodyPart(ampPart);
        mimeMultiPartAlt.addBodyPart(htmlPart);
        mimeMultiPartAlt.addBodyPart(textPart);

        MimeBodyPart wrapperPart = new MimeBodyPart();
        wrapperPart.setContent(mimeMultiPartAlt);

        MimeMultipart mimeMultiPartMixed = new MimeMultipart("mixed");
        mimeMultiPartMixed.addBodyPart(wrapperPart);

        mimeMessage.setContent(mimeMultiPartMixed);

        return mimeMessage;
    }

    public MimeMessage getSimpleMimeMessage(String from, String to, String cc, String subject, String body) throws MessagingException {
        LOGGER.info("Using Simple MIME");
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(from);
        message.setRecipients(javax.mail.Message.RecipientType.TO, to);
        message.setSubject("MIME - SIMPLE - " + subject);
        message.setText(body);

        return message;
    }

    public Map<String, Object> sendMailUsingCustomMIME(String from, String to, String cc, String subject, String body) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            LOGGER.info("Building the message");

            MimeMessage message = isMIMEMultipart? getMultiPartMimeMessage(from, to, cc, subject, body) :
                    getSimpleMimeMessage(from, to, cc, subject, body);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            message.writeTo(byteArrayOutputStream);
            String encodedMIME = Base64.getMimeEncoder().encodeToString(byteArrayOutputStream.toByteArray());

            String requestUrl = "https://graph.microsoft.com/v1.0/users/" + from + "/sendMail";
            List<HeaderOption> requestOptions = Arrays.asList(
                    new HeaderOption("Content-Type", "text/plain")
//                    new HeaderOption("MIME-Version", "1.0")
            );

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