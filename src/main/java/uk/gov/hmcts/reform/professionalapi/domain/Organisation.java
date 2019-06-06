package uk.gov.hmcts.reform.professionalapi.domain;

import static javax.persistence.GenerationType.AUTO;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "organisation")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Organisation {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "NAME")
    @Size(max = 255)
    private String name;

    @OneToMany(mappedBy = "organisation")
    private List<ProfessionalUser> users = new ArrayList<>();

    @OneToMany(mappedBy = "organisation")
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "organisation")
    private List<ContactInformation> contactInformations = new ArrayList<>();

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "SRA_ID")
    @Size(max = 255)
    private String sraId;

    @Column(name = "SRA_REGULATED")
    private Boolean sraRegulated;

    @Column(name = "COMPANY_NUMBER")
    @Size(max = 8)
    private String companyNumber;

    @Column(name = "COMPANY_URL")
    @Size(max = 512)
    private String companyUrl;

    @Column(name = "ORGANISATION_IDENTIFIER")
    private String organisationIdentifier;

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

    public void addProfessionalUser(ProfessionalUser professionalUser) {
        users.add(professionalUser);
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

    public String getName() {
        return name;
    }

    public List<ProfessionalUser> getUsers() {
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
}
