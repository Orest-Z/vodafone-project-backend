package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.GamePlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GamePlayRepository extends JpaRepository<GamePlay, UUID> {
    boolean existsByTouristIdAndGameId(UUID touristId, UUID gameId);

    // This query extracts just the string codes from the user's played games
    @Query("SELECT gp.game.code FROM GamePlay gp WHERE gp.tourist.id = :touristId")
    List<String> findPlayedGameCodesByTouristId(@Param("touristId") UUID touristId);
}