package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, UUID> {
    Optional<Tourist> findByEmail(String email);
}