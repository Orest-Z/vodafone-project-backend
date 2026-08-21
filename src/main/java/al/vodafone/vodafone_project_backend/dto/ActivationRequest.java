package al.vodafone.vodafone_project_backend.dto;

import al.vodafone.vodafone_project_backend.model.DeliveryMethod;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ActivationRequest(
    @NotNull UUID packId,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email @NotBlank String email,
    @NotBlank String passportNumber,
    @NotNull DeliveryMethod deliveryMethod,
    @AssertTrue boolean termsAccepted,

    // Proof of payment — this endpoint represents a *paid* activation, so
    // these are required. The order id comes back from create-order, the
    // capture id/amount only exist once PayPal capture has actually
    // succeeded server-side (see capture-order route on the frontend).
    @NotBlank String paypalOrderId,
    @NotBlank String paypalCaptureId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amountPaid,
    @NotBlank String currency
) {}