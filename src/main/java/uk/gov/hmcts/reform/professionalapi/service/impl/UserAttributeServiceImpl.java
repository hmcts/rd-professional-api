package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;

@Service
@Slf4j
public class UserAttributeServiceImpl implements UserAttributeService {
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;

    PrdEnumServiceImpl prdEnumService;

    @Autowired
    public UserAttributeServiceImpl(
            UserAttributeRepository userAttributeRepository,
            PrdEnumRepository prdEnumRepository,
            PrdEnumServiceImpl prdEnumService) {
        this.userAttributeRepository = userAttributeRepository;
        this.prdEnumRepository = prdEnumRepository;
        this.prdEnumService = prdEnumService;
    }

    @Override
    public void addUserAttributesToUser(ProfessionalUser newUser, List<String> userRoles) {
        List<PrdEnum> prdEnums = prdEnumService.findAllPrdEnums();
        List<String> verifiedRoles = UserCreationRequestValidator.contains(userRoles, prdEnums);

        if (verifiedRoles.isEmpty()) {
            throw new InvalidRequest("400");
        } else {
            verifiedRoles.forEach(role -> {
                for (PrdEnum prdEnum : prdEnums) {
                    if (prdEnum.getEnumName().equals(role)) {
                        PrdEnum newPrdEnum = new PrdEnum(prdEnum.getPrdEnumId(), prdEnum.getEnumName(), prdEnum.getEnumDescription());
                        UserAttribute userAttribute = new UserAttribute(newUser, newPrdEnum);
                        userAttributeRepository.save(userAttribute);
                    }
                }
            });
        }
    }

}
