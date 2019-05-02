package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.Test;

public class DxAddressTest {

    @Test
    public void creates_dx_address_correctly() {

        ContactInformation contactInformation = mock(ContactInformation.class);

        DxAddress dxAddress = new DxAddress("some-number", "some-exchange", contactInformation);

        assertThat(dxAddress.getDxNumber()).isEqualTo("some-number");
        assertThat(dxAddress.getDxExchange()).isEqualTo("some-exchange");
        assertThat(dxAddress.getContactInformation()).isEqualTo(contactInformation);
        assertThat(dxAddress.getId()).isNull();

        dxAddress.setLastUpdated(LocalDateTime.now());

        dxAddress.setCreated(LocalDateTime.now());

        assertThat(dxAddress.getLastUpdated()).isNotNull();

        assertThat(dxAddress.getCreated()).isNotNull();
    }

}
