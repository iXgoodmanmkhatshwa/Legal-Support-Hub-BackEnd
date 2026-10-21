package za.co.ixengineers.legal.matter;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
@Entity
@Table(name="lsh_audit_events")
@Getter @Setter
@NoArgsConstructor
public class AuditEvent{
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    private UUID matterId;
    @Column(nullable=false) private String action;
    @Column(nullable=false) private String actorId;
    private String actorName;
    @Column(columnDefinition="text") private String details;
    @Column(nullable=false,updatable=false) private Instant occurredAt;
    @PrePersist void create(){
        occurredAt=Instant.now();
    }
}
