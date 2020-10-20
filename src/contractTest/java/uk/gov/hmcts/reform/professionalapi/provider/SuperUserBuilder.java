package uk.gov.hmcts.reform.professionalapi.provider;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

import java.time.LocalDateTime;
import java.util.UUID;

public final class SuperUserBuilder {
    private UUID id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private Organisation organisation;
    private LocalDateTime deleted;
    private LocalDateTime lastUpdated;
    private LocalDateTime created;
    private String userIdentifier;

    private SuperUserBuilder() {
    }

    public static SuperUserBuilder aSuperUser() {
        return new SuperUserBuilder();
    }

    public SuperUserBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public SuperUserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public SuperUserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public SuperUserBuilder withEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public SuperUserBuilder withOrganisation(Organisation organisation) {
        this.organisation = organisation;
        return this;
    }

    public SuperUserBuilder withDeleted(LocalDateTime deleted) {
        this.deleted = deleted;
        return this;
    }

    public SuperUserBuilder withLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public SuperUserBuilder withCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public SuperUserBuilder withUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
        return this;
    }

    public SuperUser build() {
        SuperUser superUser = new SuperUser();
        superUser.setId(id);
        superUser.setFirstName(firstName);
        superUser.setLastName(lastName);
        superUser.setEmailAddress(emailAddress);
        superUser.setOrganisation(organisation);
        superUser.setDeleted(deleted);
        superUser.setLastUpdated(lastUpdated);
        superUser.setCreated(created);
        superUser.setUserIdentifier(userIdentifier);
        return superUser;
    }
}
