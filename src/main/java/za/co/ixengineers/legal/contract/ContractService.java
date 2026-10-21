package za.co.ixengineers.legal.contract;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import za.co.ixengineers.legal.document.DocumentStorage;
import za.co.ixengineers.legal.security.CurrentUser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static za.co.ixengineers.legal.contract.ContractDtos.*;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final long MAX_FILE_SIZE =
            25L * 1024L * 1024L;

    private static final long MAX_PACKAGE_SIZE =
            100L * 1024L * 1024L;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(
                    "pdf",
                    "doc",
                    "docx",
                    "xls",
                    "xlsx",
                    "eml",
                    "msg"
            );

    private final ContractRepository contracts;
    private final ContractDocumentRepository documents;
    private final DocumentStorage storage;

    @Transactional(readOnly = true)
    public List<ContractResponse> list() {
        return contracts
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public ContractResponse create(
            String title,
            String counterparty,
            String contractType,
            LocalDate effectiveDate,
            LocalDate expiryDate,
            boolean autoRenewal,
            Integer noticePeriodDays,
            MultipartFile primaryFile,
            List<MultipartFile> annexures,
            CurrentUser user
    ) throws IOException {
        validateText(title, "Contract title");
        validateText(counterparty, "Counterparty");

        if (
                expiryDate != null &&
                        effectiveDate != null &&
                        expiryDate.isBefore(effectiveDate)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiry date cannot be before the effective date."
            );
        }

        if (
                noticePeriodDays != null &&
                        (
                                noticePeriodDays < 0 ||
                                        noticePeriodDays > 3650
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notice period must be between 0 and 3650 days."
            );
        }

        List<MultipartFile> packageFiles =
                new ArrayList<>();

        packageFiles.add(primaryFile);

        if (annexures != null) {
            packageFiles.addAll(annexures);
        }

        validatePackage(packageFiles);

        ContractRecord contract = new ContractRecord();

        contract.setReference(nextReference());
        contract.setTitle(title.trim());
        contract.setCounterparty(counterparty.trim());
        contract.setContractType(normalize(contractType));
        contract.setEffectiveDate(effectiveDate);
        contract.setExpiryDate(expiryDate);
        contract.setAutoRenewal(autoRenewal);
        contract.setNoticePeriodDays(noticePeriodDays);

        /*
         * AI extraction is not implemented yet.
         * Human confirmation is therefore required.
         */
        contract.setStatus(
                ContractStatus.AWAITING_CONFIRMATION
        );

        contract.setUploadedById(user.id());
        contract.setUploadedByName(user.name());

        contract = contracts.save(contract);

        storeDocument(
                contract,
                primaryFile,
                true,
                user
        );

        if (annexures != null) {
            for (MultipartFile annexure : annexures) {
                if (!annexure.isEmpty()) {
                    storeDocument(
                            contract,
                            annexure,
                            false,
                            user
                    );
                }
            }
        }

        return map(contract);
    }

    @Transactional
    public ContractResponse confirm(
            String reference,
            ConfirmContractRequest request,
            CurrentUser user
    ) {
        ContractRecord contract = get(reference);

        if (contract.getVersion() != request.version()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The contract was changed by another user. "
                            + "Refresh the register and try again."
            );
        }

        if (
                request.expiryDate() != null &&
                        request.effectiveDate() != null &&
                        request.expiryDate()
                                .isBefore(request.effectiveDate())
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiry date cannot be before the effective date."
            );
        }

        contract.setEffectiveDate(
                request.effectiveDate()
        );

        contract.setExpiryDate(
                request.expiryDate()
        );

        contract.setAutoRenewal(
                request.autoRenewal()
        );

        contract.setNoticePeriodDays(
                request.noticePeriodDays()
        );

        contract.setStatus(
                contract.getExpiryDate() != null &&
                        contract.getExpiryDate()
                                .isBefore(LocalDate.now())
                        ? ContractStatus.EXPIRED
                        : ContractStatus.ACTIVE
        );

        return map(contracts.saveAndFlush(contract));
    }

    private void storeDocument(
            ContractRecord contract,
            MultipartFile file,
            boolean primary,
            CurrentUser user
    ) throws IOException {
        var stored = storage.store(
                contract.getReference(),
                file
        );

        ContractDocument document =
                new ContractDocument();

        document.setContract(contract);

        document.setFileName(
                Optional
                        .ofNullable(file.getOriginalFilename())
                        .filter(name -> !name.isBlank())
                        .orElse("contract-file")
        );

        document.setContentType(
                Optional
                        .ofNullable(file.getContentType())
                        .filter(type -> !type.isBlank())
                        .orElse("application/octet-stream")
        );

        document.setSizeBytes(file.getSize());
        document.setPrimaryDocument(primary);
        document.setExternalId(stored.externalId());
        document.setWebUrl(stored.webUrl());
        document.setUploadedBy(user.id());

        documents.save(document);
    }

    private void validatePackage(
            List<MultipartFile> files
    ) {
        long totalSize = 0;

        for (MultipartFile file : files) {
            validateFile(file);
            totalSize += file.getSize();
        }

        if (totalSize > MAX_PACKAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "The entire contract package cannot exceed 100 MB."
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Contract documents must not be empty."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Each contract document must be no larger than 25 MB."
            );
        }

        String fileName = Optional
                .ofNullable(file.getOriginalFilename())
                .orElse("");

        String extension = fileName
                .contains(".")
                ? fileName
                .substring(
                        fileName.lastIndexOf('.') + 1
                )
                .toLowerCase(Locale.ROOT)
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported contract document type: "
                            + fileName
            );
        }
    }

    private void validateText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " is required."
            );
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private ContractRecord get(String reference) {
        return contracts
                .findByReference(reference)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Contract not found: "
                                        + reference
                        )
                );
    }

    private String nextReference() {
        return "CON-"
                + Year.now().getValue()
                + "-"
                + String.format(
                "%06d",
                contracts.count() + 1
        );
    }

    private ContractResponse map(
            ContractRecord contract
    ) {
        List<ContractDocumentResponse> files =
                documents
                        .findByContractIdOrderByPrimaryDocumentDescUploadedAtAsc(
                                contract.getId()
                        )
                        .stream()
                        .map(this::map)
                        .toList();

        return new ContractResponse(
                contract.getId(),
                contract.getReference(),
                contract.getTitle(),
                contract.getCounterparty(),
                contract.getContractType(),
                contract.getEffectiveDate(),
                contract.getExpiryDate(),
                contract.isAutoRenewal(),
                contract.getNoticePeriodDays(),
                contract.getStatus(),
                contract.getExtractionConfidence(),
                contract.getExtractionNotes(),
                files,
                contract.getVersion(),
                contract.getCreatedAt()
        );
    }

    private ContractDocumentResponse map(
            ContractDocument document
    ) {
        return new ContractDocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.isPrimaryDocument(),
                document.getWebUrl(),
                document.getUploadedAt()
        );
    }
}