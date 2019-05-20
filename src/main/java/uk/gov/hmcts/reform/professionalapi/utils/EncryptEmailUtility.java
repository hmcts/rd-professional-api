package uk.gov.hmcts.reform.professionalapi.utils;

import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

/**
 * This class generates the MD5 hashed/encrypted string from input string.
 */
@Slf4j
public class EncryptEmailUtility {

    private EncryptEmailUtility(){}

    public static String getMd5ConvertedString(String input) {
        log.info("Input String for encryption: " + input);
        String hashText = null;
        if (null != input) {
            MessageDigest messageDigest = getMessageDigestInstance("MD5");
            byte[] messageDigestBytes = messageDigest.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigestBytes);
            hashText = number.toString(16);
            log.info("Encrypted hashed String: " + hashText);
        }
        return hashText;
    }

    public static MessageDigest getMessageDigestInstance(String encryptionType) {
        try {
            return MessageDigest.getInstance(encryptionType);
        } catch (NoSuchAlgorithmException e) {
            log.error("Encryption failed " + e);
            throw new InvalidRequest("Encryption failed!!");
        }
    }
}
