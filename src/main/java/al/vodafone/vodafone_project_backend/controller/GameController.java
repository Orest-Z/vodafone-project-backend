package al.vodafone.vodafone_project_backend.controller;

import al.vodafone.vodafone_project_backend.dto.GameHubStateResponse;
import al.vodafone.vodafone_project_backend.dto.PlayGameRequest;
import al.vodafone.vodafone_project_backend.dto.PlayGameResponse;
import al.vodafone.vodafone_project_backend.service.GameService;
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

    @PostMapping("/games/{gameCode}/play")
    public ResponseEntity<PlayGameResponse> playGame(
            @PathVariable String gameCode,
            @Valid @RequestBody PlayGameRequest req) {
        return ResponseEntity.ok(gameService.playGame(gameCode, req));
    }
}