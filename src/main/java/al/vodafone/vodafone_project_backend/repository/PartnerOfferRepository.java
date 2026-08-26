package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.PartnerOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface PartnerOfferRepository extends JpaRepository<PartnerOffer, UUID> {
    @Query("SELECT po FROM PartnerOffer po JOIN FETCH po.partner WHERE po.isActive = true")
    List<PartnerOffer> findAllActiveWithPartner();
}