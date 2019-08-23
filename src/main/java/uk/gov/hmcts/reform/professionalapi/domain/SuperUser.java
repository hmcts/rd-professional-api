package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "super_user_view")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SuperUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "FIRST_NAME")
    @Size(max = 255)
    private String firstName;

    @Column(name = "LAST_NAME")
    @Size(max = 255)
    private String lastName;

    @Column(name = "EMAIL_ADDRESS")
    @Size(max = 255)
    private String emailAddress;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID", nullable = false)
    private Organisation organisation;

    @Column(name = "DELETED")
    private LocalDateTime deleted;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "USER_IDENTIFIER")
    private UUID userIdentifier;

    @Transient
    private List<String> roles;

    @Transient
    private IdamStatus idamStatus;

    @Transient
    private String idamStatusCode;

    @Transient
    private String idamMessage;

    public SuperUser(
                            String firstName,
                            String lastName,
                            String emailAddress,
                            Organisation organisation) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.organisation = organisation;
    }

    public ProfessionalUser toProfessionalUser() {
        ProfessionalUser professionalUser =
                new ProfessionalUser(this.getFirstName(), this.getLastName(), this.getEmailAddress(), this.getOrganisation());

        professionalUser.setCreated(this.getCreated());
        professionalUser.setDeleted(this.getDeleted());
        professionalUser.setId(this.getId());
        professionalUser.setIdamMessage(this.getIdamMessage());
        professionalUser.setIdamStatusCode(this.getIdamStatusCode());
        professionalUser.setIdamStatus(this.getIdamStatus());
        professionalUser.setLastUpdated(this.getLastUpdated());
        professionalUser.setRoles(this.getRoles());
        professionalUser.setUserIdentifier(this.getUserIdentifier());

        return professionalUser;
    }
}
