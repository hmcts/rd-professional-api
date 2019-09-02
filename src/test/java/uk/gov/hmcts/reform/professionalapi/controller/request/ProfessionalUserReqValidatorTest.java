package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.Test;

public class ProfessionalUserReqValidatorTest {

    ProfessionalUserReqValidator profUserReqValidator = new ProfessionalUserReqValidator();

    @Test(expected = InvalidRequest.class)
    public void testValidateRequestAllNull() {
        profUserReqValidator
                .validateRequest(null,null,null, null);
    }

    @Test
    public void testValidateRequestNoneNull() {
        profUserReqValidator
                .validateRequest("ordId","true","some@email.com", "");
    }
}
