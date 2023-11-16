package uk.gov.hmcts.reform.professionalapi.dataload.binder;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BulkCustomerDetailsTest {


    @Test
    void test_bulkCustomerDetails() {

        BulkCustomerDetails bulkCustomerDetails = BulkCustomerDetails.builder()
            .bulkCustomerId("bulkOne")
            .organisationId("org1")
            .pbaNumber("pbaOne")
            .sidamId("sidamone").build();

        assertThat(bulkCustomerDetails.getBulkCustomerId()).isEqualTo("bulkOne");
        assertThat(bulkCustomerDetails.getOrganisationId()).isEqualTo("org1");
        assertThat(bulkCustomerDetails.getPbaNumber()).isEqualTo("pbaOne");
        assertThat(bulkCustomerDetails.getSidamId()).isEqualTo("sidamone");
    }

}
