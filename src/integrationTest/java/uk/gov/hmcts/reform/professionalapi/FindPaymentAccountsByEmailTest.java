package uk.gov.hmcts.reform.professionalapi;

import lombok.AllArgsConstructor;
import org.junit.Before;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

@AllArgsConstructor
public class FindPaymentAccountsByEmailTest extends Service2ServiceEnabledIntegrationTest {

    private OrganisationRepository organisationRepository;
    private ProfessionalUserRepository professionalUserRepository;
    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        professionalUserRepository.deleteAll();
        organisationRepository.deleteAll();
    }

}
