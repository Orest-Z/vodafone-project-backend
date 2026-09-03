// model/UserSubscription.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "user_subscriptions") @Getter @Setter
public class UserSubscription {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tourist_id", nullable = false) private Tourist tourist;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pack_id", nullable = false) private Pack pack;
    @Column(name = "order_ref", nullable = false, unique = true) private String orderRef;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "delivery_method", nullable = false) private DeliveryMethod deliveryMethod;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false) private ActivationStatus status = ActivationStatus.PENDING;

    @Column(name = "esim_qr_url") private String esimQrUrl;
    @Column(name = "esim_manual_code") private String esimManualCode;
    @Column(name = "passkit_member_id") private String passkitMemberId;
    @Column(name = "passkit_pass_url") private String passkitPassUrl;
    @Column(name = "activated_at") private Instant activatedAt;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}