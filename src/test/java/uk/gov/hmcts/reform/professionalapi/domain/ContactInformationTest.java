package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;


public class ContactInformationTest {

    ContactInformation contactInformation;

    @Before
    public void setUp() {
        contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setAddressLine2(RefDataUtil.removeEmptySpaces("some-address2"));
        contactInformation.setAddressLine3(RefDataUtil.removeEmptySpaces("some-address3"));
        contactInformation.setTownCity(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setCounty(RefDataUtil.removeEmptySpaces("some-county"));
        contactInformation.setCountry(RefDataUtil.removeEmptySpaces("some-country"));
        contactInformation.setPostCode(RefDataUtil.removeEmptySpaces("some-post-code"));
        contactInformation.setOrganisation(mock(Organisation.class));

    }


    @Test
    public void creates_contact_information_correctly() {

        assertThat(contactInformation.getId()).isNull();
    }

    @Test
    public void adds_dx_address_into_ContactInformation_Correctly() {

        DxAddress dxAddress = mock(DxAddress.class);

        contactInformation.addDxAddress(dxAddress);

        assertThat(contactInformation.getDxAddresses()).containsExactly(dxAddress);

        contactInformation.setLastUpdated(LocalDateTime.now());

        contactInformation.setCreated(LocalDateTime.now());

        assertThat(contactInformation.getLastUpdated()).isNotNull();

        assertThat(contactInformation.getCreated()).isNotNull();
    }


}
