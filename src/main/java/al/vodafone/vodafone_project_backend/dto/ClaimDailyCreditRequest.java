package al.vodafone.vodafone_project_backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ClaimDailyCreditRequest(
    @NotNull UUID touristId
) {}