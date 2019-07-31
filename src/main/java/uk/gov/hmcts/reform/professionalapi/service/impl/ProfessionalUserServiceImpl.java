package uk.gov.hmcts.reform.professionalapi.service.impl;

import feign.FeignException;
import feign.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Service
@Slf4j
public class ProfessionalUserServiceImpl implements ProfessionalUserService {

    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;

    UserAttributeServiceImpl userAttributeService;

    UserProfileFeignClient userProfileFeignClient;

    @Autowired
    public ProfessionalUserServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            UserAttributeRepository userAttributeRepository,
            PrdEnumRepository prdEnumRepository,
            UserAttributeServiceImpl userAttributeService,
            UserProfileFeignClient userProfileFeignClient) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.userAttributeRepository = userAttributeRepository;
        this.prdEnumRepository = prdEnumRepository;
        this.userAttributeService = userAttributeService;
        this.userProfileFeignClient = userProfileFeignClient;
    }

    @Transactional
    @Override
    public NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles, List<PrdEnum> prdEnumList) {

        ProfessionalUser persistedNewUser = persistUser(newUser);

        userAttributeService.addUserAttributesToUser(persistedNewUser, roles, prdEnumList);

        return new NewUserResponse(persistedNewUser);
    }

    public ProfessionalUser findProfessionalUserByEmailAddress(String email) {
        return professionalUserRepository.findByEmailAddress(PbaAccountUtil.removeAllSpaces(email));
    }

    public ProfessionalUser findProfessionalUserProfileByEmailAddress(String email) {
        ProfessionalUser user = professionalUserRepository.findByEmailAddress(PbaAccountUtil.removeAllSpaces(email));

        if (user == null || user.getOrganisation().getStatus() != OrganisationStatus.ACTIVE) {
            log.info("The organisation the user belongs to must be active");
            throw new EmptyResultDataAccessException(1);
        }

        return PbaAccountUtil.getSingleUserIdFromUserProfile(user, userProfileFeignClient, true);
    }

    @Override
    public ResponseEntity findProfessionalUsersByOrganisation(Organisation organisation, String showDeleted) {
        log.info("Into  ProfessionalService for get all users for organisation");
        List<ProfessionalUser> professionalUsers;
        List<UUID> usersId = new ArrayList<>();

        ResponseEntity responseResponseEntity;

        professionalUsers = professionalUserRepository.findByOrganisation(organisation);

        professionalUsers.forEach(user -> usersId.add(user.getUserIdentifier()));

        RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(usersId);

        try {
            Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted);

            Class clazz = response.status() > 300 ? ErrorResponse.class : ProfessionalUsersEntityResponse.class;
            responseResponseEntity = JsonFeignResponseHelper.toResponseEntity(response, clazz);

        }  catch (FeignException ex) {

            throw new RuntimeException();
        }

        return responseResponseEntity;
    }

    @Override
    public ProfessionalUser persistUser(ProfessionalUser updatedProfessionalUser) {
        log.info("Persisting user with emailId: " + updatedProfessionalUser.getEmailAddress());
        return professionalUserRepository.save(updatedProfessionalUser);
    }
}