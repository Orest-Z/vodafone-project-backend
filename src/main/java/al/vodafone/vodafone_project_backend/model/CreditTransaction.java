// model/CreditTransaction.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "credit_transactions") @Getter @Setter
public class CreditTransaction {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tourist_id", nullable = false) private Tourist tourist;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "subscription_id") private UserSubscription subscription;
    @Column(nullable = false) private int delta;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false) private CreditReason reason;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}