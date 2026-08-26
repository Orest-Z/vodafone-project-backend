package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.DropType;
import al.vodafone.vodafone_project_backend.model.GamePlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface GamePlayRepository extends JpaRepository<GamePlay, UUID> {
    boolean existsByTouristIdAndGameIdAndPlayedDateAndDropType(
            UUID touristId, UUID gameId, LocalDate playedDate, DropType dropType);
}