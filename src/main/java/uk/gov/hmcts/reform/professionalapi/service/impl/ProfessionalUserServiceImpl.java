package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserServiceI;

@Service
@Slf4j
public class ProfessionalUserServiceImpl implements ProfessionalUserServiceI {

    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;

    @Autowired
    public ProfessionalUserServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            UserAttributeRepository userAttributeRepository,
            PrdEnumRepository prdEnumRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.userAttributeRepository = userAttributeRepository;
        this.prdEnumRepository = prdEnumRepository;
    }

    @Override
    public OrganisationResponse addNewUserToAnOrganisation(NewUserCreationRequest newUserCreationRequest, UUID organisationIdentifier) {
        Organisation theOrganisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        ProfessionalUser newUser = new ProfessionalUser(
                newUserCreationRequest.getFirstName(),
                newUserCreationRequest.getLastName(),
                newUserCreationRequest.getEmail(),
                newUserCreationRequest.getStatus(),
                theOrganisation);

        ProfessionalUser persistedNewUser = professionalUserRepository.save(newUser);

        addUserAttributesToUser(persistedNewUser, newUserCreationRequest.getRoles());

        theOrganisation.addProfessionalUser(persistedNewUser);

        OrganisationResponse organisationResponse = new OrganisationResponse(theOrganisation);
        return organisationResponse;
    }

    private void  addUserAttributesToUser(ProfessionalUser newUser, List<String> userRoles) {
        List<PrdEnum> prdEnums = findAllPrdEnums();

        List<String> verifiedRoles = UserCreationRequestValidator.contains(userRoles, prdEnums);

        if (verifiedRoles.isEmpty()) {
            throw new InvalidRequest("400");
        } else {
            addPrdEnumToUserAttribute(newUser, verifiedRoles);
        }
    }

    private void addPrdEnumToUserAttribute(ProfessionalUser newUser, List<String> verifiedRoles) {
        List<PrdEnum> prdEnumList = findAllPrdEnums();

        verifiedRoles.forEach(role -> {
            for (PrdEnum prdEnum : prdEnumList) {
                if (prdEnum.getEnumName().equals(role)) {
                    PrdEnum newPrdEnum = new PrdEnum(prdEnum.getPrdEnumId(), prdEnum.getEnumName(), prdEnum.getEnumDescription());
                    UserAttribute userAttribute = new UserAttribute(newUser, newPrdEnum);
                    userAttributeRepository.save(userAttribute);
                }
            }
        });
    }

    public List<PrdEnum> findAllPrdEnums() {
        List<PrdEnum> prdEnums = prdEnumRepository.findAll();
        return prdEnums;
    }
}
