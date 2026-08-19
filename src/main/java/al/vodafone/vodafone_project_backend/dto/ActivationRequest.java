package al.vodafone.vodafone_project_backend.dto;

import al.vodafone.vodafone_project_backend.model.DeliveryMethod;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ActivationRequest(
    @NotNull UUID packId,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email @NotBlank String email,
    @NotBlank String passportNumber,
    @NotNull DeliveryMethod deliveryMethod,
    @AssertTrue boolean termsAccepted
) {}