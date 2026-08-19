// model/Game.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name = "games") @Getter @Setter
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true) private String code;
    private String title;
    private String tagline;
    @Column(name = "play_label") private String playLabel;
    @Column(name = "is_active") private boolean isActive = true;
}