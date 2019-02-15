package uk.gov.hmcts.reform.sysrefdataapi.domain.service;

import uk.gov.hmcts.reform.sysrefdataapi.domain.entities.SystemRefData;

public interface ResourceRetriever<T extends SystemRefData> {

    T getResource(String id);

}
