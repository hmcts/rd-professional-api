package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationMinimalInfoResponseTest {

    String name = "Organisation Name";
    String organisationIdentifier = UUID.randomUUID().toString();
    ContactInformation contactInformation = new ContactInformation();
    List<ContactInformation> contactInformations = Arrays.asList(contactInformation);
    Organisation organisationMock = mock(Organisation.class);

    @BeforeEach
    void setUp() {
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setUprn("");
        when(organisationMock.getName()).thenReturn(name);
        when(organisationMock.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(organisationMock.getContactInformation()).thenReturn(contactInformations);
    }

    @Test
    void testOrganisationMinimalInfoResponse_WithAddressTrue() {
        OrganisationMinimalInfoResponse response = new OrganisationMinimalInfoResponse(organisationMock, true);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getOrganisationIdentifier()).isEqualTo(organisationIdentifier);
        assertThat(response.getContactInformation().get(0).getAddressLine1())
                .isEqualTo(contactInformations.get(0).getAddressLine1());
        assertThat(response.getContactInformation().get(0).getUprn())
                .isEqualTo(contactInformations.get(0).getUprn());
    }

    @Test
    void testOrganisationMinimalInfoResponse_WithAddressFalse() {
        OrganisationMinimalInfoResponse response = new OrganisationMinimalInfoResponse(organisationMock, false);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getOrganisationIdentifier()).isEqualTo(organisationIdentifier);
        assertThat(response.getContactInformation()).isNull();
    }
}
