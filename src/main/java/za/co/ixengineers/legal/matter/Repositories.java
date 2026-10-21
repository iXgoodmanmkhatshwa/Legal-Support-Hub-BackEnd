package za.co.ixengineers.legal.matter;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
interface MatterRepository extends JpaRepository<Matter,UUID>{
    Optional<Matter> findByReference(String reference);
    Page<Matter> findByRequesterId(String requesterId,Pageable pageable);
}
interface DocumentRepository extends JpaRepository<DocumentRecord,UUID>{
    List<DocumentRecord> findByMatterIdOrderByUploadedAtDesc(UUID matterId);
}
interface AuditRepository extends JpaRepository<AuditEvent,UUID>{
    List<AuditEvent> findByMatterIdOrderByOccurredAtDesc(UUID matterId);
}
interface FeedbackRepository extends JpaRepository<Feedback,UUID>{}
