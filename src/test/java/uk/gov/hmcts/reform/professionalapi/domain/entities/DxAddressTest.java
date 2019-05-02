package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class DxAddressTest extends AbstractEntityTest {

    @Test
    public void creates_dx_address_correctly() {

        ContactInformation contactInformation = mock(ContactInformation.class);

        DxAddress dxAddress = new DxAddress("some-number", "some-exchange", contactInformation);

        assertThat(dxAddress.getDxNumber()).isEqualTo("some-number");
        assertThat(dxAddress.getDxExchange()).isEqualTo("some-exchange");
        assertThat(dxAddress.getContactInformation()).isEqualTo(contactInformation);
        assertThat(dxAddress.getId()).isNull();
    }

    @Override
    protected Object getBeanInstance() {
        return new DxAddress();
    }
}
