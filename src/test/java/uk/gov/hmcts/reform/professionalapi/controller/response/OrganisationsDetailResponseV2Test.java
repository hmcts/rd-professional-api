package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrganisationsDetailResponseV2Test {

    @Test
    void test_OrganisationsDetailResponse() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        OrganisationsDetailResponseV2 organisationsDetailResponseResponse
                = new OrganisationsDetailResponseV2(singletonList(organisation),
                false, false, true,true);

        assertThat(organisationsDetailResponseResponse).isNotNull();
    }
}