package uk.gov.hmcts.reform.professionalapi.domain.service;

import uk.gov.hmcts.reform.professionalapi.domain.entities.SystemRefData;

public interface ResourceRetriever<T extends SystemRefData> {

    T getResource(String id);

}
