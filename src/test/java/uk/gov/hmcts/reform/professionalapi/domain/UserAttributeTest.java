package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

public class UserAttributeTest {

    @Test
    public void creates_user_attribute_correctly() {

        PrdEnum prdEnum = mock(PrdEnum.class);

        ProfessionalUser professionalUser = mock(ProfessionalUser.class);

        UserAttribute userAttribute = new UserAttribute(professionalUser, prdEnum);

        UserAttribute userAttributeNoArg = new UserAttribute();

        assertThat(userAttributeNoArg).isNotNull();

        assertThat(userAttribute.getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(userAttribute.getPrdEnum()).isEqualTo(prdEnum);
        assertThat(userAttribute.getId()).isNull();

    }
}
