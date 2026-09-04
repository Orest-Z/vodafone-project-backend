package al.vodafone.vodafone_project_backend.dto;

import java.math.BigDecimal;

public record CustomPlanQuoteResponse(
    BigDecimal priceAll,
    String dataAllowanceLabel,
    String minutesLabel,
    String durationLabel,
    Integer durationDays
) {}
