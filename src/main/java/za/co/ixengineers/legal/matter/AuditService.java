package za.co.ixengineers.legal.matter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.ixengineers.legal.security.CurrentUser;
import java.util.*;
@Service
@RequiredArgsConstructor
public class AuditService{
    private final AuditRepository repo;
    public void record(UUID matterId,String action,CurrentUser u,String details){
        AuditEvent e=new AuditEvent();
        e.setMatterId(matterId);
        e.setAction(action);
        e.setActorId(u.id());
        e.setActorName(u.name());
        e.setDetails(details);
        repo.save(e);}
    public List<AuditEvent> history(UUID matterId){
        return repo.findByMatterIdOrderByOccurredAtDesc(matterId);
    }
}
