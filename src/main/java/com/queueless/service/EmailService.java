package com.queueless.service;

import com.queueless.entity.Shop;
import com.queueless.entity.enums.SubscriptionPlan;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String apiKey;

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final MediaType JSON
            = MediaType.parse("application/json");

    private final OkHttpClient client = new OkHttpClient();

    /* ===================== OTP EMAIL ===================== */

    public void sendOtp(String to, String otp) {
        String html = buildHtmlTemplate(otp);

        sendEmail(
                to,
                "Verify your email – Queueless",
                html
        );
    }

    /* ================= SUBSCRIPTION SUCCESS ================= */

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

        sendEmail(
                shop.getEmail(),
                "Queueless Subscription Activated",
                html
        );
    }

    /* ================= SUBSCRIPTION EXPIRED ================= */

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

        sendEmail(
                shop.getEmail(),
                "Queueless Subscription Expired",
                html
        );
    }

    /* ================= CORE RESEND SENDER ================= */

    private void sendEmail(String to, String subject, String html) {

        String payload = """
            {
              "from": "Queueless <onboarding@resend.dev>",
              "to": ["%s"],
              "subject": "%s",
              "html": "%s"
            }
        """.formatted(
                to,
                subject,
                html.replace("\"", "\\\"")
                        .replace("\n", "")
        );

        Request request = new Request.Builder()
                .url(RESEND_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                System.err.println(
                        "Resend email failed: " + response.body().string()
                );
            }

        } catch (Exception e) {
            System.err.println(
                    "Email error (ignored): " + e.getMessage()
            );
        }
    }

    /* ================= HTML OTP TEMPLATE ================= */

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
                                <h2 style="margin:0;color:#4f46e5;">Queueless</h2>
                                <p style="margin:6px 0 0;color:#6b7280;font-size:14px;">
                                    Smart Queue Management
                                </p>
                            </td>
                        </tr>

                        <tr><td>Hello 👋,</td></tr>

                        <tr>
                            <td style="padding:14px 0;">
                                Use the verification code below:
                            </td>
                        </tr>

                        <tr>
                            <td align="center">
                                <div style="
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
                            <td style="padding-top:20px;font-size:13px;">
                                ⏳ Valid for <b>10 minutes</b>.
                            </td>
                        </tr>

                        <tr>
                            <td style="padding-top:24px;font-size:12px;color:#6b7280;">
                                Created & maintained by <b>Vishal Bhawari</b><br>
                                Do not reply to this email.
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
