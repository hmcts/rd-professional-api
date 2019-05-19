package uk.gov.hmcts.reform.professionalapi.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;

/**
 * This class generates the MD5 hashed/encrypted string from input string.
 */

@Slf4j
public class MD5Converter {

    private MD5Converter(){}

    public static String getMd5ConvertedString(String input) {
        log.info("Input String for MD5 conversion: " + input);
        if (input != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());
                BigInteger number = new BigInteger(1, messageDigest);
                String hashText = number.toString(16);
                log.info("MD5 converted hashed String: " + hashText);
                return hashText;
            } catch (NoSuchAlgorithmException e) {
                log.error("Not able to convert input string to MD5 hashed string" + e);
            }
        }
        return null;
    }
}
