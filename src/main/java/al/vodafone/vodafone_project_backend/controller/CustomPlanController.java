package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.CustomPlanQuoteResponse;
import al.vodafone.vodafone_project_backend.dto.CustomPlanRequest;
import al.vodafone.vodafone_project_backend.dto.PackDto;
import al.vodafone.vodafone_project_backend.service.CustomPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/packs/custom")
@RequiredArgsConstructor
public class CustomPlanController {

    private final CustomPlanService customPlanService;

    @PostMapping("/quote")
    public ResponseEntity<CustomPlanQuoteResponse> quote(@Valid @RequestBody CustomPlanRequest req) {
        return ResponseEntity.ok(customPlanService.quote(req));
    }

    @PostMapping("/build")
    public ResponseEntity<PackDto> build(@Valid @RequestBody CustomPlanRequest req) {
        return ResponseEntity.ok(customPlanService.build(req));
    }
}
