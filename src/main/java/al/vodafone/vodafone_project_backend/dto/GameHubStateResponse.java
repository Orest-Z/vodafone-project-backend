package al.vodafone.vodafone_project_backend.dto;

import java.util.List;

public record GameHubStateResponse(
    int credits,
    List<String> playedGames
) {}