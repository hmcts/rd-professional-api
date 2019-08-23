package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.persistence.SuperUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.SuperUserService;

public class SuperUserServiceImp implements SuperUserService {

    @Autowired
    private SuperUserRepository superUserRepository;

    public SuperUser persistSuperUser(SuperUser superUser) {
        SuperUser user = superUserRepository.save(superUser);
        return user;
    }
}

