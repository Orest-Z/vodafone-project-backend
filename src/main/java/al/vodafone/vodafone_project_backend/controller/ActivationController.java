package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.ActivationRequest;
import al.vodafone.vodafone_project_backend.dto.ActivationResponse;
import al.vodafone.vodafone_project_backend.service.ActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activations")
@RequiredArgsConstructor
public class ActivationController {

    private final ActivationService activationService;

    @PostMapping
    public ResponseEntity<ActivationResponse> activate(@Valid @RequestBody ActivationRequest req) {
        return ResponseEntity.ok(activationService.activate(req));
    }
}