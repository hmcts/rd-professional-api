package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
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

        Field f = contactInformationResponse.getClass().getDeclaredField("addressLine1");
        f.setAccessible(true);
        address1 = (String) f.get(contactInformationResponse);

        assertEquals(address1, expectAddress1);

    }

}