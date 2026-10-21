package za.co.ixengineers.legal.contract;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lsh_contract_documents")
@Getter
@Setter
@NoArgsConstructor
public class ContractDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractRecord contract;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 200)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private boolean primaryDocument;

    @Column(nullable = false, length = 500)
    private String externalId;

    @Column(length = 2000)
    private String webUrl;

    @Column(nullable = false, length = 200)
    private String uploadedBy;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    void create() {
        uploadedAt = Instant.now();
    }
}