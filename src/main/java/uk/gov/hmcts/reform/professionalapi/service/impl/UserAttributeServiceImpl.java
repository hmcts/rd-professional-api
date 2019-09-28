package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

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
    public void addUserAttributesToUser(ProfessionalUser newUser, List<String> userRoles, List<PrdEnum> prdEnums) {

        List<UserAttribute> userAttributes =  new ArrayList<>();

        userRoles.forEach(role -> {
            for (PrdEnum prdEnum : prdEnums) {
                if (prdEnum.getEnumName().equals(role)) {
                    PrdEnum newPrdEnum = new PrdEnum(
                            prdEnum.getPrdEnumId(),
                            RefDataUtil.removeEmptySpaces(prdEnum.getEnumName()),
                            RefDataUtil.removeEmptySpaces(prdEnum.getEnumDescription()));

                    UserAttribute userAttribute = new UserAttribute(newUser, newPrdEnum);
                    userAttributes.add(userAttribute);
                }
            }
        });

        if (!CollectionUtils.isEmpty(userAttributes)) {

            userAttributeRepository.saveAll(userAttributes);
        }

    }

}
