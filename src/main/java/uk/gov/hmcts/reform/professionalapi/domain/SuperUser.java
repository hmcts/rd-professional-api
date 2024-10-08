package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "super_user_view")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SuperUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "FIRST_NAME", insertable = false, updatable = false)
    @Size(max = 255)
    private String firstName;

    @Column(name = "LAST_NAME", insertable = false, updatable = false)
    @Size(max = 255)
    private String lastName;

    @Column(name = "EMAIL_ADDRESS", insertable = false, updatable = false)
    @Size(max = 255)
    private String emailAddress;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID", nullable = false, insertable = false, updatable = false)
    @JsonBackReference
    private Organisation organisation;

    @Column(name = "DELETED", insertable = false, updatable = false)
    private LocalDateTime deleted;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED", insertable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "USER_IDENTIFIER", insertable = false, updatable = false)
    private String userIdentifier;


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
                new ProfessionalUser(this.getFirstName(), this.getLastName(), this.getEmailAddress(),
                        this.getOrganisation());

        professionalUser.setCreated(this.getCreated());
        professionalUser.setDeleted(this.getDeleted());
        professionalUser.setId(this.getId());
        professionalUser.setLastUpdated(this.getLastUpdated());
        professionalUser.setUserIdentifier(this.getUserIdentifier());

        return professionalUser;
    }

}
