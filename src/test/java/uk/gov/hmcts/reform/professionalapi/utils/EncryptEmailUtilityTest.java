package uk.gov.hmcts.reform.professionalapi.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

public class EncryptEmailUtilityTest {

    @Test
    public void check_encryptEmailUtility_returns_hashed_string() {
        String md5ConvertedString = EncryptEmailUtility.getMd5ConvertedString("some@org.com");
        assertThat(md5ConvertedString).isNotNull();
        assertThat(md5ConvertedString.length()).isEqualTo(32);
    }

    @Test
    public void check_encryptEmailUtility_returns_null_string_when_input_is_null() {
        String md5ConvertedString = EncryptEmailUtility.getMd5ConvertedString(null);
        assertThat(md5ConvertedString).isNull();
    }

    @Test
    public void check_encryptEmailUtility_throws_error_when_encryption_type_is_invalid() throws NoSuchAlgorithmException {

        assertThatThrownBy(() -> EncryptEmailUtility.getMessageDigestInstance("MD56"))
                .hasMessage("Encryption failed!!")
                .isExactlyInstanceOf(InvalidRequest.class);
    }
}
