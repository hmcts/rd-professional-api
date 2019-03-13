package uk.gov.hmcts.reform.professionalapi.domain.service;

import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalRefData;

public interface ResourceRetriever<T extends ProfessionalRefData> {

    T getResource(String id);

}
