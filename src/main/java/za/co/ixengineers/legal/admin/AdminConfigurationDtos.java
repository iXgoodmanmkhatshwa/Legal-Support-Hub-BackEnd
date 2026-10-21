package za.co.ixengineers.legal.admin;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public final class AdminConfigurationDtos {

    private AdminConfigurationDtos() {
    }

    public record DeadlineRuleResponse(
            String id,
            String name,
            String description,
            int days,
            DayType dayType,
            String trigger,
            boolean enabled,
            boolean dirty,
            long version
    ) {
    }

    public record ConfigurationSummaryResponse(
            List<DeadlineRuleResponse> deadlineRules,
            int questionnaireCount,
            int documentSetCount,
            int reminderTemplateCount,
            int roleMappingCount,
            boolean dirty,
            Instant publishedAt,
            String publishedBy,
            long version
    ) {
    }

    public record UpdateDeadlineRuleRequest(
            @NotBlank
            @Size(max = 250)
            String name,

            @NotNull
            @Size(max = 2000)
            String description,

            @Min(0)
            @Max(3650)
            int days,

            @NotNull
            DayType dayType,

            @NotBlank
            @Size(max = 250)
            String trigger,

            boolean enabled,

            @PositiveOrZero
            long version
    ) {
    }

    public record PublishConfigurationRequest(
            @PositiveOrZero
            long version
    ) {
    }

    public record PublishConfigurationResponse(
            long version,
            Instant publishedAt,
            String publishedBy
    ) {
    }
}