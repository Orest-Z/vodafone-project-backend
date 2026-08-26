package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "prizes")
@Getter
@Setter
public class Prize {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "partner_offer_id")
    private PartnerOffer partnerOffer;

    private String label;
    private String sponsorName;
    private String codePrefix;
    private Integer weight;
    private Boolean isActive;
}