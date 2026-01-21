package com.queueless.service;

import com.queueless.entity.Shop;
import com.queueless.entity.enums.SubscriptionPlan;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otp) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Verify your email – Queueless");

            helper.setText(buildHtmlTemplate(otp), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    public void sendSubscriptionSuccessEmail(
            Shop shop,
            SubscriptionPlan plan,
            LocalDate start,
            LocalDate end
    ) {

        String html = """
        <h2>🎉 Subscription Activated</h2>
        <p>Your <b>%s</b> plan has been successfully activated.</p>
        <p><b>Shop:</b> %s</p>
        <p><b>Start:</b> %s</p>
        <p><b>Expires:</b> %s</p>
        <p>Thank you for choosing Queueless 🚀</p>
    """.formatted(plan, shop.getName(), start, end);

        sendHtml(shop.getName(), "Queueless Subscription Activated", html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Email failed");
        }
    }

    public void sendSubscriptionExpiredEmail(
            Shop shop,
            SubscriptionPlan oldPlan
    ) {

        String html = """
        <h2>⚠ Subscription Expired</h2>
        <p>Your <b>%s</b> plan has expired.</p>
        <p>Your shop is now on <b>FREE</b> plan.</p>
        <p>Please upgrade to continue premium features.</p>
    """.formatted(oldPlan);

        sendHtml(shop.getName(),
                "Queueless Subscription Expired",
                html);
    }



    private String buildHtmlTemplate(String otp) {

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Email Verification</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,sans-serif;">
            
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center" style="padding:40px 0;">
                            
                            <table width="420" cellpadding="0" cellspacing="0"
                                   style="background:#ffffff;border-radius:12px;
                                          box-shadow:0 10px 30px rgba(0,0,0,0.15);
                                          padding:32px;">
                                
                                <tr>
                                    <td align="center" style="padding-bottom:20px;">
                                        <h2 style="margin:0;color:#4f46e5;">
                                            Queueless
                                        </h2>
                                        <p style="margin:6px 0 0;color:#6b7280;font-size:14px;">
                                            Smart Queue Management
                                        </p>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="color:#111827;font-size:15px;padding-bottom:16px;">
                                        Hello 👋,
                                    </td>
                                </tr>

                                <tr>
                                    <td style="color:#374151;font-size:14px;padding-bottom:20px;">
                                        Use the verification code below to complete your
                                        Queueless registration.
                                    </td>
                                </tr>

                                <tr>
                                    <td align="center" style="padding-bottom:24px;">
                                        <div style="
                                            display:inline-block;
                                            background:#eef2ff;
                                            color:#1e3a8a;
                                            font-size:28px;
                                            font-weight:700;
                                            letter-spacing:6px;
                                            padding:14px 28px;
                                            border-radius:10px;">
                                            %s
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="color:#374151;font-size:13px;padding-bottom:16px;">
                                        ⏳ This code is valid for <b>10 minutes</b>.
                                        Please do not share this code with anyone.
                                    </td>
                                </tr>

                                <tr>
                                    <td style="color:#6b7280;font-size:12px;padding-bottom:28px;">
                                        If you didn’t request this, you can safely ignore this email.
                                    </td>
                                </tr>

                                <tr>
                                    <td style="border-top:1px solid #e5e7eb;padding-top:16px;
                                               color:#6b7280;font-size:12px;text-align:center;">
                                        Created & maintained by <b>Vishal Bhawari</b><br>
                                        This is an automated email. Please do not reply.
                                    </td>
                                </tr>

                            </table>

                        </td>
                    </tr>
                </table>

            </body>
            </html>
        """.formatted(otp);
    }
}
