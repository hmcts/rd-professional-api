package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void has_mandatory_fields_specified_not_null() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest(null, null, null, null, "false",
                        null, null, null, null,
                        null);

        Set<ConstraintViolation<OrganisationCreationRequest>> violations = validator
                .validate(organisationCreationRequest);

        assertThat(violations).hasSize(3);
    }

    @Test
    void test_OrganisationCreationRequest() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest("test", "PENDING", null, "sra-id", "false",
                        "number02", "company-url", null, null,
                        null);

        organisationCreationRequest.setStatus("ACTIVE");
        organisationCreationRequest.setStatusMessage("In review");

        assertThat(organisationCreationRequest.getName()).isEqualTo("test");
        assertThat(organisationCreationRequest.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisationCreationRequest.getStatusMessage()).isEqualTo("In review");
        assertThat(organisationCreationRequest.getSraId()).isEqualTo("sra-id");
        assertThat(organisationCreationRequest.getSraRegulated()).isEqualTo("false");
    }

    @Test
    void test_OrganisationCreationRequestBuilder() {
        String name = "name";
        String status = "status";
        String statusMessage = "statusMessage";
        String sraId = "sraId";
        String sraRegulated = "sraRegulated";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        OrganisationCreationRequest organisationCreationRequest = OrganisationCreationRequest
                .anOrganisationCreationRequest()
                .name(name)
                .status(status)
                .statusMessage(statusMessage)
                .sraId(sraId)
                .sraRegulated(sraRegulated)
                .companyNumber(companyNumber)
                .companyUrl(companyUrl)
                .build();

        assertThat(organisationCreationRequest.getName()).isEqualTo(name);
        assertThat(organisationCreationRequest.getStatus()).isEqualTo(status);
        assertThat(organisationCreationRequest.getStatusMessage()).isEqualTo(statusMessage);
        assertThat(organisationCreationRequest.getSraId()).isEqualTo(sraId);
        assertThat(organisationCreationRequest.getSraRegulated()).isEqualTo(sraRegulated);
        assertThat(organisationCreationRequest.getCompanyNumber()).isEqualTo(companyNumber);
        assertThat(organisationCreationRequest.getCompanyUrl()).isEqualTo(companyUrl);
    }
}