package za.co.ixengineers.legal.admin;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import za.co.ixengineers.legal.security.CurrentUser;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static za.co.ixengineers.legal.admin.AdminConfigurationDtos.*;

@Service
@RequiredArgsConstructor
public class AdminConfigurationService {

    private static final int QUESTIONNAIRE_COUNT = 7;
    private static final int DOCUMENT_SET_COUNT = 7;
    private static final int REMINDER_TEMPLATE_COUNT = 12;
    private static final int ROLE_MAPPING_COUNT = 3;

    private final DeadlineRuleRepository deadlineRules;
    private final ConfigurationStateRepository configurationStates;

    @Transactional(readOnly = true)
    public ConfigurationSummaryResponse getConfiguration() {
        ConfigurationState state = getState();

        List<DeadlineRuleResponse> rules = deadlineRules
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::mapEffective)
                .toList();

        return new ConfigurationSummaryResponse(
                rules,
                QUESTIONNAIRE_COUNT,
                DOCUMENT_SET_COUNT,
                REMINDER_TEMPLATE_COUNT,
                ROLE_MAPPING_COUNT,
                deadlineRules.existsByDirtyTrue(),
                state.getPublishedAt(),
                state.getPublishedBy(),
                state.getPublishedVersion()
        );
    }

    @Transactional
    public DeadlineRuleResponse updateDeadlineRule(
            String ruleId,
            UpdateDeadlineRuleRequest request,
            CurrentUser user
    ) {
        DeadlineRule rule = deadlineRules
                .findById(ruleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deadline rule not found: " + ruleId
                ));

        /*
         * The frontend sends the rule's JPA version.
         * A stale version means another administrator
         * changed the same rule.
         */
        if (rule.getVersion() != request.version()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The deadline rule was changed by another administrator. "
                            + "Refresh Configuration and try again."
            );
        }

        rule.setDraftName(request.name().trim());
        rule.setDraftDescription(request.description().trim());
        rule.setDraftDays(request.days());
        rule.setDraftDayType(request.dayType());
        rule.setDraftTriggerName(request.trigger().trim());
        rule.setDraftEnabled(request.enabled());

        rule.setDirty(true);
        rule.setDraftUpdatedBy(user.name());
        rule.setDraftUpdatedAt(Instant.now());

        DeadlineRule saved =
                deadlineRules.saveAndFlush(rule);

        return mapEffective(saved);
    }

    @Transactional
    public PublishConfigurationResponse publish(
            PublishConfigurationRequest request,
            CurrentUser user
    ) {
        ConfigurationState state = getState();

        if (
                state.getPublishedVersion()
                        != request.version()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Configuration version is stale. "
                            + "Refresh Configuration before publishing."
            );
        }

        List<DeadlineRule> dirtyRules =
                deadlineRules.findByDirtyTrue();

        if (dirtyRules.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "There are no unpublished configuration changes."
            );
        }

        Instant publishedAt = Instant.now();

        for (DeadlineRule rule : dirtyRules) {
            rule.applyDraft();
            rule.setPublishedAt(publishedAt);
            rule.setPublishedBy(user.name());
        }

        deadlineRules.saveAll(dirtyRules);
        deadlineRules.flush();

        long nextVersion =
                state.getPublishedVersion() + 1;

        state.setPublishedVersion(nextVersion);
        state.setDraftVersion(nextVersion);
        state.setPublishedAt(publishedAt);
        state.setPublishedBy(user.name());

        configurationStates.saveAndFlush(state);

        return new PublishConfigurationResponse(
                nextVersion,
                publishedAt,
                user.name()
        );
    }

    /**
     * Called by MatterService.
     *
     * This method reads only published values. Draft values
     * are ignored until publish() applies each draft.
     */
    @Transactional(readOnly = true)
    public LocalDate calculateDeadline(
            String ruleId,
            LocalDate triggerDate
    ) {
        if (triggerDate == null) {
            throw new IllegalArgumentException(
                    "Trigger date is required to calculate a deadline"
            );
        }

        DeadlineRule rule = deadlineRules
                .findById(ruleId)
                .filter(DeadlineRule::isEnabled)
                .orElseThrow(() -> new IllegalStateException(
                        "Published deadline rule is unavailable: "
                                + ruleId
                ));

        return switch (rule.getDayType()) {
            case CALENDAR_DAYS ->
                    triggerDate.plusDays(rule.getDays());

            case BUSINESS_DAYS ->
                    addBusinessDays(
                            triggerDate,
                            rule.getDays()
                    );
        };
    }

    /**
     * Adds weekdays only.
     *
     * Saturday and Sunday are excluded. South African
     * public holidays are not yet excluded.
     */
    private LocalDate addBusinessDays(
            LocalDate start,
            int days
    ) {
        if (days < 0) {
            throw new IllegalArgumentException(
                    "Deadline days cannot be negative"
            );
        }

        LocalDate result = start;
        int added = 0;

        while (added < days) {
            result = result.plusDays(1);

            if (
                    result.getDayOfWeek()
                            != DayOfWeek.SATURDAY
                            &&
                            result.getDayOfWeek()
                                    != DayOfWeek.SUNDAY
            ) {
                added++;
            }
        }

        return result;
    }

    private ConfigurationState getState() {
        return configurationStates
                .findById(ConfigurationState.GLOBAL_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Global configuration state has not been seeded"
                ));
    }

    private DeadlineRuleResponse mapEffective(
            DeadlineRule rule
    ) {
        return new DeadlineRuleResponse(
                rule.getId(),
                rule.effectiveName(),
                rule.effectiveDescription(),
                rule.effectiveDays(),
                rule.effectiveDayType(),
                rule.effectiveTriggerName(),
                rule.effectiveEnabled(),
                rule.isDirty(),
                rule.getVersion()
        );
    }
}