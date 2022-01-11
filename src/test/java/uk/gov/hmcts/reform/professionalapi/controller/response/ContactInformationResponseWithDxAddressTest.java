package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

@ExtendWith(MockitoExtension.class)
class ContactInformationResponseWithDxAddressTest {

    final String expectAddress1 = "apt 1";
    final String expectAddress2 = "London Bridge Rd";
    final String expectAddress3 = "City Apartments";
    final String expectPostCode = "N1 PD2";
    final String expectCounty = "City of London";
    final String expectCountry = "England";
    final String expectTownCity = "London";
    final String uprn = "uprn";

    @Test
    void testGetContactInformationResponse() {
        List<DxAddress> dxAddressList = Arrays.asList(new DxAddress());

        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(expectAddress1);
        contactInformation.setAddressLine2(expectAddress2);
        contactInformation.setAddressLine3(expectAddress3);
        contactInformation.setPostCode(expectPostCode);
        contactInformation.setCounty(expectCounty);
        contactInformation.setCountry(expectCountry);
        contactInformation.setTownCity(expectTownCity);
        contactInformation.setDxAddresses(dxAddressList);
        contactInformation.setUprn(uprn);

        ContactInformationResponseWithDxAddress sut = new ContactInformationResponseWithDxAddress(contactInformation);

        assertThat(sut.getAddressLine1()).isEqualTo(expectAddress1);
        assertThat(sut.getAddressLine2()).isEqualTo(expectAddress2);
        assertThat(sut.getAddressLine3()).isEqualTo(expectAddress3);
        assertThat(sut.getPostCode()).isEqualTo(expectPostCode);
        assertThat(sut.getCounty()).isEqualTo(expectCounty);
        assertThat(sut.getCountry()).isEqualTo(expectCountry);
        assertThat(sut.getTownCity()).isEqualTo(expectTownCity);
        assertThat(sut.getDxAddress()).isNotEmpty();
        assertThat(sut.getUprn()).isEqualTo(uprn);
    }

    @Test
    void testGetContactInformationResponseWithDxAddress() {
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn(uprn);
        contactInformation.setAddressLine1(expectAddress1);
        contactInformation.setAddressLine2(expectAddress2);
        contactInformation.setAddressLine3(expectAddress3);
        contactInformation.setPostCode(expectPostCode);
        contactInformation.setCounty(expectCounty);
        contactInformation.setCountry(expectCountry);
        contactInformation.setTownCity(expectTownCity);

        ContactInformationResponseWithDxAddress sut = new ContactInformationResponseWithDxAddress(contactInformation);

        assertThat(sut.getUprn()).isEqualTo(uprn);
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