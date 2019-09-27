package uk.gov.hmcts.reform.professionalapi.service.impl;

import feign.FeignException;
import feign.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

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
        return professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(email));
    }

    public ProfessionalUser findProfessionalUserProfileByEmailAddress(String email) {
        ProfessionalUser user = professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(email));

        if (user == null || user.getOrganisation().getStatus() != OrganisationStatus.ACTIVE) {
            throw new EmptyResultDataAccessException(1);
        }

        return RefDataUtil.getSingleUserIdFromUserProfile(user, userProfileFeignClient, true);
    }

    @Override
    public ProfessionalUser findProfessionalUserById(UUID id) {
        Optional<ProfessionalUser> professionalUser = professionalUserRepository.findById(id);
        if (professionalUser.isPresent()) {
            return professionalUser.get();
        }
        return null;
    }

    @Override
    public ResponseEntity findProfessionalUsersByOrganisation(Organisation organisation, String showDeleted, boolean rolesRequired, String status) {
        ResponseEntity responseEntity;
        RetrieveUserProfilesRequest retrieveUserProfilesRequest = generateRetrieveUserProfilesRequest(organisation);

        try (Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted, Boolean.toString(rolesRequired))) {

            Class clazz = response.status() > 300 ? ErrorResponse.class : ProfessionalUsersEntityResponse.class;
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, clazz);

        }  catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "Error while invoking UP");
        }

        if (!StringUtils.isBlank(status)) {
            log.info("Filtering users by status: " + status);

            ProfessionalUsersEntityResponse professionalUsersEntityResponse = RefDataUtil.filterUsersByStatus(responseEntity, status);
            return responseEntity.status(responseEntity.getStatusCode()).headers(responseEntity.getHeaders()).body(professionalUsersEntityResponse);
        }

        return responseEntity;
    }

    private RetrieveUserProfilesRequest generateRetrieveUserProfilesRequest(Organisation organisation) {
        List<ProfessionalUser> professionalUsers;
        List<String> usersId = new ArrayList<>();

        professionalUsers = professionalUserRepository.findByOrganisation(organisation);

        professionalUsers.forEach(user -> usersId.add(user.getUserIdentifier()));

        return new RetrieveUserProfilesRequest(usersId);
    }

    @Override
    public ProfessionalUser persistUser(ProfessionalUser updatedProfessionalUser) {
        return professionalUserRepository.save(updatedProfessionalUser);
    }

    @Override
    public ModifyUserRolesResponse modifyRolesForUser(ModifyUserProfileData modifyUserProfileData, String userId) {
        ModifyUserRolesResponse modifyUserRolesResponse;
        try (Response response =  userProfileFeignClient.modifyUserRoles(modifyUserProfileData, userId)) {

            Class clazz = ModifyUserRolesResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseHelper.toResponseEntity(response, clazz);

            if (response.status() > 300) {
                ModifyUserRolesResponse userProfileErrorResponse = (ModifyUserRolesResponse) responseResponseEntity.getBody();
            }
            modifyUserRolesResponse = (ModifyUserRolesResponse)responseResponseEntity.getBody();
        }  catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "Error while invoking modifyRoles API in UP");
        }
        return modifyUserRolesResponse;
    }
}