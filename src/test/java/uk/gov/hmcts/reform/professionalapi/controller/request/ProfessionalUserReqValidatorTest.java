package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.Test;

public class ProfessionalUserReqValidatorTest {

    ProfessionalUserReqValidator profUserReqValidator = new ProfessionalUserReqValidator();

    @Test(expected = InvalidRequest.class)
    public void testValidateRequestAllNull() {
        profUserReqValidator
                .validateRequest(null,null,null, null);
    }

    @Test(expected = Test.None.class)
    public void should_validate_valid_email_and_should_not_throw_exception() {

        String[] validEmails = new String[] {
            "shreedhar.lomte@hmcts.net",
            "shreedhar@yahoo.com",
            "Email.100@yahoo.com",
            "email111@email.com",
            "email.100@email.com.au",
            "email@gmail.com.com",
            "email_231_a@email.com",
            "email_100@yahoo-test.ABC.CoM",
            "email-100@yahoo.com",
            "email-100@email.net",
            "email+100@gmail.com",
            "emAil-100@yahoo-test.com",
            "v.green@ashfords.co.uk",
            "j.robinson@timms-law.com",
            "あいうえお@example.com",
            "emAil@1.com",
            "email@.com.my",
            "email123@gmail.",
            "email123@.com",
            "email123@.com.com",
            ".email@email.com",
            "email()*@gmAil.com",
            "eEmail()*@gmail.com",
            "email@%*.com",
            "email..2002@gmail.com",
            "email.@gmail.com",
            "email@email@gmail.com",
            "email@gmail.com.",
            "email..2002@gmail.com@",
            "-email.23@email.com",
            "$email.3@email.com",
            "!email@email.com",
            "+@Adil61371@gmail.com",
            "_email.23@email.com",
            "email.23@-email.com"};

        for (String email : validEmails) {
            profUserReqValidator
                    .validateRequest("1ASDFG2","false", email, null);
        }

    }

    @Test(expected = InvalidRequest.class)
    public void should_validate_valid_email_and_should_throw_exception() {

        String[] validEmails = new String[] {
            "email.com",
            "email@com",
            "@hotmail.com",
            "email@",
            "@"
        };

        for (String email : validEmails) {
            profUserReqValidator
                    .validateRequest("1ASDFG2","false", email, null);
        }
    }
}
