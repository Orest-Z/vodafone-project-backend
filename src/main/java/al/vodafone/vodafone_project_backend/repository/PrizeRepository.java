package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface PrizeRepository extends JpaRepository<Prize, UUID> {
    @Query("SELECT p FROM Prize p WHERE p.isActive = true")
    List<Prize> findAllActive();
}