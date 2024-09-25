package com.dkr.kumbarastore;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {

    private String username = "dewankerjarantingcibarusah@gmail.com"; // Ganti dengan email Anda
    private final String password = "ladj ldxs scey ivru"; // Ganti dengan password email Anda

    public void sendEmail(String recipientEmail, String subject, String body, MailCallback callback) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "Kumbara Store"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            if (callback != null) {
                callback.onSuccess();
            }
        } catch (MessagingException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
        } catch (UnsupportedEncodingException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    public interface MailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
