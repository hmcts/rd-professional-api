package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public class DxAddressResponseTest {
    private final String expectDxNumber = "01234567";
    private final String expectDxExchange = "DX 1234";
    private DxAddress dxAddress = new DxAddress();


    @Test
    public void testDxAddress() {
        dxAddress.setDxNumber(expectDxNumber);
        dxAddress.setDxExchange(expectDxExchange);
        DxAddressResponse dxAddressResponse = new DxAddressResponse(dxAddress);
    }



}