package za.co.ixengineers.legal.contract;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lsh_contracts")
@Getter
@Setter
@NoArgsConstructor
public class ContractRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 300)
    private String counterparty;

    @Column(length = 200)
    private String contractType;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private boolean autoRenewal;

    private Integer noticePeriodDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ContractStatus status;

    @Column(nullable = false, length = 200)
    private String uploadedById;

    @Column(nullable = false, length = 200)
    private String uploadedByName;

    private Integer extractionConfidence;

    @Column(length = 4000)
    private String extractionNotes;

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void create() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void update() {
        updatedAt = Instant.now();
    }
}