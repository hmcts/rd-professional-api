package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UserAttributeTest {

    @Test
    public void creates_user_attribute_correctly() {
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
