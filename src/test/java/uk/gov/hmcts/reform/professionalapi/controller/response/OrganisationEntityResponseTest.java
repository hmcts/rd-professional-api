package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationEntityResponseTest {

    @Test
    public void test_OrganisationEntityResponse() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        OrganisationEntityResponse organisationEntityResponse = new OrganisationEntityResponse(organisation,
                false);

        assertThat(organisationEntityResponse).isNotNull();
    }
}
