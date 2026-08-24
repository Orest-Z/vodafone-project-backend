// model/GamePlay.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Server-side execution log for a single game play. This row is the source
 * of truth for whether a tourist played a given game and what they won —
 * the frontend never sends a score or a result, it only ever asks to play
 * and gets told the outcome. ipAddress/userAgent are captured for abuse
 * review, and the unique constraint below stops two concurrent requests
 * (e.g. a double-tap or a retried request) from both squeezing through the
 * "already played?" check in GameService and creating two plays (and
 * spending two credits) for the same tourist+game.
 */
@Entity
@Table(
        name = "game_plays",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_play_tourist_game",
                columnNames = {"tourist_id", "game_id"}
        )
)
@Getter @Setter
public class GamePlay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    private boolean won;

    @Column(name = "prize_code")
    private String prizeCode;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "played_at", updatable = false)
    private Instant playedAt;
}