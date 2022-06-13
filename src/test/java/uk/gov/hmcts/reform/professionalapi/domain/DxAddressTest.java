package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DxAddressTest {

    @Test
    void test_creates_dx_address_correctly() {
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

    @Test
    void test_creates_dx_address_with_exchange_more_than_20_characters_correctly() {
        ContactInformation contactInformation = new ContactInformation();
        DxAddress dxAddress = new DxAddress("DX 1234567890",
                "some-exchange-twenty-characters", contactInformation);
        dxAddress.setLastUpdated(LocalDateTime.now());
        dxAddress.setCreated(LocalDateTime.now());

        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 1234567890");
        assertThat(dxAddress.getDxExchange()).isEqualTo("some-exchange-twenty-characters");
        assertThat(dxAddress.getContactInformation()).isEqualTo(contactInformation);
        assertThat(dxAddress.getId()).isNull();
        assertThat(dxAddress.getLastUpdated()).isNotNull();
        assertThat(dxAddress.getCreated()).isNotNull();
    }
}
