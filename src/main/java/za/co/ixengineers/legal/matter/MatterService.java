package za.co.ixengineers.legal.matter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.ixengineers.legal.admin.AdminConfigurationService;
import za.co.ixengineers.legal.document.DocumentStorage;
import za.co.ixengineers.legal.security.CurrentUser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static za.co.ixengineers.legal.matter.Domain.*;
import static za.co.ixengineers.legal.matter.MatterDtos.*;

@Service
@RequiredArgsConstructor
public class MatterService {

 private final MatterRepository matters;
 private final DocumentRepository docs;
 private final FeedbackRepository feedback;
 private final AuditService audit;
 private final DocumentStorage storage;
 private final AdminConfigurationService configuration;

 @Transactional
 public MatterResponse create(
         CreateMatterRequest request,
         CurrentUser user
 ) {
  validate(request);

  Matter matter = new Matter();

  matter.setReference(nextReference());

  matter.setTitle(
          request.type() +
                  " · " +
                  Optional.ofNullable(request.counterparty())
                          .filter(s -> !s.isBlank())
                          .orElse(request.subCategory())
  );

  matter.setType(request.type());
  matter.setSubCategory(request.subCategory());
  matter.setStatus(MatterStatus.SUBMITTED);
  matter.setUrgency(request.urgency());

  matter.setRequesterId(user.id());
  matter.setRequesterName(user.name());

  matter.setRequestedService(
          request.requestedService()
  );

  matter.setBusinessUnit(
          request.businessUnit()
  );

  matter.setCounterparty(
          request.counterparty()
  );

  matter.setTriggerDate(
          request.triggerDate()
  );

  matter.setDueDate(
          request.deadlineDriver() != null
                  ? request.deadlineDriver()
                  : calculateDue(request)
  );

  matter.setBackground(
          request.background()
  );

  matter.setCommercialRationale(
          request.commercialRationale()
  );

  matter.setReviewPerspective(
          request.reviewPerspective()
  );

  matter.setInvoiceNumber(
          request.invoiceNumber()
  );

  matter.setAmount(
          request.amount()
  );

  matter.setPaymentDueDate(
          request.paymentDueDate()
  );

  matter.setAgreementReference(
          request.agreementReference()
  );

  matter.setCourt(
          request.court()
  );

  matter.setInsuranceRelevant(
          request.insuranceRelevant()
  );

  matter.setRestrictedMatter(
          request.restrictedMatter()
  );

  matter = matters.save(matter);

  audit.record(
          matter.getId(),
          "MATTER_CREATED",
          user,
          "Submitted legal intake"
  );

  return map(matter);
 }

 @Transactional(readOnly = true)
 public Page<MatterResponse> list(
         CurrentUser user,
         Pageable pageable
 ) {
  boolean all =
          user.has("LEGAL_ADMIN")
                  || user.has("LEGAL_USER")
                  || user.has("HEAD_OF_LEGAL")
                  || user.has("SENIOR_MANAGEMENT")
                  || user.has("SYSTEM_ADMIN");

  return (
          all
                  ? matters.findAll(pageable)
                  : matters.findByRequesterId(
                  user.id(),
                  pageable
          )
  ).map(this::map);
 }

 @Transactional
 public MatterResponse assign(
         String reference,
         AssignmentRequest request,
         CurrentUser user
 ) {
  Matter matter = get(reference);

  matter.setOwnerId(
          request.userId()
  );

  matter.setOwnerName(
          request.userName()
  );

  matter.setStatus(
          MatterStatus.ASSIGNED
  );

  audit.record(
          matter.getId(),
          "MATTER_ASSIGNED",
          user,
          "Assigned to " + request.userName()
  );

  return map(matter);
 }

 @Transactional
 public MatterResponse status(
         String reference,
         StatusRequest request,
         CurrentUser user
 ) {
  Matter matter = get(reference);

  if (
          isPaused(request.status())
                  && Optional.ofNullable(
                          request.pendingReason()
                  ).orElse("")
                  .isBlank()
  ) {
   throw new IllegalArgumentException(
           "Pending reason is required for paused statuses"
   );
  }

  matter.setStatus(
          request.status()
  );

  matter.setPendingReason(
          request.pendingReason()
  );

  audit.record(
          matter.getId(),
          "STATUS_CHANGED",
          user,
          request.status().name()
  );

  return map(matter);
 }

 @Transactional
 public DocumentResponse upload(
         String reference,
         DocumentCategory category,
         MultipartFile file,
         CurrentUser user
 ) throws IOException {

  if (
          file.isEmpty()
                  || file.getSize() > 25 * 1024 * 1024
  ) {
   throw new IllegalArgumentException(
           "File must be non-empty and no larger than 25 MB"
   );
  }

  Matter matter = get(reference);

  var stored =
          storage.store(reference, file);

  DocumentRecord document =
          new DocumentRecord();

  document.setMatter(matter);

  document.setFileName(
          Optional.ofNullable(
                  file.getOriginalFilename()
          ).orElse("file")
  );

  document.setContentType(
          Optional.ofNullable(
                  file.getContentType()
          ).orElse(
                  "application/octet-stream"
          )
  );

  document.setSizeBytes(
          file.getSize()
  );

  document.setCategory(
          category
  );

  document.setExternalId(
          stored.externalId()
  );

  document.setWebUrl(
          stored.webUrl()
  );

  document.setUploadedBy(
          user.id()
  );

  document = docs.save(document);

  audit.record(
          matter.getId(),
          "DOCUMENT_UPLOADED",
          user,
          document.getFileName() +
                  " [" +
                  category +
                  "]"
  );

  return map(document);
 }

 @Transactional(readOnly = true)
 public List<DocumentResponse> listDocuments(
         String reference,
         CurrentUser user
 ) {
  Matter matter =
          getVisibleMatter(
                  reference,
                  user
          );

  return docs
          .findAllByMatterIdOrderByUploadedAtDesc(
                  matter.getId()
          )
          .stream()
          .map(this::map)
          .toList();
 }

 @Transactional(readOnly = true)
 public DownloadedDocument downloadDocument(
         String reference,
         UUID documentId,
         CurrentUser user
 ) throws IOException {

  Matter matter =
          getVisibleMatter(
                  reference,
                  user
          );

  DocumentRecord document =
          docs.findByIdAndMatterId(
                          documentId,
                          matter.getId()
                  )
                  .orElseThrow(() ->
                          new NoSuchElementException(
                                  "Document not found for matter "
                                          + reference
                          )
                  );

  Resource resource =
          storage.loadAsResource(
                  document.getExternalId()
          );

  if (
          !resource.exists()
                  || !resource.isReadable()
  ) {
   throw new NoSuchElementException(
           "Document content is unavailable."
   );
  }

  audit.record(
          matter.getId(),
          "DOCUMENT_VIEWED",
          user,
          document.getFileName()
  );

  return new DownloadedDocument(
          document.getFileName(),
          document.getContentType(),
          document.getSizeBytes(),
          resource
  );
 }

 @Transactional
 public void addFeedback(
         String reference,
         FeedbackRequest request,
         CurrentUser user
 ) {
  Matter matter = get(reference);

  if (
          !matter.getRequesterId().equals(
                  user.id()
          )
                  && !user.has("HEAD_OF_LEGAL")
  ) {
   throw new AccessDeniedException(
           "Only requester may submit feedback"
   );
  }

  Feedback feedbackItem =
          new Feedback();

  feedbackItem.setMatter(matter);
  feedbackItem.setRating(
          request.rating()
  );

  feedbackItem.setComment(
          request.comment()
  );

  feedbackItem.setSubmittedBy(
          user.id()
  );

  feedback.save(feedbackItem);

  audit.record(
          matter.getId(),
          "FEEDBACK_SUBMITTED",
          user,
          "Rating " + request.rating()
  );
 }

 private Matter getVisibleMatter(
         String reference,
         CurrentUser user
 ) {
  Matter matter = get(reference);

  boolean canViewAll =
          user.has("LEGAL_ADMIN")
                  || user.has("LEGAL_USER")
                  || user.has("HEAD_OF_LEGAL")
                  || user.has("SENIOR_MANAGEMENT")
                  || user.has("SYSTEM_ADMIN");

  if (
          !canViewAll &&
                  !Objects.equals(
                          matter.getRequesterId(),
                          user.id()
                  )
  ) {
   throw new AccessDeniedException(
           "Access denied"
   );
  }

  return matter;
 }

 private Matter get(String reference) {
  return matters
          .findByReference(reference)
          .orElseThrow(() ->
                  new NoSuchElementException(
                          "Matter not found: "
                                  + reference
                  )
          );
 }

 private String nextReference() {
  return "LEG-"
          + Year.now().getValue()
          + "-"
          + String.format(
          "%06d",
          matters.count() + 1
  );
 }

 private LocalDate calculateDue(
         CreateMatterRequest request
 ) {
  LocalDate triggerDate =
          request.triggerDate() != null
                  ? request.triggerDate()
                  : LocalDate.now();

  String subCategory =
          Optional.ofNullable(
                          request.subCategory()
                  )
                  .orElse("")
                  .toLowerCase(Locale.ROOT);

  String ruleId;

  if (subCategory.contains("summons")) {
   ruleId = "SUMMONS_DEFEND";
  } else if (
          subCategory.contains("motion")
                  || subCategory.contains(
                  "notice of motion"
          )
  ) {
   ruleId = "MOTION_OPPOSE";
  } else {
   ruleId = "ADDITIONAL_RESPONSE";
  }

  return configuration.calculateDeadline(
          ruleId,
          triggerDate
  );
 }

 private void validate(
         CreateMatterRequest request
 ) {
  if (
          request.urgency()
                  != Urgency.NORMAL
                  && Optional.ofNullable(
                          request.urgencyReason()
                  ).orElse("")
                  .isBlank()
  ) {
   throw new IllegalArgumentException(
           "Urgency reason is required"
   );
  }

  if (
          request.type()
                  == MatterType.CONTRACT_REVIEW
                  && (
                  blank(
                          request.commercialRationale()
                  )
                          || blank(
                          request.reviewPerspective()
                  )
          )
  ) {
   throw new IllegalArgumentException(
           "Commercial rationale and review perspective are required"
   );
  }
 }

 private boolean blank(String value) {
  return value == null || value.isBlank();
 }

 private boolean isPaused(
         MatterStatus status
 ) {
  return status
          == MatterStatus.AWAITING_THIRD_PARTY
          || status
          == MatterStatus.AWAITING_PM_BUSINESS
          || status
          == MatterStatus.PENDING_EXTERNAL_OUTCOME
          || status
          == MatterStatus.AWAITING_REQUESTER_INFORMATION;
 }

 private MatterResponse map(
         Matter matter
 ) {
  return new MatterResponse(
          matter.getId(),
          matter.getReference(),
          matter.getTitle(),
          matter.getType(),
          matter.getSubCategory(),
          matter.getStatus(),
          matter.getUrgency(),
          matter.getRequesterName(),
          matter.getBusinessUnit(),
          matter.getCounterparty(),
          matter.getOwnerName(),
          matter.getDueDate(),
          matter.getPendingReason(),
          matter.isRestrictedMatter(),
          matter.getCreatedAt()
  );
 }

 private DocumentResponse map(
         DocumentRecord document
 ) {
  return new DocumentResponse(
          document.getId(),
          document.getFileName(),
          document.getContentType(),
          document.getSizeBytes(),
          document.getCategory(),
          document.getWebUrl(),
          document.getUploadedAt()
  );
 }
}