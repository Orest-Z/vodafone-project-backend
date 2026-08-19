package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.Pack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PackRepository extends JpaRepository<Pack, UUID> {
    List<Pack> findAllByIsActiveTrueOrderBySortOrderAsc();
}