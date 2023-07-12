package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrgAttributesTest {

    private OrgAttributes orgAttributes;

    private final Organisation organisation = new Organisation();

    @BeforeEach
    void setUp() {
        orgAttributes = new OrgAttributes();
        orgAttributes.setId(UUID.randomUUID());
        orgAttributes.setKey("TestKey");
        orgAttributes.setValue("TestValue");
        orgAttributes.setOrganisation(organisation);
    }

    @Test
    void test_creates_orgAttributes_correctly() {
        assertThat(orgAttributes.getId()).isNotNull();
        assertThat(orgAttributes.getOrganisation()).isEqualTo(organisation);
        assertThat(orgAttributes.getValue()).isEqualTo("TestValue");
        assertThat(orgAttributes.getKey()).isEqualTo("TestKey");
    }

}
