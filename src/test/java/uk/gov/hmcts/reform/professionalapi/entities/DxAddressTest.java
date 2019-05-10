package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public class DxAddressTest {

    private final String dummyDxNumber = "some-number";

    private final String dummyDxExchange = "some-exchange";

    private ContactInformation contactInformation;

    private DxAddress dxAddress;

    @Before
    public void setUp() {

        contactInformation = new ContactInformation();
        dxAddress = new DxAddress();
        dxAddress.setDxNumber(dummyDxNumber);
        dxAddress.setDxExchange(dummyDxExchange);
        dxAddress.setContactInformation(contactInformation);
    }

    @Test
    public void creates_dx_address_correctly() {

        assertThat(dxAddress.getDxNumber()).isEqualTo(dummyDxNumber);
        assertThat(dxAddress.getDxExchange()).isEqualTo(dummyDxExchange);
        assertThat(dxAddress.getContactInformation()).isEqualTo(contactInformation);
        assertThat(dxAddress.getId()).isNull();

        dxAddress.setLastUpdated(LocalDateTime.now());

        dxAddress.setCreated(LocalDateTime.now());

        assertThat(dxAddress.getLastUpdated()).isNotNull();

        assertThat(dxAddress.getCreated()).isNotNull();
    }
}
