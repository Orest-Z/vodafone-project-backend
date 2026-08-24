package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.DailyCreditClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface DailyCreditClaimRepository extends JpaRepository<DailyCreditClaim, UUID> {
    boolean existsByTouristIdAndClaimDate(UUID touristId, LocalDate claimDate);
}