package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
    @Query("SELECT COALESCE(SUM(c.delta), 0) FROM CreditTransaction c WHERE c.tourist.id = :touristId")
    int getBalanceByTouristId(@Param("touristId") UUID touristId);
}