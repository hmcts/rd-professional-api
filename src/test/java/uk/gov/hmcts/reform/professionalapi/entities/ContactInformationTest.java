package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;


public class ContactInformationTest {

    private Organisation organisation;

    private ContactInformation contactInformation;

    private DxAddress dxAddress;

    @Before
    public void setUp() {

        organisation = new Organisation();
        contactInformation = new ContactInformation();
        dxAddress = new DxAddress();
        contactInformation.setAddressLine1("some-address1");
        contactInformation.setAddressLine2("some-address2");
        contactInformation.setAddressLine3("some-address3");
        contactInformation.setTownCity("some-town-city");
        contactInformation.setCounty("some-county");
        contactInformation.setCountry("some-country");
        contactInformation.setPostCode("some-post-code");
        contactInformation.setOrganisation(organisation);
    }

    @Test
    public void creates_contact_information_correctly() {

        assertThat(contactInformation.getId()).isNull();
    }

    @Test
    public void adds_dx_address_into_ContactInformation_Correctly() {

        contactInformation.addDxAddress(dxAddress);

        assertThat(contactInformation.getDxAddresses()).containsExactly(dxAddress);

        contactInformation.setLastUpdated(LocalDateTime.now());

        contactInformation.setCreated(LocalDateTime.now());

        assertThat(contactInformation.getLastUpdated()).isNotNull();

        assertThat(contactInformation.getCreated()).isNotNull();
    }
}
