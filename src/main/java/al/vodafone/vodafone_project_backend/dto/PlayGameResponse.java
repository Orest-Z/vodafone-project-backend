package al.vodafone.vodafone_project_backend.dto;

public record PlayGameResponse(
    boolean won,
    PrizeDetails prize
) {
    public record PrizeDetails(
        String label,
        String sponsor,
        String code
    ) {}
}