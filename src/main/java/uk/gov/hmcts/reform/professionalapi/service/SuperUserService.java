package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

public interface SuperUserService {

    SuperUser persistSuperUser(SuperUser superUser);
}

