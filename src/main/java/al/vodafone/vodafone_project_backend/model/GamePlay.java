package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "game_plays",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_play_tourist_game_date_type",
                columnNames = {"tourist_id", "game_id", "played_date", "drop_type"}
        )
)
@Getter @Setter
public class GamePlay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "prize_id")
         private Prize prize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "played_date", nullable = false)
    private LocalDate playedDate;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "drop_type", nullable = false)
    private DropType dropType;

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