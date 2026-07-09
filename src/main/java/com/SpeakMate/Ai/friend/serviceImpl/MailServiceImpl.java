package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.service.MailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String email, String otp) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f4f8fb;font-family:Arial,sans-serif;'><div style='max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1);'><div style='background:#38bdf8;padding:20px;text-align:center;'><h1 style='color:white;margin:0;'>SpeakMate AI Friend</h1><p style='color:#e0f7ff;margin:5px 0 0 0;'>Email Verification</p></div><div style='padding:30px;'><h2 style='color:#333;'>Hello User 👋</h2><p style='font-size:16px;color:#555;line-height:1.6;'>Thank you for registering with <b>SpeakMate AI Friend</b>. Please use the OTP below to verify your email address.</p><div style='background:#e0f7ff;border:2px dashed #38bdf8;border-radius:10px;padding:20px;text-align:center;margin:25px 0;'><p style='margin:0;color:#666;'>Your Verification OTP</p><h1 style='margin:10px 0;color:#0284c7;letter-spacing:5px;'>" + otp + "</h1></div><p style='font-size:15px;color:#ef4444;'><b>⏰ This OTP is valid for 5 minutes only.</b></p><p style='font-size:15px;color:#555;'>If you did not request this verification, please ignore this email.</p><div style='background:#f0f9ff;border-left:4px solid #38bdf8;padding:15px;margin-top:25px;border-radius:8px;'><p style='margin:0;color:#555;font-size:14px;'><b>Having trouble with Login or Registration?</b><br><br>📞 Contact: <b>Kunal Ananda Sagar</b><br>📱 Mobile: <b>7249176496</b><br>💬 Drop a message and I'll help you resolve the issue.</p></div><p style='margin-top:30px;color:#555;'>Regards,<br><b>Kunal Ananda Sagar</b><br>Founder, SpeakMate AI Friend</p></div><div style='background:#f8fafc;padding:15px;text-align:center;color:#888;font-size:12px;'>© 2026 SpeakMate AI Friend. All rights reserved.</div></div></body></html>";
            helper.setTo(email);
            helper.setSubject("SpeakMate AI Friend - Email Verification");
            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}