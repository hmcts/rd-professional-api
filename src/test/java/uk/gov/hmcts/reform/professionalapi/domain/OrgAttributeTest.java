package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.generateRandomUUID;

class OrgAttributeTest {

    private OrgAttribute orgAttribute;
    private final Organisation organisation = new Organisation();

    @BeforeEach
    void setUp() {
        orgAttribute = new OrgAttribute();
        orgAttribute.setId(generateRandomUUID());
        orgAttribute.setKey("test-key");
        orgAttribute.setValue("test-value");
        orgAttribute.setOrganisation(organisation);
    }

    @Test
    void test_orgAttributes() {
        assertThat(orgAttribute.getId()).isNotNull();
        assertThat(orgAttribute.getOrganisation()).isEqualTo(organisation);
        assertThat(orgAttribute.getKey()).isEqualTo("test-key");
        assertThat(orgAttribute.getValue()).isEqualTo("test-value");

    }

}
