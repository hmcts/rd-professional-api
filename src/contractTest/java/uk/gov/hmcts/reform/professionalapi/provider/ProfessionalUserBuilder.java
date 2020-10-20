package uk.gov.hmcts.reform.professionalapi.provider;

import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProfessionalUserBuilder {
    private UUID id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private Organisation organisation;
    private List<UserAttribute> userAttributes = new ArrayList<>();
    private LocalDateTime deleted;
    private LocalDateTime lastUpdated;
    private LocalDateTime created;
    private String userIdentifier;
    private List<String> roles;
    private IdamStatus idamStatus;
    private String idamStatusCode;
    private String idamMessage;

    private ProfessionalUserBuilder() {
    }

    public static ProfessionalUserBuilder aProfessionalUser() {
        return new ProfessionalUserBuilder();
    }

    public ProfessionalUserBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ProfessionalUserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ProfessionalUserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ProfessionalUserBuilder withEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public ProfessionalUserBuilder withOrganisation(Organisation organisation) {
        this.organisation = organisation;
        return this;
    }

    public ProfessionalUserBuilder withUserAttributes(List<UserAttribute> userAttributes) {
        this.userAttributes = userAttributes;
        return this;
    }

    public ProfessionalUserBuilder withDeleted(LocalDateTime deleted) {
        this.deleted = deleted;
        return this;
    }

    public ProfessionalUserBuilder withLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public ProfessionalUserBuilder withCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public ProfessionalUserBuilder withUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
        return this;
    }

    public ProfessionalUserBuilder withRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public ProfessionalUserBuilder withIdamStatus(IdamStatus idamStatus) {
        this.idamStatus = idamStatus;
        return this;
    }

    public ProfessionalUserBuilder withIdamStatusCode(String idamStatusCode) {
        this.idamStatusCode = idamStatusCode;
        return this;
    }

    public ProfessionalUserBuilder withIdamMessage(String idamMessage) {
        this.idamMessage = idamMessage;
        return this;
    }

    public ProfessionalUser build() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        professionalUser.setId(id);
        professionalUser.setFirstName(firstName);
        professionalUser.setLastName(lastName);
        professionalUser.setEmailAddress(emailAddress);
        professionalUser.setOrganisation(organisation);
        professionalUser.setUserAttributes(userAttributes);
        professionalUser.setDeleted(deleted);
        professionalUser.setLastUpdated(lastUpdated);
        professionalUser.setCreated(created);
        professionalUser.setUserIdentifier(userIdentifier);
        professionalUser.setRoles(roles);
        professionalUser.setIdamStatus(idamStatus);
        professionalUser.setIdamStatusCode(idamStatusCode);
        professionalUser.setIdamMessage(idamMessage);
        return professionalUser;
    }
}
