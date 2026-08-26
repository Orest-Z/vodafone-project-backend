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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final String DAILY_DROP_GAME_CODE = "daily_drop";

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

    private Game dailyDropGame() {
        return gameRepository.findByCode(DAILY_DROP_GAME_CODE)
                .orElseThrow(() -> new IllegalStateException("daily_drop game is not seeded"));
    }

    @Transactional(readOnly = true)
    public GameHubStateResponse getGameState(UUID touristId) {
        int credits = creditTransactionRepository.getBalanceByTouristId(touristId);
        LocalDate today = LocalDate.now(zone());

        boolean hasPlayedToday = gamePlayRepository.existsByTouristIdAndGameIdAndPlayedDateAndDropType(
                touristId, dailyDropGame().getId(), today, DropType.FREE);

        boolean claimedToday = dailyCreditClaimRepository.existsByTouristIdAndClaimDate(touristId, today);
        Instant nextClaimAt = claimedToday
                ? today.plusDays(1).atStartOfDay(zone()).toInstant()
                : null;

        return new GameHubStateResponse(credits, hasPlayedToday, !claimedToday, nextClaimAt);
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
    public PlayGameResponse playDrop(PlayGameRequest req, String ipAddress, String userAgent) {
        return play(req.touristId(), DropType.FREE, ipAddress, userAgent);
    }

    @Transactional
    public PlayGameResponse playPaidRedrop(PlayGameRequest req, String ipAddress, String userAgent) {
        return play(req.touristId(), DropType.PAID_REDROP, ipAddress, userAgent);
    }

    private PlayGameResponse play(UUID touristId, DropType dropType, String ipAddress, String userAgent) {
        Game game = dailyDropGame();

        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new IllegalArgumentException("Tourist not found: " + touristId));

        LocalDate today = LocalDate.now(zone());

        if (gamePlayRepository.existsByTouristIdAndGameIdAndPlayedDateAndDropType(
                touristId, game.getId(), today, dropType)) {
            throw new IllegalStateException("Already played today.");
        }

        if (dropType == DropType.FREE) {
            int currentCredits = creditTransactionRepository.getBalanceByTouristId(touristId);
            if (currentCredits <= 0) {
                throw new IllegalStateException("Insufficient game credits.");
            }

            CreditTransaction spendTx = new CreditTransaction();
            spendTx.setTourist(tourist);
            spendTx.setDelta(-1);
            spendTx.setReason(CreditReason.GAME_SPEND);
            creditTransactionRepository.save(spendTx);
        }

        String prizeCode = "VF-REWARD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        GamePlay play = new GamePlay();
        play.setTourist(tourist);
        play.setGame(game);
        play.setPlayedDate(today);
        play.setDropType(dropType);
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