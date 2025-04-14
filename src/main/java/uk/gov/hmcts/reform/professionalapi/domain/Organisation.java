package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.GenerationType.AUTO;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NamedEntityGraph(
    name = "Organisation.alljoins",
    attributeNodes = {
        @NamedAttributeNode("users"),
        @NamedAttributeNode("paymentAccounts"),
        @NamedAttributeNode("contactInformations"),
        @NamedAttributeNode("orgAttributes"),
        @NamedAttributeNode("bulkCustomerDetails")
    }
)
public class Organisation implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "NAME")
    @Size(max = 255)
    private String name;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = SuperUser.class)
    @JoinColumn(name = "organisation_id", insertable = false, updatable = false)
    @JsonManagedReference
    private List<SuperUser> users = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "organisation")
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "organisation")
    private List<ContactInformation> contactInformations = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "organisation", optional = false, fetch = FetchType.LAZY)
    private OrganisationMfaStatus organisationMfaStatus;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "organisation")
    private List<BulkCustomerDetails> bulkCustomerDetails = new ArrayList<>();

    @OneToMany(mappedBy = "organisation")
    private List<OrgAttribute> orgAttributes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrganisationStatus status;

    private String statusMessage;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    private String orgType;

    @CreatedDate
    private LocalDateTime created;

    @Size(max = 255)
    private String sraId;

    private Boolean sraRegulated;

    private String companyNumber;

    @Size(max = 512)
    private String companyUrl;

    private String organisationIdentifier;

    private LocalDateTime dateApproved;

    public Organisation(String name, OrganisationStatus status, String sraId,
                        String companyNumber, Boolean sraRegulated, String companyUrl) {
        this.name = name;
        this.status = status;
        this.sraId = sraId;
        this.companyNumber = companyNumber;
        this.sraRegulated = sraRegulated;
        this.companyUrl = companyUrl;
        this.organisationIdentifier = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);
    }

    public boolean isOrganisationStatusActive() {
        return OrganisationStatus.ACTIVE == this.status;
    }

    public void addProfessionalUser(SuperUser superUser) {
        this.users.add(superUser);
    }

    public void addPaymentAccount(PaymentAccount paymentAccount) {
        this.paymentAccounts.add(paymentAccount);
    }

    public void addContactInformation(ContactInformation contactInformation) {
        this.contactInformations.add(contactInformation);
    }

    public void addAttribute(OrgAttribute orgAttribute) {
        this.orgAttributes.add(orgAttribute);
    }
}
