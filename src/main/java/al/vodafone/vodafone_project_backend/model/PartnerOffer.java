package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "partner_offers")
@Getter
@Setter
public class PartnerOffer {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    private String discountLabel;
    private Boolean isActive;
    private Integer sortOrder;
}