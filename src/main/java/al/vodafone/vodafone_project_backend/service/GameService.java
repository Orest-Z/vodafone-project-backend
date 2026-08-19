package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.GameHubStateResponse;
import al.vodafone.vodafone_project_backend.dto.PlayGameRequest;
import al.vodafone.vodafone_project_backend.dto.PlayGameResponse;
import al.vodafone.vodafone_project_backend.model.*;
import al.vodafone.vodafone_project_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final TouristRepository touristRepository;
    private final GamePlayRepository gamePlayRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    @Transactional(readOnly = true)
    public GameHubStateResponse getGameState(UUID touristId) {
        int credits = creditTransactionRepository.getBalanceByTouristId(touristId);
        List<String> playedGames = gamePlayRepository.findPlayedGameCodesByTouristId(touristId);
        return new GameHubStateResponse(credits, playedGames);
    }

    @Transactional
    public PlayGameResponse playGame(String gameCode, PlayGameRequest req) {
        Game game = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameCode));

        Tourist tourist = touristRepository.findById(req.touristId())
                .orElseThrow(() -> new IllegalArgumentException("Tourist not found: " + req.touristId()));

        if (gamePlayRepository.existsByTouristIdAndGameId(tourist.getId(), game.getId())) {
            throw new IllegalStateException("Game already played by this tourist.");
        }

        int currentCredits = creditTransactionRepository.getBalanceByTouristId(tourist.getId());
        if (currentCredits <= 0) {
            throw new IllegalStateException("Insufficient game credits.");
        }

        // Spend credit (-1)
        CreditTransaction spendTx = new CreditTransaction();
        spendTx.setTourist(tourist);
        spendTx.setDelta(-1);
        spendTx.setReason(CreditReason.GAME_SPEND);
        creditTransactionRepository.save(spendTx);

        // Generate winning reward code
        String prizeCode = "VF-REWARD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        GamePlay play = new GamePlay();
        play.setTourist(tourist);
        play.setGame(game);
        play.setWon(true);
        play.setPrizeCode(prizeCode);
        gamePlayRepository.save(play);

        return new PlayGameResponse(
                true,
                new PlayGameResponse.PrizeDetails("15% Off Local Restaurants", "OPA!", prizeCode)
        );
    }
}