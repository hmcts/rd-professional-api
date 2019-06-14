package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.junit.Assert.*;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;





public class ContactInformationResponseTest {

    @Test
    public void testGetContactInformationResponse() throws Exception {
        String expectAddress1 = "someAddress1";
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(expectAddress1);
        ContactInformationResponse contactInformationResponse = new ContactInformationResponse(contactInformation);

        String address1 = "";

    }

}