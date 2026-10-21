package za.co.ixengineers.legal.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "lsh_configuration_state")
@Getter
@Setter
@NoArgsConstructor
public class ConfigurationState {

    public static final String GLOBAL_ID = "GLOBAL";

    @Id
    @Column(length = 30)
    private String id;

    @Column(nullable = false)
    private long publishedVersion;

    @Column(nullable = false)
    private long draftVersion;

    private Instant publishedAt;

    @Column(length = 200)
    private String publishedBy;

    @Version
    private long entityVersion;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void create() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    void update() {
        updatedAt = Instant.now();
    }
}