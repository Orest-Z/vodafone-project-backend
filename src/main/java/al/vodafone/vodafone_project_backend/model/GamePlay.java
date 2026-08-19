// model/GamePlay.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "game_plays") @Getter @Setter 
public class GamePlay {
    
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tourist_id", nullable = false) private Tourist tourist;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_id", nullable = false) private Game game;
    private boolean won;
    @Column(name = "prize_code") private String prizeCode;
    @Column(name = "redeemed_at") private Instant redeemedAt;
    @CreationTimestamp @Column(name = "played_at", updatable = false) private Instant playedAt;
}