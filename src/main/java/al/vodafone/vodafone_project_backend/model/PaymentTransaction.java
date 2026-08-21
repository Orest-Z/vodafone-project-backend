// model/PaymentTransaction.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "payment_transactions") @Getter @Setter
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "subscription_id", nullable = false)
    private UserSubscription subscription;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    // The PayPal order that was created (from /v2/checkout/orders)
    @Column(name = "paypal_order_id", nullable = false, unique = true)
    private String paypalOrderId;

    // The PayPal capture id returned once the order is actually captured
    // (from /v2/checkout/orders/{id}/capture) — this is the true proof of
    // payment, distinct from the order id.
    @Column(name = "paypal_capture_id", unique = true)
    private String paypalCaptureId;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}