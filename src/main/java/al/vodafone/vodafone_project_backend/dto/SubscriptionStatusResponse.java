package al.vodafone.vodafone_project_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionStatusResponse(
    UUID subscriptionId,
    String touristFirstName,
    String touristLastName,
    String orderRef,
    String packTitle,
    String packSubtitle,
    String dataAllowance,
    Integer minutesAllowance,
    Integer durationDays,
    String deliveryMethod,
    String status,
    Instant activatedAt,
    Instant expiresAt,
    BigDecimal amountPaid,
    String currency,
    int gameCredits
) {}
