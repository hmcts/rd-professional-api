package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@ExtendWith(MockitoExtension.class)
class OrganisationEntityResponseTest {

    @Test
    void test_OrganisationEntityResponse() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        OrganisationEntityResponse organisationEntityResponse = new OrganisationEntityResponse(organisation,
                false, false, true);

        assertThat(organisationEntityResponse).isNotNull();
    }

    @Test
    void test_OrganisationEntityResponse_organisation_null() {

        OrganisationEntityResponse organisationEntityResponse = new OrganisationEntityResponse(null,
                false, false, true);

        assertThat(organisationEntityResponse).isNotNull();
        assertThat(organisationEntityResponse.getName()).isNull();
        assertThat(organisationEntityResponse.getOrganisationIdentifier()).isNull();
        assertThat(organisationEntityResponse.getContactInformation()).isNull();
    }
}
