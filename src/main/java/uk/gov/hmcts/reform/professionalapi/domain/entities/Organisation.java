package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static javax.persistence.GenerationType.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Entity(name = "organisation")
@Getter
@NoArgsConstructor
public class Organisation {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;
    @Column(name = "NAME")
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organisation")
    private List<ProfessionalUser> users = new ArrayList<>();
    @Column(name = "STATUS")
    private String status;
    @UpdateTimestamp
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    public Organisation(
            String name,
            String status) {

        this.name = name;
        this.status = status;
    }

    public void addProfessionalUser(ProfessionalUser professionalUser) {
        users.add(professionalUser);
    }
}
