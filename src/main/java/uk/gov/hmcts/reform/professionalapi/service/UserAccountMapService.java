package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;

import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

public interface UserAccountMapService {

    void deleteUserAccountMapsByIds(List<UserAccountMapId> accountsToDelete);
}
