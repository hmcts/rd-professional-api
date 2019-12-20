package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class JurisdictionUserCreationRequestTest {

    @Test
    public void test_JurisdictionUserCreationRequest() {
        Jurisdiction jurisdictionMock = mock(Jurisdiction.class);
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(jurisdictionMock);

        String id = UUID.randomUUID().toString();

        JurisdictionUserCreationRequest jurisdictionUserCreationRequest = new JurisdictionUserCreationRequest(null, null);

        jurisdictionUserCreationRequest.setId(id);
        jurisdictionUserCreationRequest.setJurisdictions(jurisdictions);

        assertThat(jurisdictionUserCreationRequest.getId()).isEqualTo(id);
        assertThat(jurisdictionUserCreationRequest.getJurisdictions()).isEqualTo(jurisdictions);
    }

}
