package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.model.DeliveryMethod;

public record TouristWelcomeEmailContext(
    String touristFirstName,
    String orderRef,
    String packTitle,
    String dataAllowance,
    Integer minutesAllowance,
    Integer durationDays,
    DeliveryMethod deliveryMethod,
    String esimQrUrl,
    String esimManualCode,
    String esimActivationCode,
    String esimPhoneNumber,
    String gameHubUrl,
    String appleWalletUrl,
    String googleWalletUrl,
    String myPackUrl
) {}