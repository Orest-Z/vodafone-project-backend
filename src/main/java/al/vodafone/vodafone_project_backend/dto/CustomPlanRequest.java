package al.vodafone.vodafone_project_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CustomPlanRequest(
    @Min(1) @Max(50) Integer dataAllowanceGb,
    boolean unlimitedData,
    @NotNull @Min(0) @Max(1000) Integer minutesAllowance,
    @NotNull @Min(7) @Max(90) Integer durationDays
) {
    @AssertTrue(message = "dataAllowanceGb is required when unlimitedData is false")
    public boolean isDataSelectionValid() {
        return unlimitedData || dataAllowanceGb != null;
    }
}
