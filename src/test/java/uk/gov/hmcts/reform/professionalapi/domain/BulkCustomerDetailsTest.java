package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BulkCustomerDetailsTest {

    private BulkCustomerDetails bulkCustomerDetails;



    @BeforeEach
    void setUp() {


        bulkCustomerDetails = new BulkCustomerDetails();
        bulkCustomerDetails.setBulkCustomerId("6601e79e-3169-461d-a751-59a33a5sdfd");
        bulkCustomerDetails.setId(UUID.randomUUID());
        bulkCustomerDetails.setPbaNumber("pba-1234567");
        bulkCustomerDetails.setSidamId("6601e79e-3169-461d-a751-59a33a5sdfd");
        bulkCustomerDetails.setOrganisationId("orgId");
        Organisation organisation = new Organisation("some-name", OrganisationStatus.ACTIVE,
            "sra-id", "company-number", Boolean.FALSE, "company-url");
        bulkCustomerDetails.setOrganisation(organisation);

    }

    @Test
    void test_bulk_customer_details() {
        assertThat(bulkCustomerDetails.getBulkCustomerId()).isEqualTo("6601e79e-3169-461d-a751-59a33a5sdfd");
        assertThat(bulkCustomerDetails.getPbaNumber()).isEqualTo("pba-1234567");
        assertThat(bulkCustomerDetails.getId()).isNotNull();
        assertThat(bulkCustomerDetails.getSidamId()).isEqualTo("6601e79e-3169-461d-a751-59a33a5sdfd");
        assertThat(bulkCustomerDetails.getOrganisation().getName()).isEqualTo("some-name");
        assertThat(bulkCustomerDetails.getOrganisation().getSraId())
            .isEqualTo("sra-id");
        assertThat(bulkCustomerDetails.getOrganisation().getCompanyNumber())
            .isEqualTo("company-number");
        assertThat(bulkCustomerDetails.getOrganisation().getCompanyUrl())
            .isEqualTo("company-url");
        assertThat(bulkCustomerDetails.getOrganisationId())
            .isEqualTo("orgId");
    }
}
