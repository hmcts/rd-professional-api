package uk.gov.hmcts.reform.professionalapi.domain;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrgAttributeTest {

    private OrgAttribute orgAttribute;

    private final Organisation organisation = new Organisation();

    @BeforeEach
    void setUp() {
        orgAttribute = new OrgAttribute();
        orgAttribute.setId(UUID.randomUUID());
        orgAttribute.setKey("TestKey");
        orgAttribute.setValue("TestValue");
        orgAttribute.setOrganisation(organisation);
    }

    @Test
    void test_creates_orgAttribute_correctly() {
        assertThat(orgAttribute.getId()).isNotNull();
        assertThat(orgAttribute.getOrganisation()).isEqualTo(organisation);
        assertThat(orgAttribute.getValue()).isEqualTo("TestValue");
        assertThat(orgAttribute.getKey()).isEqualTo("TestKey");
    }
}
