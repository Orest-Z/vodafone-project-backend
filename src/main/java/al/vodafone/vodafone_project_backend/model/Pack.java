// model/Pack.java
package al.vodafone.vodafone_project_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "packs") @Getter @Setter
public class Pack {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private String title;
    private String subtitle;
    @Column(name = "price_all", precision = 10, scale = 2) private BigDecimal priceAll;
    @Column(name = "duration_days") private Integer durationDays;
    @Column(name = "data_allowance") private String dataAllowance;
    @Column(name = "minutes_allowance") private Integer minutesAllowance;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roaming_details", columnDefinition = "jsonb") private String roamingDetails;
    @Column(name = "image_url") private String imageUrl;
    @Column(name = "is_active") private boolean isActive = true;
    // Boolean (not primitive) on purpose: this column was added after real
    // pack rows already existed (ddl-auto=update only ever ADDs columns, it
    // never backfills existing rows), so pre-existing rows read back NULL
    // here — a primitive boolean can't hold that and Hibernate throws on
    // every SELECT. Treat null as "not custom" wherever this is read.
    @Column(name = "is_custom") private Boolean isCustom = false;
    @Column(name = "sort_order") private int sortOrder;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;

    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PackFeature> features = new ArrayList<>();
}