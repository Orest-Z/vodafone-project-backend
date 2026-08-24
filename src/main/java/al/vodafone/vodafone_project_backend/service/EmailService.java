package al.vodafone.vodafone_project_backend.service;

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

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendTouristWelcomeEmail(String toEmail, String touristName, String packageName, String magicToken) {
        
        // Vodafone branded enterprise HTML template
        String htmlBody = "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);\">" +
                "<div style=\"background-color: #E60000; padding: 30px; text-align: center; color: white;\">" +
                "<h1 style=\"margin: 0; font-size: 28px;\">vodafone</h1>" +
                "<p style=\"margin: 5px 0 0 0; text-transform: uppercase; font-size: 14px;\">Albania Tourist Pass</p>" +
                "</div>" +
                "<div style=\"padding: 40px 30px;\">" +
                "<h2 style=\"margin-top: 0; color: #333;\">Mirësevini në Shqipëri, " + touristName + "! 🇦🇱</h2>" +
                "<p style=\"color: #555; font-size: 16px;\">Your <strong>" + packageName + "</strong> is active and ready to use.</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"https://touristpass.al/dashboard?token=" + magicToken + "\" style=\"background-color: #333; color: #fff; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;\">Open Live Dashboard & Claim Rewards</a>" +
                "</div>" +
                "<p style=\"color: #999; font-size: 12px; text-align: center; margin-top: 40px;\">© 2026 Vodafone Albania. All rights reserved.</p>" +
                "</div></div></div>";

        try {
            // Create a MimeMessage to support HTML content
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Vodafone Tourist Pass is Ready");
            
            // The 'true' boolean flag indicates that the text is HTML
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully via Gmail SMTP to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
}