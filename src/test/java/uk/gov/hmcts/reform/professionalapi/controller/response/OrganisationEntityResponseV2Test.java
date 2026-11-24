package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrganisationEntityResponseV2Test {

    @Test
    void test_OrganisationEntityResponse() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        OrganisationEntityResponseV2 organisationEntityResponse = new OrganisationEntityResponseV2(organisation,
                false, false, true, true);

        assertThat(organisationEntityResponse).isNotNull();
    }

    @Test
    void test_OrganisationEntityResponse_organisation_null() {

        OrganisationEntityResponseV2 organisationEntityResponse = new OrganisationEntityResponseV2(null,
                false, false, true,true);

        assertThat(organisationEntityResponse).isNotNull();
        assertThat(organisationEntityResponse.getName()).isNull();
        assertThat(organisationEntityResponse.getOrganisationIdentifier()).isNull();
        assertThat(organisationEntityResponse.getContactInformation()).isNull();
    }
}
