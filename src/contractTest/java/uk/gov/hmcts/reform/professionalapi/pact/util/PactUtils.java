package uk.gov.hmcts.reform.professionalapi.pact.util;

import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccess;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccessId;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;

public class PactUtils {

    private PactUtils() {
    }

    public static final String PROFESSIONAL_USER_ID_STR = "123456";
    public static final UUID PROFESSIONAL_USER_ID = UUID.fromString(PROFESSIONAL_USER_ID_STR);
    public static final String PROFESSIONAL_USER_ID2_STR = "234567";
    public static final UUID PROFESSIONAL_USER_ID2 =  UUID.fromString(PROFESSIONAL_USER_ID2_STR);
    public static final String ORGANISATION_IDENTIFIER = "HM2OHHS";
    public static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    public static Organisation getMinimalOrganisation() {
        return new Organisation("Org-Name", OrganisationStatus.ACTIVE, "sra-id",
                "companyN", false, "www.org.com");
    }

    public static void addSuperUser(Organisation organisation) {
        SuperUser superUser = new SuperUser("some-fname", "some-lname",
                "some-email-address", organisation);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(users);
    }

    public static Organisation getOrganisation() {
        Organisation organisation = getMinimalOrganisation();
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn");
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        organisation.setContactInformations(Collections.singletonList(contactInformation));
        return organisation;
    }

    public static UserProfileCreationResponse getUserProfileCreationResponse() {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);
        return userProfileCreationResponse;
    }

    public static UserProfile getUserProfile() {
        return new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
    }

    public static ProfessionalUser getProfessionalUser(String name, String sraId, String companyNumber,
                                                       String companyUrl) {
        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.setCompanyNumber(companyNumber);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setSraId(sraId);
        organisation.setSraRegulated(true);
        organisation.setCompanyUrl(companyUrl);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");

        SuperUser su = getSuperUser();

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");

        organisation.setPaymentAccounts(Collections.singletonList(pa));
        organisation.setUsers(Collections.singletonList(su));

        ProfessionalUser pu = new ProfessionalUser();
        pu.setUserIdentifier(PROFESSIONAL_USER_ID);
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }

    public static ProfessionalUser getProfessionalUser() {
        return getProfessionalUser("org-name", "sra-id", "companyNumber",
                "companyUrl");
    }

    public static ProfessionalUser getProfessionalUser(Organisation organisation, int i) {
        ProfessionalUser professionalUser = new ProfessionalUser("firstName" + i, "lastName" + i,
                "email" + 1 + "@org.com", organisation);
        professionalUser.setId(UUID.randomUUID());
        professionalUser.setUserIdentifier("someUid" +  i);
        professionalUser.setIdamStatus(IdamStatus.ACTIVE);
        professionalUser.setIdamStatusCode("200");
        professionalUser.setLastUpdated(LocalDateTime.now());
        return professionalUser;

    }

    public static Organisation getOrgWithMfaStatus() {
        Organisation organisation = getMinimalOrganisation();
        OrganisationMfaStatus organisationMfaStatus = new OrganisationMfaStatus();
        organisationMfaStatus.setMfaStatus(MFAStatus.EMAIL);
        organisation.setOrganisationMfaStatus(organisationMfaStatus);
        return organisation;
    }

    public static SuperUser getSuperUser() {
        SuperUser su = new SuperUser();
        su.setEmailAddress("superUser@email.com");
        su.setFirstName("some-fname");
        su.setLastName("some-lname");
        su.setUserIdentifier("someUserIdentifier");
        return su;
    }

    public static UserConfiguredAccess getUserConfiguredAccess(ProfessionalUser professionalUser, int i) {
        final UserConfiguredAccessId userConfiguredAccessId =
                new UserConfiguredAccessId(professionalUser,
                        "jurisdictionId" + 1,
                        "organisationProfileId" + 1,
                        "accessTypeId" + 1);
        return new UserConfiguredAccess(userConfiguredAccessId,true);
    }

    public static List<UserConfiguredAccess> getUserConfiguredAccesses(ProfessionalUser professionalUser) {
        final UserConfiguredAccess userConfiguredAccess1 = getUserConfiguredAccess(professionalUser, 1);
        return List.of(userConfiguredAccess1);
    }

    public static Set<UserAccessType> getUserAccessTypes() {
        UserAccessType userAccessType = new UserAccessType(
                "jurisdictionId1",
                "organisationProfileId1",
                "accessTypeId1",
                true);
        return Set.of(userAccessType);
    }

    public static Organisation setUpOrganisation(String name, String sraId, String companyNumber, String companyUrl) {
        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.setCompanyNumber(companyNumber);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setSraId(sraId);
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        organisation.setCompanyUrl(companyUrl);
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn");
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        organisation.setContactInformations(asList(contactInformation));

        return organisation;
    }
}
