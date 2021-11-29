package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public class ContactInformationResponseWithDxAddressTest {

    final String expectAddress1 = "apt 1";
    final String expectAddress2 = "London Bridge Rd";
    final String expectAddress3 = "City Apartments";
    final String expectPostCode = "N1 PD2";
    final String expectCounty = "City of London";
    final String expectCountry = "England";
    final String expectTownCity = "London";

    @Test
    public void testGetContactInformationResponse() {
        List<DxAddress> dxAddressList = Collections.singletonList(new DxAddress());

        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(expectAddress1);
        contactInformation.setAddressLine2(expectAddress2);
        contactInformation.setAddressLine3(expectAddress3);
        contactInformation.setPostCode(expectPostCode);
        contactInformation.setCounty(expectCounty);
        contactInformation.setCountry(expectCountry);
        contactInformation.setTownCity(expectTownCity);
        contactInformation.setDxAddresses(dxAddressList);

        ContactInformationResponseWithDxAddress sut = new ContactInformationResponseWithDxAddress(contactInformation);

        assertThat(sut.getAddressLine1()).isEqualTo(expectAddress1);
        assertThat(sut.getAddressLine2()).isEqualTo(expectAddress2);
        assertThat(sut.getAddressLine3()).isEqualTo(expectAddress3);
        assertThat(sut.getPostCode()).isEqualTo(expectPostCode);
        assertThat(sut.getCounty()).isEqualTo(expectCounty);
        assertThat(sut.getCountry()).isEqualTo(expectCountry);
        assertThat(sut.getTownCity()).isEqualTo(expectTownCity);
        assertThat(sut.getDxAddress()).isNotEmpty();
    }

    @Test
    public void testGetContactInformationResponseWithDxAddress() {
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(expectAddress1);
        contactInformation.setAddressLine2(expectAddress2);
        contactInformation.setAddressLine3(expectAddress3);
        contactInformation.setPostCode(expectPostCode);
        contactInformation.setCounty(expectCounty);
        contactInformation.setCountry(expectCountry);
        contactInformation.setTownCity(expectTownCity);

        ContactInformationResponseWithDxAddress sut = new ContactInformationResponseWithDxAddress(contactInformation);

        assertThat(sut.getAddressLine1()).isEqualTo(expectAddress1);
        assertThat(sut.getAddressLine2()).isEqualTo(expectAddress2);
        assertThat(sut.getAddressLine3()).isEqualTo(expectAddress3);
        assertThat(sut.getPostCode()).isEqualTo(expectPostCode);
        assertThat(sut.getCounty()).isEqualTo(expectCounty);
        assertThat(sut.getCountry()).isEqualTo(expectCountry);
        assertThat(sut.getTownCity()).isEqualTo(expectTownCity);
        assertThat(sut.getDxAddress()).isEmpty();
    }
}