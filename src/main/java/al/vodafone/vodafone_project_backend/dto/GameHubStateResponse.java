package al.vodafone.vodafone_project_backend.dto;

import java.time.Instant;

public record GameHubStateResponse(
    String touristFirstName,
    int credits,
    boolean hasPlayedToday,
    boolean dailyClaimAvailable,
    Instant nextClaimAt
) {}