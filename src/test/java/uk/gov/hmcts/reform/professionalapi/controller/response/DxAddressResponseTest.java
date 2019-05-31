package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public class DxAddressResponseTest {
    String expectDxNumber = "01234567";
    String expectDxExchange = "DX 1234";
    DxAddress dxAddress = new DxAddress();


    @Test
    public void testDxAddress() throws Exception {
        dxAddress.setDxNumber(expectDxNumber);
        dxAddress.setDxExchange(expectDxExchange);
        DxAddressResponse dxAddressResponse = new DxAddressResponse(dxAddress);

        Field f = dxAddressResponse.getClass().getDeclaredField("dxNumber");
        f.setAccessible(true);
        String actualDxNumber = (String) f.get(dxAddressResponse);

        f = dxAddressResponse.getClass().getDeclaredField("dxExchange");
        f.setAccessible(true);
        String actualDxExchange = (String) f.get(dxAddressResponse);

        assertThat(expectDxNumber).isEqualTo(actualDxNumber);
        assertThat(expectDxExchange).isEqualTo(actualDxExchange);
    }



}