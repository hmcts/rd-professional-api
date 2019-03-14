package uk.gov.hmcts.reform.professionalapi.domain.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "organisation")
@Getter
@NoArgsConstructor
public class Organisation {

    @Id
    private String id;
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organisation")
    private List<ProfessionalUser> users = new ArrayList<>();
    private String status;

    public Organisation(
            UUID id,
            String name,
            String status) {

        this.id = id.toString();
        this.name = name;
        this.status = status;
    }

    public void addProfessionalUser(ProfessionalUser professionalUser) {
        users.add(professionalUser);
    }
}
