package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserTest {

    private List<UserAttribute> userAttributes;
    private List<UserConfiguredAccess> userConfiguredAccesses;

    @BeforeEach
    void setUp() {
        userAttributes = new ArrayList<>();
        userConfiguredAccesses = new ArrayList<>();
        userConfiguredAccesses.add(new UserConfiguredAccess());
    }

    @Test
    void test_creates_professional_for_empty_user_Attribute() {

        List<String> roles = new ArrayList<>();
        roles.add("pui-user-manager");

        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "some-email-address", organisation);

        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setRoles(roles);
        professionalUser.setUserAttributes(userAttributes);
        professionalUser.setUserConfiguredAccesses(userConfiguredAccesses);

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getUserIdentifier()).isNull();
        assertThat(professionalUser.getId()).isNull(); // hibernate generated
        assertThat(professionalUser.getRoles()).hasSize(1);
        assertThat(professionalUser.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(professionalUser.getLastUpdated()).isNotNull();
        assertThat(professionalUser.getCreated()).isNotNull();
        assertThat(professionalUser.getUserAttributes()).isEmpty();
        assertThat(professionalUser.getUserConfiguredAccesses()).hasSize(1);
    }


    @Test
    void test_creates_professional_user_correctly() {
        List<String> roles = new ArrayList<>();
        roles.add("pui-user-manager");

        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "some-email-address", organisation);

        PrdEnumId prdEnumId = new PrdEnumId(1, "test");
        PrdEnum anEnum = new PrdEnum(prdEnumId, "testenumName", "testenumDescription");
        UserAttribute userAttribute = new UserAttribute(professionalUser, anEnum);
        List<UserAttribute> userAttributes = new ArrayList<>();
        userAttributes.add(userAttribute);

        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setRoles(roles);
        professionalUser.setUserAttributes(userAttributes);
        professionalUser.setUserConfiguredAccesses(userConfiguredAccesses);

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getUserIdentifier()).isNull();
        assertThat(professionalUser.getId()).isNull(); // hibernate generated
        assertThat(professionalUser.getRoles()).hasSize(1);
        assertThat(professionalUser.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(professionalUser.getLastUpdated()).isNotNull();
        assertThat(professionalUser.getCreated()).isNotNull();
        assertThat(professionalUser.getUserAttributes()).isNotNull();
        assertThat(professionalUser.getUserConfiguredAccesses()).hasSize(1);

        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }


    @Test
    void test_creates_professional_user_for_empty_user_attribute_correctly() {
        List<String> roles = new ArrayList<>();
        roles.add("pui-user-manager");

        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "some-email-address", organisation);
        List<UserAttribute> userAttributes = null;


        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setRoles(roles);
        professionalUser.setUserAttributes(userAttributes);
        professionalUser.setUserConfiguredAccesses(new ArrayList<>());

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getUserIdentifier()).isNull();
        assertThat(professionalUser.getId()).isNull(); // hibernate generated
        assertThat(professionalUser.getRoles()).hasSize(1);
        assertThat(professionalUser.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(professionalUser.getLastUpdated()).isNotNull();
        assertThat(professionalUser.getCreated()).isNotNull();
        assertThat(professionalUser.getUserAttributes()).isNull();
        assertThat(professionalUser.getUserConfiguredAccesses()).isEmpty();

        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

    @Test
    void test_toSuperUser() {
        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "some-email-address", organisation);

        PrdEnumId prdEnumId = new PrdEnumId(1, "test");
        PrdEnum anEnum = new PrdEnum(prdEnumId, "testenumName", "testenumDescription");
        UserAttribute userAttribute = new UserAttribute(professionalUser, anEnum);
        List<UserAttribute> userAttributes = new ArrayList<>();
        userAttributes.add(userAttribute);

        UUID id = UUID.randomUUID();
        professionalUser.setUserIdentifier(id);
        professionalUser.setId(id);
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setDeleted(LocalDateTime.now());
        professionalUser.setUserAttributes(userAttributes);
        professionalUser.setUserConfiguredAccesses(userConfiguredAccesses);

        SuperUser superUser = professionalUser.toSuperUser();
        assertThat(superUser.getFirstName()).isEqualTo("some-fname");
        assertThat(superUser.getLastName()).isEqualTo("some-lname");
        assertThat(superUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(superUser.getCreated()).isNotNull();
        assertThat(superUser.getDeleted()).isNotNull();
        assertThat(superUser.getId()).isNotNull();
        assertThat(superUser.getLastUpdated()).isNotNull();
        assertThat(superUser.getUserIdentifier()).isEqualTo(id.toString());
        assertThat(professionalUser.getUserAttributes()).isNotNull();
        assertThat(professionalUser.getUserConfiguredAccesses()).hasSize(1);
    }
}
