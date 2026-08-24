package al.vodafone.vodafone_project_backend.dto;

import java.time.Instant;
import java.util.List;

public record GameHubStateResponse(
    int credits,
    List<String> playedGames,
    // true if the tourist has not yet claimed today's free daily credit
    boolean dailyClaimAvailable,
    // when the next claim opens up (start of next calendar day, game-hub
    // timezone) — null when dailyClaimAvailable is true, i.e. claimable now
    Instant nextClaimAt
) {}