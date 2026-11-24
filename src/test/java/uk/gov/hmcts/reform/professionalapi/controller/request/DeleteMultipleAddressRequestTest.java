package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteMultipleAddressRequestTest {

    @Test
    void testDeleteMultipleAddressRequestTest() {
        var addressId = "test01";
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();
        deleteMultipleAddressRequest.setAddressId(addressId);

        assertThat(deleteMultipleAddressRequest.getAddressId()).isEqualTo(addressId);

    }
}
