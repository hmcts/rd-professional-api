package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

@ExtendWith(MockitoExtension.class)
public class DxAddressResponseTest {

    @Test
    public void test_DxAddress() {
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