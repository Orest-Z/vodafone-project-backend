package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.ClaimDailyCreditRequest;
import al.vodafone.vodafone_project_backend.dto.GameHubStateResponse;
import al.vodafone.vodafone_project_backend.dto.PlayGameRequest;
import al.vodafone.vodafone_project_backend.dto.PlayGameResponse;
import al.vodafone.vodafone_project_backend.service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/game-hub/state")
    public ResponseEntity<GameHubStateResponse> getGameState(@RequestParam UUID touristId) {
        return ResponseEntity.ok(gameService.getGameState(touristId));
    }

    @PostMapping("/game-hub/claim-daily-credit")
    public ResponseEntity<GameHubStateResponse> claimDailyCredit(
            @Valid @RequestBody ClaimDailyCreditRequest req) {
        return ResponseEntity.ok(gameService.claimDailyCredit(req.touristId()));
    }

    @PostMapping("/drop/play")
    public ResponseEntity<PlayGameResponse> playDrop(
            @Valid @RequestBody PlayGameRequest req,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(gameService.playDrop(
                req, clientIp(httpRequest), httpRequest.getHeader("User-Agent")
        ));
    }

    @PostMapping("/drop/redrop/play")
    public ResponseEntity<PlayGameResponse> playPaidRedrop(
            @Valid @RequestBody PlayGameRequest req,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(gameService.playPaidRedrop(
                req, clientIp(httpRequest), httpRequest.getHeader("User-Agent")
        ));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}