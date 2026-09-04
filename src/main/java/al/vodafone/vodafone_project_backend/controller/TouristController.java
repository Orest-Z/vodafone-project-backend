package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.DiscountLookupResponse;
import al.vodafone.vodafone_project_backend.dto.SubscriptionStatusResponse;
import al.vodafone.vodafone_project_backend.service.ActivationService;
import al.vodafone.vodafone_project_backend.service.TouristService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tourists")
@RequiredArgsConstructor
@Validated
public class TouristController {

    private final ActivationService activationService;
    private final TouristService touristService;

    @GetMapping("/discount-by-email")
    public ResponseEntity<DiscountLookupResponse> getDiscountByEmail(
            @RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(activationService.getAvailableDiscountByEmail(email));
    }

    @GetMapping("/{touristId}/subscription")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(@PathVariable UUID touristId) {
        try {
            return ResponseEntity.ok(touristService.getSubscriptionStatus(touristId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
