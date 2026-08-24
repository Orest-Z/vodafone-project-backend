package al.vodafone.vodafone_project_backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Resend resend;

    @Value("${resend.sender-email}")
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

        // CHANGED: Using CreateEmailOptions instead of SendEmailRequest
        CreateEmailOptions sendEmailOptions = CreateEmailOptions.builder()
                .from(senderEmail)
                .to(toEmail)
                .subject("Your Vodafone Tourist Pass is Ready")
                .html(htmlBody)
                .build();

        try {
            // CHANGED: Using CreateEmailResponse instead of SendEmailResponse
            CreateEmailResponse data = resend.emails().send(sendEmailOptions);
            log.info("Welcome email sent successfully to {}. Message ID: {}", toEmail, data.getId());
        } catch (ResendException e) {
            log.error("Failed to send welcome email to {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
}