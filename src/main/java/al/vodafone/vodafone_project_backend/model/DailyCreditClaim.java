// model/DailyCreditClaim.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One row per tourist per calendar day they claimed their free daily game
 * credit. The unique constraint on (tourist_id, claim_date) is the actual
 * enforcement of "1 credit per 24h" — it is a DB-level guard, not just an
 * application-level check, so it holds even under concurrent requests
 * (double-tap, two tabs, retried request, etc.).
 */
@Entity
@Table(
        name = "daily_credit_claims",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_credit_claim_tourist_date",
                columnNames = {"tourist_id", "claim_date"}
        )
)
@Getter @Setter
public class DailyCreditClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    // Calendar day (in the configured game-hub timezone) this claim covers.
    @Column(name = "claim_date", nullable = false)
    private LocalDate claimDate;

    @CreationTimestamp
    @Column(name = "claimed_at", updatable = false)
    private Instant claimedAt;
}