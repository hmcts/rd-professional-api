package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;

@Entity(name = "professional_user")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NamedEntityGraph(
        name = "User.alljoins",
        attributeNodes = {
                @NamedAttributeNode(value = "userAccountMap"),
        }
)
public class ProfessionalUser implements Serializable {

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

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = UserAttribute.class, mappedBy = "professionalUser", cascade = CascadeType.ALL)
    private List<UserAttribute> userAttributes = new ArrayList<>();

    @Column(name = "DELETED")
    private LocalDateTime deleted;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Fetch(FetchMode.JOIN)
    @OneToMany(targetEntity = UserAccountMap.class)
    @JoinColumn(name = "PROFESSIONAL_USER_ID", referencedColumnName = "id")
    private List<UserAccountMap> userAccountMap = new ArrayList<>();

    @Column(name = "USER_IDENTIFIER")
    private String userIdentifier;

    @Transient
    private List<String> roles;

    @Transient
    private IdamStatus idamStatus;

    @Transient
    private String idamStatusCode;

    @Transient
    private String idamMessage;

    public ProfessionalUser(
                            String firstName,
                            String lastName,
                            String emailAddress,
                            Organisation organisation) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.organisation = organisation;
    }

    public SuperUser toSuperUser() {
        SuperUser superUser =
                new SuperUser(this.getFirstName(), this.getLastName(), this.getEmailAddress(), this.getOrganisation());

        superUser.setCreated(this.getCreated());
        superUser.setDeleted(this.getDeleted());
        superUser.setId(this.getId());
        superUser.setLastUpdated(this.getLastUpdated());
        superUser.setUserIdentifier(this.getUserIdentifier());

        return superUser;
    }
}
