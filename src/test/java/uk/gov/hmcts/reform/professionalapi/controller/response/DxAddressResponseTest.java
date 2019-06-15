package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public class DxAddressResponseTest {

    @Test
    public void testDxAddress() {
        final String expectDxNumber = "01234567";
        final String expectDxExchange = "DX 1234";
        final DxAddress dxAddress = new DxAddress();
        dxAddress.setDxNumber(expectDxNumber);
        dxAddress.setDxExchange(expectDxExchange);

        DxAddressResponse sut = new DxAddressResponse(dxAddress);

        assertThat(sut.getDxExchange()).isEqualTo(expectDxExchange);
        assertThat(sut.getDxNumber()).isEqualTo(expectDxNumber);
    }



}