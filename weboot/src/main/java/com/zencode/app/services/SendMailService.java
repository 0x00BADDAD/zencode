package com.zencode.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class SendMailService{
    @Autowired
    JavaMailSender mailSender;

    @Autowired
    @Qualifier("mailOTPSender")
    JavaMailSender mailOTPSender;

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

    public void sendOTPMail(String OTP, String mailId) throws MessagingException{
        MimeMessage message = mailSender.createMimeMessage();
        // use the true flag to indicate you need a multipart message
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(mailId);
        helper.setFrom("kurokamigenjisan@gmail.com");
        helper.setSubject("[OTP] " + OTP + " -- for login into zencode");
        String htmlString = "<html><body><h3> OTP for logging into zencode is " + OTP + ". It expires in 60 seconds.</h3></body></html>";
        // use the true flag to indicate the text included is HTML
        helper.setText(htmlString, true);
        mailOTPSender.send(message);
    }
}
