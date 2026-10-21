package za.co.ixengineers.legal.matter;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import static za.co.ixengineers.legal.matter.Domain.*;
public final class MatterDtos{
    private MatterDtos(){}
    public record CreateMatterRequest(
            @NotNull MatterType type,@NotBlank String subCategory,
            @NotNull Urgency urgency,String urgencyReason,
            LocalDate deadlineDriver,@NotBlank String requestedService,
            @NotBlank String businessUnit,String counterparty,LocalDate triggerDate,
            String background,String commercialRationale,String reviewPerspective,
            String invoiceNumber,@PositiveOrZero BigDecimal amount,LocalDate paymentDueDate,
            String agreementReference,String court,boolean insuranceRelevant,boolean restrictedMatter){

    }
    public record MatterResponse(UUID id,String reference,String title,MatterType type,
                                 String subCategory,MatterStatus status,
                                 Urgency urgency,String requesterName,String businessUnit,
                                 String counterparty,String ownerName,LocalDate dueDate,
                                 String pendingReason,boolean restrictedMatter,Instant createdAt){

    }
    public record AssignmentRequest(
            @NotBlank String userId,@NotBlank String userName){

    }
    public record StatusRequest(@NotNull MatterStatus status,String pendingReason){

    }
    public record FeedbackRequest(@Min(1) @Max(5) int rating,@Size(max=4000) String comment){

    }
    public record DocumentResponse(UUID id,String fileName,String contentType,
                                   long sizeBytes,DocumentCategory category,String webUrl,Instant uploadedAt){

    }
}
