package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrgAttributeResponseTest {

    final String key = "TestKey";

    final String value = "TestValue";

    final UUID id = UUID.randomUUID();

    @Test
    void testGetOrgAttributeResponseResponse() {
        OrgAttribute orgAttributes = new OrgAttribute();
        orgAttributes.setKey(key);
        orgAttributes.setValue(value);
        orgAttributes.setId(id);

        OrgAttributeResponse orgAttributeResponse = new OrgAttributeResponse(orgAttributes);
        assertThat(orgAttributeResponse.getKey()).isEqualTo(key);
        assertThat(orgAttributeResponse.getValue()).isEqualTo(value);
    }
}
