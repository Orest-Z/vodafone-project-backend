// model/Partner.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name = "partners") @Getter @Setter
public class Partner {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private String name;
    @Column(name = "logo_url") private String logoUrl;
    private String category;
    @Column(name = "is_active") private boolean isActive = true;
}