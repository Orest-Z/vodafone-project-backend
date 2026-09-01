package al.vodafone.vodafone_project_backend.dto;

public record PrizeCatalogEntry(
    String label,
    String sponsor,
    Integer discountPercent,
    double chancePercent
) {}
