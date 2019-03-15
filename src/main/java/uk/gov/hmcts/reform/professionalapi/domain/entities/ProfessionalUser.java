package uk.gov.hmcts.reform.professionalapi.domain.entities;

import java.util.UUID;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "professional_user")
@Getter
@NoArgsConstructor
public class ProfessionalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;
    @Column(name = "STATUS")
    private String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANISATION_ID", nullable = false)
    private Organisation organisation;

    public ProfessionalUser(
            String firstName,
            String lastName,
            String emailAddress,
            String status,
            Organisation organisation) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.status = status;
        this.organisation = organisation;
    }
}
