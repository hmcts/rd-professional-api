package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SingletonOrgTypeTest {

    private SingletonOrgType singletonOrgType;

    @BeforeEach
    void setup() {
        singletonOrgType = new SingletonOrgType();
        singletonOrgType.setOrgType("testOrgType");
        singletonOrgType.setId(1);
    }

    @Test
    void test_singleton_org_type_created_coretly() {
        assertThat(singletonOrgType.getOrgType()).isEqualTo("testOrgType");
        assertEquals(1, singletonOrgType.getId());
    }
}
