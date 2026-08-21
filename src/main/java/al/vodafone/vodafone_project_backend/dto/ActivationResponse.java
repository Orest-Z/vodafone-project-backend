package al.vodafone.vodafone_project_backend.dto;

import al.vodafone.vodafone_project_backend.model.ActivationStatus;
import java.util.UUID;

public record ActivationResponse(
    UUID subscriptionId,
    UUID touristId,
    UUID transactionId,
    String orderRef,
    ActivationStatus status,
    String esimQrUrl
) {}