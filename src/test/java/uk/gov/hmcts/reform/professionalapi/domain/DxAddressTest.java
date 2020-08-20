package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;

public class DxAddressTest {

    @Test
    public void test_creates_dx_address_correctly() {
        ContactInformation contactInformation = new ContactInformation();
        DxAddress dxAddress = new DxAddress("DX 1234567890", "some-exchange", contactInformation);
        dxAddress.setLastUpdated(LocalDateTime.now());
        dxAddress.setCreated(LocalDateTime.now());

        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 1234567890");
        assertThat(dxAddress.getDxExchange()).isEqualTo("some-exchange");
        assertThat(dxAddress.getContactInformation()).isEqualTo(contactInformation);
        assertThat(dxAddress.getId()).isNull();
        assertThat(dxAddress.getLastUpdated()).isNotNull();
        assertThat(dxAddress.getCreated()).isNotNull();
    }
}
