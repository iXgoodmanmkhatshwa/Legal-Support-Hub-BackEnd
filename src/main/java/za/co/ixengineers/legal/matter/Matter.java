package za.co.ixengineers.legal.matter;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.*;import java.util.*;import static za.co.ixengineers.legal.matter.Domain.*;
@Entity @Table(name="lsh_matters") @Getter @Setter @NoArgsConstructor
public class Matter{
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,unique=true,length=30) private String reference;
    @Column(nullable=false) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private MatterType type;
    @Column(nullable=false) private String subCategory;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private MatterStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Urgency urgency;
    @Column(nullable=false) private String requesterId;private String requesterName;
    @Column(nullable=false) private String businessUnit;private String counterparty;
    @Column(length=6000) private String requestedService;
    @Column(length=12000) private String background;
    private LocalDate triggerDate;
    private LocalDate dueDate;private String ownerId;
    private String ownerName;
    private String pendingReason;
    private boolean restrictedMatter;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate paymentDueDate;
    private String agreementReference;
    private String court;
    private boolean insuranceRelevant;
    private String commercialRationale;
    private String reviewPerspective;
    @Version private long version;
    @Column(nullable=false,updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist void create(){
        createdAt=updatedAt=Instant.now();
    }
    @PreUpdate void update(){
        updatedAt=Instant.now();
    }
}
