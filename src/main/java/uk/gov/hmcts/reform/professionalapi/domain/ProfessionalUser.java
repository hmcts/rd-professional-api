package uk.gov.hmcts.reform.professionalapi.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator;

@Entity(name = "professional_user")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ProfessionalUser {

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

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private ProfessionalUserStatus status;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID", nullable = false)
    private Organisation organisation;

    @OneToMany(mappedBy = "professionalUser")
    private List<UserAttribute> userAttributes = new ArrayList<>();

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    @OneToMany
    @JoinColumn(name = "PROFESSIONAL_USER_ID", referencedColumnName = "id")
    private List<UserAccountMap> userAccountMap = new ArrayList<>();

    @Column(name = "USER_IDENTIFIER")
    private UUID userIdentifier;

    public ProfessionalUser(
                            String firstName,
                            String lastName,
                            String emailAddress,
                            ProfessionalUserStatus status,
                            Organisation organisation) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.status = status;
        this.organisation = organisation;
        this.userIdentifier = ProfessionalApiGenerator.generateUniqueUuid();
    }
}
