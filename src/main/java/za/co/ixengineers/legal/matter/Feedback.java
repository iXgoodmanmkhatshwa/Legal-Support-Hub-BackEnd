package za.co.ixengineers.legal.matter;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
@Entity @Table(name="lsh_feedback")
@Getter @Setter
@NoArgsConstructor
public class Feedback{
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @OneToOne(fetch=FetchType.LAZY,optional=false) private Matter matter;
    @Column(nullable=false) private int rating;
    @Column(length=4000) private String comment;
    @Column(nullable=false) private String submittedBy;
    @Column(nullable=false,updatable=false) private Instant submittedAt;
    @PrePersist void create(){
        submittedAt=Instant.now();
    }
}
