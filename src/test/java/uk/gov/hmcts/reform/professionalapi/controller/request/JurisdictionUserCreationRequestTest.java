package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;

public class JurisdictionUserCreationRequestTest {

    @Test
    public void test_JurisdictionUserCreationRequest() {
        Jurisdiction jurisdiction = new Jurisdiction();
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(jurisdiction);

        String id = UUID.randomUUID().toString();

        JurisdictionUserCreationRequest jurisdictionUserCreationRequest = new JurisdictionUserCreationRequest(null, null);

        jurisdictionUserCreationRequest.setId(id);
        jurisdictionUserCreationRequest.setJurisdictions(jurisdictions);

        assertThat(jurisdictionUserCreationRequest.getId()).isEqualTo(id);
        assertThat(jurisdictionUserCreationRequest.getJurisdictions()).isEqualTo(jurisdictions);
    }

}
