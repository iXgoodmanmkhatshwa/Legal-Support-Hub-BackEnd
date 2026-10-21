package za.co.ixengineers.legal.contract;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ContractDtos {

    private ContractDtos() {
    }

    public record ContractDocumentResponse(
            UUID id,
            String fileName,
            String contentType,
            long sizeBytes,
            boolean primaryDocument,
            String webUrl,
            Instant uploadedAt
    ) {
    }

    public record ContractResponse(
            UUID id,
            String reference,
            String title,
            String counterparty,
            String contractType,
            LocalDate effectiveDate,
            LocalDate expiryDate,
            boolean autoRenewal,
            Integer noticePeriodDays,
            ContractStatus status,
            Integer extractionConfidence,
            String extractionNotes,
            List<ContractDocumentResponse> documents,
            long version,
            Instant createdAt
    ) {
    }

    public record ConfirmContractRequest(
            LocalDate effectiveDate,
            LocalDate expiryDate,

            boolean autoRenewal,

            @Min(0)
            @Max(3650)
            Integer noticePeriodDays,

            @NotNull
            Long version
    ) {
    }
}