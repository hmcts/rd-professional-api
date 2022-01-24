package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteMultipleAddressRequestTest {

    @Test
    void testDeleteMultipleAddressRequestTest() {
        var addressId = Set.of("test01","test02");
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();
        deleteMultipleAddressRequest.setAddressId(addressId);

        assertThat(deleteMultipleAddressRequest.getAddressId().size()).isEqualTo(2);
        assertThat(deleteMultipleAddressRequest.getAddressId().containsAll(addressId));

    }
}
