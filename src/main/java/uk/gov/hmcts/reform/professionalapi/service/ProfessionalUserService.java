package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.List;

@Service
public interface ProfessionalUserService {

    public ProfessionalUser findProfessionalUserByEmailAddress(String email);

    public List<ProfessionalUser> findProfessionalUsersByOrganisation(Organisation existingOrganisation, boolean showDeleted);
}
