package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationValidationResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ContactInformationEntityResponseTest {
    ContactInformationEntityResponse contactInformationEntityResponse;

    @BeforeEach
    void setUp() {
        contactInformationEntityResponse = new ContactInformationEntityResponse();
        List<ContactInformationValidationResponse> contactInfoValidations = new ArrayList<>();
        contactInformationEntityResponse.setContactInfoValidations(contactInfoValidations);
        contactInformationEntityResponse.setOrganisationIdentifier("Identifier");
    }

    @Test
    void test_contact_information_entiry_response_correctly() {
        assertThat(contactInformationEntityResponse).isNotNull();
        assertThat(contactInformationEntityResponse.getContactInfoValidations()).isNotNull();
        assertThat(contactInformationEntityResponse.getOrganisationIdentifier()).isNotNull();
    }
}
