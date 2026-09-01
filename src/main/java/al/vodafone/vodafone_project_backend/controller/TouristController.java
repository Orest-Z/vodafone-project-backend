package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.DiscountLookupResponse;
import al.vodafone.vodafone_project_backend.service.ActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tourists")
@RequiredArgsConstructor
public class TouristController {

    private final ActivationService activationService;

    @GetMapping("/discount-by-email")
    public ResponseEntity<DiscountLookupResponse> getDiscountByEmail(@RequestParam String email) {
        return ResponseEntity.ok(activationService.getAvailableDiscountByEmail(email));
    }
}