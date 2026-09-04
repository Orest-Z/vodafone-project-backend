package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.DropType;
import al.vodafone.vodafone_project_backend.model.GamePlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GamePlayRepository extends JpaRepository<GamePlay, UUID> {
    boolean existsByTouristIdAndGameIdAndPlayedDateAndDropType(
            UUID touristId, UUID gameId, LocalDate playedDate, DropType dropType);

    // Native query + explicit ::text cast: comparing the prize_type enum
    // column via JPQL (`gp.prize.prizeType = PrizeType.PACK_DISCOUNT`) makes
    // Hibernate cast the literal to a Postgres type named after the Java
    // enum class ("PrizeType" -> prizetype), which is a different type than
    // the prizes.prize_type column's actual native enum type in the DB —
    // Postgres then rejects the comparison with "operator does not exist".
    // Casting both sides to text sidesteps the type-name mismatch entirely.
    @Query(value = """
        SELECT gp.* FROM game_plays gp
        JOIN prizes pz ON pz.id = gp.prize_id
        WHERE gp.tourist_id = :touristId
        AND pz.prize_type::text = 'PACK_DISCOUNT'
        AND gp.redeemed_at IS NULL
        ORDER BY gp.played_at DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<GamePlay> findFirstUnredeemedPackDiscount(UUID touristId);
}