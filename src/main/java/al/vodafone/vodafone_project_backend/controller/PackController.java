package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.PackDto;
import al.vodafone.vodafone_project_backend.dto.SponsorOfferResponse;
import al.vodafone.vodafone_project_backend.service.PackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/packs")
@RequiredArgsConstructor
public class PackController {

    private final PackService packService;

    @GetMapping
    public ResponseEntity<List<PackDto>> getPacks() {
        return ResponseEntity.ok(packService.getAllActivePacks());
    }

    @GetMapping("/{packId}")
    public ResponseEntity<PackDto> getPack(@PathVariable UUID packId) {
        return ResponseEntity.ok(packService.getPackById(packId));
    }
        @GetMapping("/sponsors")
    public ResponseEntity<List<SponsorOfferResponse>> getSponsorOffers() {
        return ResponseEntity.ok(packService.getActiveSponsorOffers());
        }
}