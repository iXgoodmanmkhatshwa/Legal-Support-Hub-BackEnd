package za.co.ixengineers.legal.matter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.ixengineers.legal.document.DocumentStorage;
import za.co.ixengineers.legal.security.CurrentUser;
import java.io.*;
import java.time.*;
import java.util.*;
import static za.co.ixengineers.legal.matter.Domain.*;
import static za.co.ixengineers.legal.matter.MatterDtos.*;
@Service
@RequiredArgsConstructor public class MatterService{
 private final MatterRepository matters;
 private final DocumentRepository docs;
 private final FeedbackRepository feedback;
 private final AuditService audit;
 private final DocumentStorage storage;
 @Transactional public MatterResponse create(CreateMatterRequest q,CurrentUser u){
  validate(q);
  Matter m=new Matter();
  m.setReference(nextReference());
  m.setTitle(q.type()+" · "+Optional.ofNullable(q.counterparty()).filter(s->!s.isBlank()).orElse(q.subCategory()));
  m.setType(q.type());
  m.setSubCategory(q.subCategory());
  m.setStatus(MatterStatus.SUBMITTED);
  m.setUrgency(q.urgency());
  m.setRequesterId(u.id());
  m.setRequesterName(u.name());
  m.setRequestedService(q.requestedService());
  m.setBusinessUnit(q.businessUnit());
  m.setCounterparty(q.counterparty());
  m.setTriggerDate(q.triggerDate());
  m.setDueDate(q.deadlineDriver()!=null?q.deadlineDriver():calculateDue(q));
  m.setBackground(q.background());
  m.setCommercialRationale(q.commercialRationale());
  m.setReviewPerspective(q.reviewPerspective());
  m.setInvoiceNumber(q.invoiceNumber());
  m.setAmount(q.amount());
  m.setPaymentDueDate(q.paymentDueDate());
  m.setAgreementReference(q.agreementReference());
  m.setCourt(q.court());
  m.setInsuranceRelevant(q.insuranceRelevant());
  m.setRestrictedMatter(q.restrictedMatter());
  m=matters.save(m);
  audit.record(m.getId(),"MATTER_CREATED",u,"Submitted legal intake");
  return map(m);
 }
 @Transactional(readOnly=true)
 public Page<MatterResponse> list(CurrentUser u,Pageable p){
  boolean all=u.has("LEGAL_ADMIN")||
          u.has("LEGAL_USER")||
          u.has("HEAD_OF_LEGAL")||
          u.has("SENIOR_MANAGEMENT")||
          u.has("SYSTEM_ADMIN");
  return (
          all?matters.findAll(p): matters.findByRequesterId(u.id(),p)).map(this::map
  );
 }
 @Transactional public MatterResponse assign(String ref,AssignmentRequest q,CurrentUser u){Matter m=get(ref);m.setOwnerId(q.userId());m.setOwnerName(q.userName());m.setStatus(MatterStatus.ASSIGNED);audit.record(m.getId(),"MATTER_ASSIGNED",u,"Assigned to "+q.userName());return map(m);}
 @Transactional public MatterResponse status(String ref,StatusRequest q,CurrentUser u){Matter m=get(ref);if(isPaused(q.status())&&Optional.ofNullable(q.pendingReason()).orElse("").isBlank())throw new IllegalArgumentException("Pending reason is required for paused statuses");m.setStatus(q.status());m.setPendingReason(q.pendingReason());audit.record(m.getId(),"STATUS_CHANGED",u,q.status().name());return map(m);}
 @Transactional public DocumentResponse upload(String ref,DocumentCategory cat,MultipartFile file,CurrentUser u)throws IOException{if(file.isEmpty()||file.getSize()>25*1024*1024)throw new IllegalArgumentException("File must be non-empty and no larger than 25 MB");Matter m=get(ref);var stored=storage.store(ref,file);DocumentRecord d=new DocumentRecord();d.setMatter(m);d.setFileName(Optional.ofNullable(file.getOriginalFilename()).orElse("file"));d.setContentType(Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"));d.setSizeBytes(file.getSize());d.setCategory(cat);d.setExternalId(stored.externalId());d.setWebUrl(stored.webUrl());d.setUploadedBy(u.id());d=docs.save(d);audit.record(m.getId(),"DOCUMENT_UPLOADED",u,d.getFileName()+" ["+cat+"]");return map(d);}
 @Transactional public void addFeedback(String ref,FeedbackRequest q,CurrentUser u){Matter m=get(ref);if(!m.getRequesterId().equals(u.id())&&!u.has("HEAD_OF_LEGAL"))throw new AccessDeniedException("Only requester may submit feedback");Feedback f=new Feedback();f.setMatter(m);f.setRating(q.rating());f.setComment(q.comment());f.setSubmittedBy(u.id());feedback.save(f);audit.record(m.getId(),"FEEDBACK_SUBMITTED",u,"Rating "+q.rating());}
 private Matter get(String ref){return matters.findByReference(ref).orElseThrow(()->new NoSuchElementException("Matter not found: "+ref));}private String nextReference(){return "LEG-"+Year.now().getValue()+"-"+String.format("%06d",matters.count()+1);}private LocalDate calculateDue(CreateMatterRequest q){if(q.triggerDate()==null)return LocalDate.now().plusDays(10);String s=q.subCategory().toLowerCase();if(s.contains("summons"))return q.triggerDate().plusDays(10);if(s.contains("motion"))return q.triggerDate().plusDays(5);return q.triggerDate().plusDays(15);}private void validate(CreateMatterRequest q){if(q.urgency()!=Urgency.NORMAL&&Optional.ofNullable(q.urgencyReason()).orElse("").isBlank())throw new IllegalArgumentException("Urgency reason is required");if(q.type()==MatterType.CONTRACT_REVIEW&&(blank(q.commercialRationale())||blank(q.reviewPerspective())))throw new IllegalArgumentException("Commercial rationale and review perspective are required");}private boolean blank(String s){return s==null||s.isBlank();}private boolean isPaused(MatterStatus s){return s==MatterStatus.AWAITING_THIRD_PARTY||s==MatterStatus.AWAITING_PM_BUSINESS||s==MatterStatus.PENDING_EXTERNAL_OUTCOME||s==MatterStatus.AWAITING_REQUESTER_INFORMATION;}private MatterResponse map(Matter m){return new MatterResponse(m.getId(),m.getReference(),m.getTitle(),m.getType(),m.getSubCategory(),m.getStatus(),m.getUrgency(),m.getRequesterName(),m.getBusinessUnit(),m.getCounterparty(),m.getOwnerName(),m.getDueDate(),m.getPendingReason(),m.isRestrictedMatter(),m.getCreatedAt());}private DocumentResponse map(DocumentRecord d){return new DocumentResponse(d.getId(),d.getFileName(),d.getContentType(),d.getSizeBytes(),d.getCategory(),d.getWebUrl(),d.getUploadedAt());}}
