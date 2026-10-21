package za.co.ixengineers.legal.matter;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
import static za.co.ixengineers.legal.matter.Domain.*;
@Entity
@Table(name="lsh_documents")
@Getter @Setter
@NoArgsConstructor
public class DocumentRecord{
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) private Matter matter;
    @Column(nullable=false) private String fileName;
    @Column(nullable=false) private String contentType;
    @Column(nullable=false) private long sizeBytes;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private DocumentCategory category;
    private String externalId;private String webUrl;
    @Column(nullable=false) private String uploadedBy;
    @Column(nullable=false,updatable=false) private Instant uploadedAt;
    @PrePersist void create(){
        uploadedAt=Instant.now();
    }
}
