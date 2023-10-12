package uk.gov.hmcts.reform.professionalapi.dataload.mapper;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.dataload.binder.BulkCustomerDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.trim;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BulkCustomerDetailsMapperTest {


    @Spy
    BulkCustomerDetailsMapper bulkCustomerDetailsMapper = new BulkCustomerDetailsMapper();

    @Test
    void test_BulkCustomerDetailsMapper() {

        BulkCustomerDetails bulkCustomerDetails = BulkCustomerDetails.builder()
            .bulkCustomerId("bulkOne")
            .organisationId("046b6c7f-0b8a-43b9-b35d-6489e6daee44")
            .pbaNumber("pbaOne")
            .sidamId("sidamone").build();
        var expected = new HashMap<String, Object>();
        expected.put("organisation_id", trim(bulkCustomerDetails.getOrganisationId()));
        expected.put("bulk_customer_id", trim(bulkCustomerDetails.getBulkCustomerId()));
        expected.put("sidam_id", trim(bulkCustomerDetails.getSidamId()));
        expected.put("pba_number", trim(bulkCustomerDetails.getPbaNumber()));
        Map<String, Object> actual = bulkCustomerDetailsMapper.getMap(bulkCustomerDetails);
        verify(bulkCustomerDetailsMapper, times(1)).getMap(bulkCustomerDetails);
        assertThat(actual.get("organisation_id")).isEqualTo(UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee44"));
    }
}
