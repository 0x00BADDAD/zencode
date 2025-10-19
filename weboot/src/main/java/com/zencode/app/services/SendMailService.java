package com.zencode.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class SendMailService{
    @Autowired
    JavaMailSender mailSender;

    public void sendMail(String sessionId, String errContentApiName, String errContentStackTrace) throws MessagingException{
        MimeMessage message = mailSender.createMimeMessage();
        // use the true flag to indicate you need a multipart message
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo("rantdubey6.9@gmail.com");
        helper.setFrom("atharv008@gmail.com");
        helper.setSubject("ERR || " + sessionId);
        String htmlString = "<html><body>" + "<h2>Errored API: " + errContentApiName + "</h2> <hr> <h3> stacktrace: " + errContentStackTrace + "</h3></body></html>";
        // use the true flag to indicate the text included is HTML
        helper.setText(htmlString, true);
        mailSender.send(message);
    }

}
