package uk.gov.hmcts.reform.professionalapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.constants.PrdEnumType;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<UserAttribute> addUserAttributesToSuperUser(ProfessionalUser user,
                                                                             List<UserAttribute> attributes) {
        prdEnumService.findAllPrdEnums().stream()
                .filter(prdEnum -> isValidEnumType(prdEnum.getPrdEnumId().getEnumType()))
                .map(prdEnum -> {
                    PrdEnum e = new PrdEnum(prdEnum.getPrdEnumId(), prdEnum.getEnumName(),
                            prdEnum.getEnumDescription());
                    return new UserAttribute(user, e);
                }).forEach(attributes::add);

        if (!CollectionUtils.isEmpty(attributes)) {
            userAttributeRepository.saveAll(attributes);
        }

        return attributes;

    }

    public boolean isValidEnumType(String enumType) {
        return enumType.equalsIgnoreCase(PrdEnumType.SIDAM_ROLE.name())
                || enumType.equalsIgnoreCase(PrdEnumType.ADMIN_ROLE.name());
    }

    public void updateUser(ProfessionalUser existingAdmin, ProfessionalUser newAdmin) {
        List<UserAttribute> userAttributes = userAttributeRepository
            .fetchByProfessionalUserIdAndPrdEnumType(existingAdmin.getId());
        if (userAttributes.size() > 0) {
            userAttributes.forEach(userAttribute -> {
                userAttribute.setProfessionalUser(newAdmin);
                userAttributeRepository.save(userAttribute);
            });
        } else {
            throw new InvalidRequest("The requested user's attributes not found or is not an admin");
        }
    }
}
