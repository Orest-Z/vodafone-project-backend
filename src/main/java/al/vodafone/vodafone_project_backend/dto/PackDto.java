package al.vodafone.vodafone_project_backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PackDto(
    UUID id,
    String title,
    String subtitle,
    BigDecimal priceAll,
    Integer durationDays,
    String dataAllowance,
    Integer minutesAllowance,
    String imageUrl,
    List<PackFeatureDto> features
) {}