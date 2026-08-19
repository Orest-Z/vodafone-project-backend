package al.vodafone.vodafone_project_backend.dto;

public record PartnerOfferDto(
    String partnerName,
    String logoUrl,
    String category,
    String discountLabel
) {}