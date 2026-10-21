package za.co.ixengineers.legal.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "lsh_deadline_rules")
@Getter
@Setter
@NoArgsConstructor
public class DeadlineRule {

    @Id
    @Column(length = 100)
    private String id;

    /*
     * Published values used by the matter deadline engine.
     */
    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private int days;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DayType dayType;

    @Column(nullable = false, length = 250)
    private String triggerName;

    @Column(nullable = false)
    private boolean enabled;

    /*
     * Draft values edited through Configuration.
     * Null means no unpublished draft exists.
     */
    @Column(length = 250)
    private String draftName;

    @Column(length = 2000)
    private String draftDescription;

    private Integer draftDays;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DayType draftDayType;

    @Column(length = 250)
    private String draftTriggerName;

    private Boolean draftEnabled;

    @Column(nullable = false)
    private boolean dirty;

    @Column(length = 200)
    private String draftUpdatedBy;

    private Instant draftUpdatedAt;

    @Column(length = 200)
    private String publishedBy;

    private Instant publishedAt;

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void create() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void update() {
        updatedAt = Instant.now();
    }

    public String effectiveName() {
        return dirty && draftName != null
                ? draftName
                : name;
    }

    public String effectiveDescription() {
        return dirty && draftDescription != null
                ? draftDescription
                : description;
    }

    public int effectiveDays() {
        return dirty && draftDays != null
                ? draftDays
                : days;
    }

    public DayType effectiveDayType() {
        return dirty && draftDayType != null
                ? draftDayType
                : dayType;
    }

    public String effectiveTriggerName() {
        return dirty && draftTriggerName != null
                ? draftTriggerName
                : triggerName;
    }

    public boolean effectiveEnabled() {
        return dirty && draftEnabled != null
                ? draftEnabled
                : enabled;
    }

    public void applyDraft() {
        if (!dirty) {
            return;
        }

        name = effectiveName();
        description = effectiveDescription();
        days = effectiveDays();
        dayType = effectiveDayType();
        triggerName = effectiveTriggerName();
        enabled = effectiveEnabled();

        clearDraft();
    }

    public void clearDraft() {
        draftName = null;
        draftDescription = null;
        draftDays = null;
        draftDayType = null;
        draftTriggerName = null;
        draftEnabled = null;
        draftUpdatedBy = null;
        draftUpdatedAt = null;
        dirty = false;
    }
}