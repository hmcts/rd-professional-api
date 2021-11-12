package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@ExtendWith(MockitoExtension.class)
class OrganisationsDetailResponseTest {

    @Test
    void test_OrganisationsDetailResponse() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        OrganisationsDetailResponse organisationsDetailResponseResponse
                = new OrganisationsDetailResponse(singletonList(organisation), false);

        assertThat(organisationsDetailResponseResponse).isNotNull();
    }
}
