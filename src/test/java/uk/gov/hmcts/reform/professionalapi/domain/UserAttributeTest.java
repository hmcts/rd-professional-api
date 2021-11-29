package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAttributeTest {

    @Test
    void test_creates_user_attribute_correctly() {
        PrdEnum prdEnum = new PrdEnum();
        ProfessionalUser professionalUser = new ProfessionalUser();

        UserAttribute userAttributeNoArg = new UserAttribute();
        assertThat(userAttributeNoArg).isNotNull();

        UserAttribute userAttribute = new UserAttribute(professionalUser, prdEnum);
        assertThat(userAttribute.getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(userAttribute.getPrdEnum()).isEqualTo(prdEnum);
        assertThat(userAttribute.getId()).isNull();
    }
}
