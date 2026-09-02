package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.model.DeliveryMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String RED = "#E60000";
    private static final String INK = "#1a1a1a";
    private static final String MUTED = "#6b6b6b";
    private static final String BORDER = "#e8e8e8";
    private static final String CARD_BG = "#fafafa";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendTouristWelcomeEmail(String toEmail, TouristWelcomeEmailContext ctx) {
        String htmlBody = """
                <div style="font-family: -apple-system, Arial, sans-serif; background-color: #f4f4f4; padding: 24px 12px;">
                <div style="max-width: 600px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                %s
                <div style="padding: 32px 28px;">
                %s
                %s
                %s
                %s
                </div>
                %s
                </div>
                </div>
                """.formatted(
                        header(),
                        greetingAndSummary(ctx),
                        deliverySection(ctx),
                        actionButtons(ctx),
                        faqSection(ctx),
                        footer()
                );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Vodafone Tourist Pass is ready — " + ctx.orderRef());
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully via Gmail SMTP to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }

    private String header() {
        return """
                <div style="background-color: %s; padding: 28px 28px; text-align: center; color: white;">
                <h1 style="margin: 0; font-size: 26px; letter-spacing: -0.5px;">vodafone</h1>
                <p style="margin: 6px 0 0 0; text-transform: uppercase; font-size: 12px; letter-spacing: 1px; opacity: 0.9;">Albania Tourist Pass</p>
                </div>
                """.formatted(RED);
    }

    private String greetingAndSummary(TouristWelcomeEmailContext ctx) {
        String minutesLine = ctx.minutesAllowance() != null
                ? ctx.minutesAllowance() + " min calls"
                : "Data only";

        return """
                <h2 style="margin: 0 0 4px 0; color: %s; font-size: 22px;">Mirësevini në Shqipëri, %s.</h2>
                <p style="color: %s; font-size: 15px; margin: 0 0 24px 0;">Your pack is active. Here's everything you need for your trip.</p>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background: %s; border: 1px solid %s; border-radius: 10px;">
                <tr>
                <td style="padding: 18px 20px;">
                <p style="margin: 0 0 2px 0; font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; color: %s;">Order %s</p>
                <p style="margin: 0; font-size: 17px; font-weight: 700; color: %s;">%s</p>
                <p style="margin: 6px 0 0 0; font-size: 13px; color: %s;">%s &middot; %s &middot; %s days</p>
                </td>
                </tr>
                </table>
                """.formatted(
                        INK, escapeHtml(ctx.touristFirstName()),
                        MUTED,
                        CARD_BG, BORDER,
                        MUTED, ctx.orderRef(),
                        INK, escapeHtml(ctx.packTitle()),
                        MUTED, ctx.dataAllowance(), minutesLine, ctx.durationDays()
                );
    }

    private String deliverySection(TouristWelcomeEmailContext ctx) {
        if (ctx.deliveryMethod() == DeliveryMethod.ESIM) {
            return ctx.esimQrUrl() != null ? esimReadySection(ctx) : esimPendingSection();
        }
        return physicalSimSection();
    }

    private String esimReadySection(TouristWelcomeEmailContext ctx) {
        String manualCodeRow = ctx.esimManualCode() != null
                ? """
                  <p style="margin: 12px 0 0 0; font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; color: %s;">Can't scan?</p>
                  <p style="margin: 4px 0 0 0; font-family: 'Courier New', monospace; font-size: 13px; background: #fff; border: 1px solid %s; border-radius: 6px; padding: 8px 10px; color: %s; word-break: break-all;">%s</p>
                  """.formatted(MUTED, BORDER, INK, ctx.esimManualCode())
                : "";

        return """
                <h3 style="margin: 28px 0 10px 0; font-size: 15px; color: %s;">Activate your eSIM</h3>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background: %s; border: 1px solid %s; border-radius: 10px;">
                <tr>
                <td style="padding: 20px; text-align: center;">
                <img src="%s" width="150" height="150" alt="eSIM activation QR code" style="display: block; margin: 0 auto; border-radius: 8px; border: 1px solid %s;" />
                <p style="margin: 12px 0 0 0; font-size: 13px; color: %s;">Open Camera or Settings &rarr; Cellular &rarr; Add eSIM and scan this code.</p>
                %s
                </td>
                </tr>
                </table>
                """.formatted(INK, CARD_BG, BORDER, ctx.esimQrUrl(), BORDER, MUTED, manualCodeRow);
    }

    private String esimPendingSection() {
        return """
                <h3 style="margin: 28px 0 10px 0; font-size: 15px; color: %s;">Activate your eSIM</h3>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background: %s; border: 1px solid %s; border-radius: 10px;">
                <tr>
                <td style="padding: 18px 20px;">
                <p style="margin: 0; font-size: 14px; color: %s;">Your eSIM QR code is being generated and will land in a separate email shortly, before you land.</p>
                </td>
                </tr>
                </table>
                """.formatted(INK, CARD_BG, BORDER, MUTED);
    }

    private String physicalSimSection() {
        return """
                <h3 style="margin: 28px 0 10px 0; font-size: 15px; color: %s;">Collect your SIM card</h3>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background: %s; border: 1px solid %s; border-radius: 10px;">
                <tr>
                <td style="padding: 18px 20px;">
                <p style="margin: 0 0 10px 0; font-size: 14px; color: %s;">Pick it up on arrival at the Vodafone counter, Tirana International Airport, Arrivals Hall.</p>
                <p style="margin: 0; font-size: 13px; color: %s;">Bring your passport and this order reference. Your pack activates as soon as the SIM is handed to you.</p>
                </td>
                </tr>
                </table>
                """.formatted(INK, CARD_BG, BORDER, INK, MUTED);
    }

    private String actionButtons(TouristWelcomeEmailContext ctx) {
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin-top: 28px;">
                <tr>
                <td style="padding-bottom: 8px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                <td width="50%%" style="padding-right: 6px;">
                <a href="%s" style="display: block; background-color: %s; color: #fff; padding: 14px 0; text-decoration: none; border-radius: 8px; font-weight: 700; font-size: 14px; text-align: center;">Open Game Hub</a>
                </td>
                <td width="50%%" style="padding-left: 6px;">
                <a href="%s" style="display: block; background-color: #fff; color: %s; border: 1px solid %s; padding: 13px 0; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 14px; text-align: center;">Check Your Data Usage</a>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                <tr>
                <td>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                <td width="50%%" style="padding-right: 6px;">
                <a href="%s" style="display: block; background-color: #000; color: #fff; padding: 11px 0; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 13px; text-align: center;">Add to Apple Wallet</a>
                </td>
                <td width="50%%" style="padding-left: 6px;">
                <a href="%s" style="display: block; background-color: #fff; color: %s; border: 1px solid %s; padding: 10px 0; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 13px; text-align: center;">Add to Google Wallet</a>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                </table>
                """.formatted(ctx.gameHubUrl(), RED, ctx.myPackUrl(), INK, BORDER,
                              ctx.appleWalletUrl(), ctx.googleWalletUrl(), INK, BORDER);
    }

    private String faqSection(TouristWelcomeEmailContext ctx) {
        return """
                <div style="margin-top: 32px; border-top: 1px solid %s; padding-top: 20px;">
                <h3 style="margin: 0 0 4px 0; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; color: %s;">Questions</h3>
                %s
                %s
                %s
                %s
                </div>
                """.formatted(
                        BORDER, MUTED,
                        faqItem("My eSIM QR code won't scan. What now?",
                                "Make sure Wi-Fi is on when you scan — eSIM activation needs a data connection the first time. If it still fails, use the manual activation code included with your QR instead."),
                        faqItem("Can I check my pack details and expiry date?",
                                "Yes — use the \"Check Your Data Usage\" button above. It shows your order reference, activation date, expiry date, and game hub credits anytime."),
                        faqItem("What happens when my pack runs out?",
                                "You can top up or buy a new pack from the same page you activated this one from. Your Game Hub credits and prize history carry over."),
                        faqItem("Who do I contact if something's wrong?",
                                "Reply to this email with order reference " + ctx.orderRef() + " and we'll follow up.")
                );
    }

    private String faqItem(String question, String answer) {
        return """
                <details style="border-bottom: 1px solid %s; padding: 12px 0;">
                <summary style="cursor: pointer; list-style: none; font-size: 14px; font-weight: 600; color: %s;">
                %s <span style="color: %s; float: right;">&#9662;</span>
                </summary>
                <p style="margin: 10px 0 0 0; font-size: 13px; color: %s; line-height: 1.5;">%s</p>
                </details>
                """.formatted(BORDER, INK, question, RED, MUTED, answer);
    }

    private String footer() {
        return """
                <div style="padding: 18px 28px; background: %s; border-top: 1px solid %s;">
                <p style="margin: 0; color: %s; font-size: 11px; text-align: center;">&copy; 2026 Vodafone Albania. All rights reserved.</p>
                </div>
                """.formatted(CARD_BG, BORDER, MUTED);
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}