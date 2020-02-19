package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public class DxAddressTest {

    @Test
    public void creates_dx_address_correctly() {

        ContactInformation contactInformation = mock(ContactInformation.class);

        DxAddress dxAddress = new DxAddress("DX 1234567890", "some-exchange", contactInformation);

        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 1234567890");
        assertThat(dxAddress.getDxExchange()).isEqualTo("some-exchange");
        assertThat(dxAddress.getContactInformation()).isEqualTo(contactInformation);
        assertThat(dxAddress.getId()).isNull();

        dxAddress.setLastUpdated(LocalDateTime.now());

        dxAddress.setCreated(LocalDateTime.now());

        assertThat(dxAddress.getLastUpdated()).isNotNull();

        assertThat(dxAddress.getCreated()).isNotNull();
    }

}
