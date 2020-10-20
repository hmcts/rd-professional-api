package uk.gov.hmcts.reform.professionalapi.provider;

import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class OrganisationBuilder {
    private UUID id;
    private String name;
    private List<SuperUser> users = new ArrayList<>();
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();
    private List<ContactInformation> contactInformations = new ArrayList<>();
    private OrganisationStatus status;
    private LocalDateTime lastUpdated;
    private LocalDateTime created;
    private String sraId;
    private Boolean sraRegulated;
    private String companyNumber;
    private String companyUrl;
    private String organisationIdentifier;

    private OrganisationBuilder() {
    }

    public static OrganisationBuilder anOrganisation() {
        return new OrganisationBuilder();
    }

    public OrganisationBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public OrganisationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public OrganisationBuilder withUsers(List<SuperUser> users) {
        this.users = users;
        return this;
    }

    public OrganisationBuilder withPaymentAccounts(List<PaymentAccount> paymentAccounts) {
        this.paymentAccounts = paymentAccounts;
        return this;
    }

    public OrganisationBuilder withContactInformations(List<ContactInformation> contactInformations) {
        this.contactInformations = contactInformations;
        return this;
    }

    public OrganisationBuilder withStatus(OrganisationStatus status) {
        this.status = status;
        return this;
    }

    public OrganisationBuilder withLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public OrganisationBuilder withCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public OrganisationBuilder withSraId(String sraId) {
        this.sraId = sraId;
        return this;
    }

    public OrganisationBuilder withSraRegulated(Boolean sraRegulated) {
        this.sraRegulated = sraRegulated;
        return this;
    }

    public OrganisationBuilder withCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public OrganisationBuilder withCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
        return this;
    }

    public OrganisationBuilder withOrganisationIdentifier(String organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
        return this;
    }

    public Organisation build() {
        Organisation organisation = new Organisation();
        organisation.setId(id);
        organisation.setName(name);
        organisation.setUsers(users);
        organisation.setPaymentAccounts(paymentAccounts);
        organisation.setContactInformations(contactInformations);
        organisation.setStatus(status);
        organisation.setLastUpdated(lastUpdated);
        organisation.setCreated(created);
        organisation.setSraId(sraId);
        organisation.setSraRegulated(sraRegulated);
        organisation.setCompanyNumber(companyNumber);
        organisation.setCompanyUrl(companyUrl);
        organisation.setOrganisationIdentifier(organisationIdentifier);
        return organisation;
    }
}
