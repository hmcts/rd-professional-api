package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

import java.util.List;
import java.util.UUID;

public interface SuperUserService {

    SuperUser persistSuperUser(SuperUser superUser);
}

