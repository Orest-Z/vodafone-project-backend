// model/PackFeature.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name = "pack_features") @Getter @Setter
public class PackFeature {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pack_id", nullable = false) private Pack pack;
    private String label;
    @Column(name = "icon_key") private String iconKey;
    @Column(name = "sort_order") private int sortOrder;
}