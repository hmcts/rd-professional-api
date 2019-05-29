package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;

import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface LegacyPbaAccountService {

    List<String> findLegacyPbaAccountByUserEmail(ProfessionalUser professionalUser);

}
