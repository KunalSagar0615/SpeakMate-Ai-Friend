package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MailServiceImpl implements MailService {

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    public MailServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendOtpEmail(String email, String otp) {

        System.out.println("=== STARTING EMAIL SEND ===");
        System.out.println("To: " + email);

        try {

            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f4f8fb;font-family:Arial,sans-serif;'><div style='max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1);'><div style='background:#38bdf8;padding:20px;text-align:center;'><h1 style='color:white;margin:0;'>SpeakMate AI Friend</h1><p style='color:#e0f7ff;margin:5px 0 0 0;'>Email Verification</p></div><div style='padding:30px;'><h2 style='color:#333;'>Hello User 👋</h2><p style='font-size:16px;color:#555;line-height:1.6;'>Thank you for registering with <b>SpeakMate AI Friend</b>. Please use the OTP below to verify your email address.</p><div style='background:#e0f7ff;border:2px dashed #38bdf8;border-radius:10px;padding:20px;text-align:center;margin:25px 0;'><p style='margin:0;color:#666;'>Your Verification OTP</p><h1 style='margin:10px 0;color:#0284c7;letter-spacing:5px;'>"
                            + otp +
                            "</h1></div><p style='font-size:15px;color:#ef4444;'><b>⏰ This OTP is valid for 5 minutes only.</b></p><p style='font-size:15px;color:#555;'>If you did not request this verification, please ignore this email.</p><p style='margin-top:30px;color:#555;'>Regards,<br><b>SpeakMate AI Friend Team</b></p></div></body></html>";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of(
                            "name", "SpeakMate AI Friend",
                            "email", "kunalsagar3041@gmail.com"
                    ),
                    "to", List.of(
                            Map.of("email", email)
                    ),
                    "subject", "SpeakMate AI Friend - Email Verification",
                    "htmlContent", html
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            System.out.println("Sending OTP email via Brevo API...");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    request,
                    String.class
            );

            System.out.println("Brevo Response: " + response.getBody());
            System.out.println("=== EMAIL SENT SUCCESSFULLY ===");

        } catch (Exception e) {
            System.out.println("=== EMAIL FAILED ===");
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }


    @Override
    public void sendLoginFailedAlertEmail(String email) {

        System.out.println("Sending login security alert to: " + email);

        try {

            String html =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head><meta charset='UTF-8'></head>" +
                            "<body style='margin:0;padding:0;background:#f4f8fb;font-family:Arial,sans-serif;'>" +

                            "<div style='max-width:600px;margin:30px auto;background:#ffffff;" +
                            "border-radius:12px;overflow:hidden;" +
                            "box-shadow:0 4px 15px rgba(0,0,0,0.1);'>" +

                            "<div style='background:#ef4444;padding:20px;text-align:center;'>" +
                            "<h1 style='color:white;margin:0;'>SpeakMate AI Friend</h1>" +
                            "<p style='color:#fee2e2;margin:5px 0 0 0;'>Security Alert</p>" +
                            "</div>" +

                            "<div style='padding:30px;'>" +

                            "<h2 style='color:#333;'>Login attempt blocked ⚠️</h2>" +

                            "<p style='font-size:16px;color:#555;line-height:1.6;'>" +
                            "We detected 5 unsuccessful login attempts on your SpeakMate AI Friend account." +
                            "</p>" +

                            "<div style='background:#fef2f2;border-left:4px solid #ef4444;" +
                            "padding:15px;margin:25px 0;border-radius:8px;'>" +

                            "<p style='margin:0;color:#555;font-size:15px;'>" +
                            "<b>Your account has been temporarily locked for 15 minutes.</b>" +
                            "</p>" +

                            "</div>" +

                            "<p style='font-size:15px;color:#555;line-height:1.6;'>" +
                            "If this was you, please wait until the temporary lock expires and try again." +
                            "</p>" +

                            "<p style='font-size:15px;color:#555;line-height:1.6;'>" +
                            "If you did not attempt to log in, someone may be trying to access your account. " +
                            "We recommend changing your password after you regain access." +
                            "</p>" +

                            "<p style='margin-top:30px;color:#555;'>" +
                            "Regards,<br>" +
                            "<b>SpeakMate AI Friend Team</b><br>" +
                            "</p>" +

                            "</div>" +
                            "</div>" +

                            "</body>" +
                            "</html>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of(
                            "name", "SpeakMate AI Friend",
                            "email", "kunalsagar3041@gmail.com"
                    ),
                    "to", List.of(
                            Map.of("email", email)
                    ),
                    "subject", "SpeakMate AI Friend - Security Alert",
                    "htmlContent", html
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email",
                            request,
                            String.class
                    );

            System.out.println("Security alert email sent: " + response.getBody());

        } catch (Exception e) {

            System.out.println("Failed to send login security alert email");

            e.printStackTrace();
        }
    }
}