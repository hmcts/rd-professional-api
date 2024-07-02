package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NamedEntityGraph(
        name = "Organisation.alljoins",
        attributeNodes = {
                @NamedAttributeNode(value = "users"),
        }
)
@SuppressWarnings("checkstyle:Indentation")
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
    @OneToMany(targetEntity = PaymentAccount.class, mappedBy = "organisation")
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = ContactInformation.class, mappedBy = "organisation")
    private List<ContactInformation> contactInformations = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "organisation", optional = false, fetch = FetchType.LAZY)
    private OrganisationMfaStatus organisationMfaStatus;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = BulkCustomerDetails.class, mappedBy = "organisation")
    private List<BulkCustomerDetails> bulkCustomerDetails;

    @OneToMany(targetEntity = OrgAttribute.class, mappedBy = "organisation")
    private List<OrgAttribute> orgAttributes = new ArrayList<>();

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status;

    @Column(name = "STATUS_MESSAGE")
    private String statusMessage;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @Column(name = "ORG_TYPE")
    private String orgType;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "SRA_ID")
    @Size(max = 255)
    private String sraId;

    @Column(name = "SRA_REGULATED")
    private Boolean sraRegulated;

    @Column(name = "COMPANY_NUMBER")
    private String companyNumber;

    @Column(name = "COMPANY_URL")
    @Size(max = 512)
    private String companyUrl;

    @Column(name = "ORGANISATION_IDENTIFIER")
    private String organisationIdentifier;

    @Column(name = "date_approved")
    private LocalDateTime dateApproved;


    public Organisation(
            String name,
            OrganisationStatus status,
            String sraId,
            String companyNumber,
            Boolean sraRegulated,
            String companyUrl) {

        this.name = name;
        this.status = status;
        this.sraId = sraId;
        this.companyNumber = companyNumber;
        this.sraRegulated = sraRegulated;
        this.companyUrl = companyUrl;
        this.organisationIdentifier = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);
    }

    public void addProfessionalUser(SuperUser superUser) {
        users.add(superUser);
    }

    public void addPaymentAccount(PaymentAccount paymentAccount) {
        paymentAccounts.add(paymentAccount);
    }

    public void addContactInformation(ContactInformation contactInformation) {
        contactInformations.add(contactInformation);
    }

    public UUID getId() {
        return id;
    }

    public String getOrgType() {
        return orgType;
    }

    public List<OrgAttribute> getOrgAttributes() {
        return orgAttributes;
    }

    public String getName() {
        return name;
    }

    public List<SuperUser> getUsers() {
        return users;
    }

    public List<PaymentAccount> getPaymentAccounts() {
        return paymentAccounts;
    }

    public List<ContactInformation> getContactInformation() {
        return contactInformations;
    }

    public OrganisationStatus getStatus() {
        return status;
    }

    public String getSraId() {
        return sraId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public Boolean getSraRegulated() {
        return sraRegulated;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public String getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    public void setOrganisationIdentifier(String organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }

    public boolean isOrganisationStatusActive() {
        return OrganisationStatus.ACTIVE == getStatus();
    }

    public void addAttribute(OrgAttribute orgAttribute) {
        orgAttributes.add(orgAttribute);
    }

}
