package uk.gov.hmcts.reform.professionalapi.provider;


import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.internal.BulkCustomerDetailsInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.BulkCustomerOrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.repository.BulkCustomerDetailsRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;


@Provider("referenceData_bulkCustomerDetails")
@ContextConfiguration(classes = {BulkCustomerDetailsInternalController.class})
@Import(BulkCustomerDetailsProviderTestConfiguration.class)
public class BulkCustomerDetailsProviderTest extends MockMvcProviderTest {

    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";
    public static final String PBA_NUMBER = "PBA1234567";


    @Autowired
    BulkCustomerDetailsInternalController bulkCustomerDetailsInternalController;

    @MockBean
    OrganisationServiceImpl organisationService;

    @MockBean
    BulkCustomerDetailsRepository bulkCustomerDetailsRepository;


    @Override
    void setController() {
        testTarget.setControllers(bulkCustomerDetailsInternalController);
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(
                bulkCustomerDetailsInternalController);
        if (nonNull(context)) {
            context.setTarget(testTarget);
        }

    }


    @State("Organisation Details for Bulk Customer")
    public void toReturnOrganisationalServiceDetails() throws JsonProcessingException {
        BulkCustomerDetails bulkCustomerDetails = getOrganisationDetailsForBulkCustomer();

        when(organisationService.retrieveOrganisationDetailsForBulkCustomer(anyString(), anyString()))
                            .thenReturn(new BulkCustomerOrganisationsDetailResponse(bulkCustomerDetails));
        when(bulkCustomerDetailsRepository.findByBulkCustomerId(anyString(), anyString()))
                            .thenReturn(bulkCustomerDetails);

    }

    private BulkCustomerRequest getbulkCustomerRequest() {
        return BulkCustomerRequest.abulkCustomerRequest()
                        .bulkCustomerId("BulkcustId")
                        .idamId("sidamId")
                        .build();

    }

    private BulkCustomerDetails getOrganisationDetailsForBulkCustomer() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        organisation.setId(UUID.fromString("c5e5c75d-cced-4e57-97c8-e359ce33a855"));


        BulkCustomerDetails bulkCustomerDetails = new BulkCustomerDetails();
        bulkCustomerDetails.setOrganisation(organisation);
        bulkCustomerDetails.setSidamId("sidamId");
        bulkCustomerDetails.setPbaNumber(PBA_NUMBER);
        bulkCustomerDetails.setId(UUID.randomUUID());
        bulkCustomerDetails.setBulkCustomerId("BulkcustId");

        return bulkCustomerDetails;
    }
}


