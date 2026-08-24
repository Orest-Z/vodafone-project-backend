package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.GameHubStateResponse;
import al.vodafone.vodafone_project_backend.dto.PlayGameRequest;
import al.vodafone.vodafone_project_backend.dto.PlayGameResponse;
import al.vodafone.vodafone_project_backend.model.*;
import al.vodafone.vodafone_project_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final TouristRepository touristRepository;
    private final GamePlayRepository gamePlayRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final DailyCreditClaimRepository dailyCreditClaimRepository;

    @Value("${app.game-hub.timezone:Europe/Tirane}")
    private String gameHubTimezone;

    private ZoneId zone() {
        return ZoneId.of(gameHubTimezone);
    }

    @Transactional(readOnly = true)
    public GameHubStateResponse getGameState(UUID touristId) {
        int credits = creditTransactionRepository.getBalanceByTouristId(touristId);
        List<String> playedGames = gamePlayRepository.findPlayedGameCodesByTouristId(touristId);

        LocalDate today = LocalDate.now(zone());
        boolean claimedToday = dailyCreditClaimRepository.existsByTouristIdAndClaimDate(touristId, today);
        Instant nextClaimAt = claimedToday
                ? today.plusDays(1).atStartOfDay(zone()).toInstant()
                : null;

        return new GameHubStateResponse(credits, playedGames, !claimedToday, nextClaimAt);
    }

    @Transactional
    public GameHubStateResponse claimDailyCredit(UUID touristId) {
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new IllegalArgumentException("Tourist not found: " + touristId));

        LocalDate today = LocalDate.now(zone());

        if (dailyCreditClaimRepository.existsByTouristIdAndClaimDate(touristId, today)) {
            throw new IllegalStateException("Daily game credit already claimed for today.");
        }

        DailyCreditClaim claim = new DailyCreditClaim();
        claim.setTourist(tourist);
        claim.setClaimDate(today);
        dailyCreditClaimRepository.saveAndFlush(claim);

        CreditTransaction credit = new CreditTransaction();
        credit.setTourist(tourist);
        credit.setDelta(1);
        credit.setReason(CreditReason.DAILY_CREDIT_GRANT);
        creditTransactionRepository.save(credit);

        return getGameState(touristId);
    }

    @Transactional
    public PlayGameResponse playGame(String gameCode, PlayGameRequest req, String ipAddress, String userAgent) {
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

        CreditTransaction spendTx = new CreditTransaction();
        spendTx.setTourist(tourist);
        spendTx.setDelta(-1);
        spendTx.setReason(CreditReason.GAME_SPEND);
        creditTransactionRepository.save(spendTx);

        String prizeCode = "VF-REWARD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        GamePlay play = new GamePlay();
        play.setTourist(tourist);
        play.setGame(game);
        play.setWon(true);
        play.setPrizeCode(prizeCode);
        play.setIpAddress(ipAddress);
        play.setUserAgent(userAgent);

        gamePlayRepository.saveAndFlush(play);

        return new PlayGameResponse(
                true,
                new PlayGameResponse.PrizeDetails("15% Off Local Restaurants", "OPA!", prizeCode)
        );
    }
}