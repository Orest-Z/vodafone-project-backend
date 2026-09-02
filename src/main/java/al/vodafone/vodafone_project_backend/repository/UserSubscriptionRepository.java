package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    Optional<UserSubscription> findByOrderRef(String orderRef);
    Optional<UserSubscription> findFirstByTouristIdOrderByCreatedAtDesc(UUID touristId);
}