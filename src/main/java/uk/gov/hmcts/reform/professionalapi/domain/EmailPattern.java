package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailPattern {

    private Pattern pattern;
    private Matcher matcher;

    public static boolean isEmailValid(String email) {
        Pattern p = Pattern.compile(EMAIL_PATTERN);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private static final String EMAIL_PATTERN =
        "^[A-Za-z0-9\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String[] ValidEmails = new String[] {
        "email@yahoo.com", "email-100@yahoo.com", "Email.100@yahoo.com", "email111@email.com", "email-100@email.net", "email.100@email.com.au", "emAil@1.com", "email@gmail.com.com", "email+100@gmail.com", "emAil-100@yahoo-test.com", "email_100@yahoo-test.ABC.CoM"
        };

    private static final String[] InvalidEmails = new String[] {
        "あいうえお@example.com", "email@111", "email", "email@.com.my", "email123@gmail.", "email123@.com", "email123@.com.com",
        ".email@email.com", "email()*@gmAil.com", "eEmail()*@gmail.com", "email@%*.com", "email..2002@gmail.com",
        "email.@gmail.com", "email@email@gmail.com", "email@gmail.com.", "email..2002@gmail.com@",
        "-email.23@email.com", "$email.3@email.com", "!email@email.com"
    };

    public static void main(String[] args) {

        for (String emails : ValidEmails) {
            System.out.println("Email::" + emails + "::" + isEmailValid(emails));
        }

        for (String emails2 : InvalidEmails) {
            System.out.println("Email::" + emails2 + "::" + isEmailValid(emails2));
        }
    }
}
