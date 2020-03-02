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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseUtil;
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

    @Override
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
        return professionalUser.orElse(null);
    }

    @Override
    public ResponseEntity findProfessionalUsersByOrganisationWithPageable(Organisation organisation, String showDeleted, boolean rolesRequired, String status, Pageable pageable) {
        Page<ProfessionalUser> pagedProfessionalUsers = getPagedListOfUsers(organisation, pageable);

        ResponseEntity responseEntity = retrieveUserProfiles(generateRetrieveUserProfilesRequest(pagedProfessionalUsers.getContent()), showDeleted, rolesRequired, status);

        HttpHeaders headers = RefDataUtil.generateResponseEntityWithPaginationHeader(pageable, pagedProfessionalUsers, responseEntity);

        return ResponseEntity.status(responseEntity.getStatusCode()).headers(headers).body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity findProfessionalUsersByOrganisation(Organisation organisation, String showDeleted, boolean rolesRequired, String status) {
        List<ProfessionalUser> professionalUsers = professionalUserRepository.findByOrganisation(organisation);

        if (professionalUsers.isEmpty()) {
            throw new ResourceNotFoundException("No Users were found for the given organisation");
        }

        return retrieveUserProfiles(generateRetrieveUserProfilesRequest(professionalUsers), showDeleted, rolesRequired, status);
    }

    private ResponseEntity retrieveUserProfiles(RetrieveUserProfilesRequest retrieveUserProfilesRequest, String showDeleted, boolean rolesRequired, String status) {
        ResponseEntity responseEntity;

        try (Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted, Boolean.toString(rolesRequired))) {

            Class clazz = response.status() > 300 ? ErrorResponse.class : ProfessionalUsersEntityResponse.class;
            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "Error while invoking UP");
        }

        if (!StringUtils.isBlank(status)) {
            //Filtering users by status

            ProfessionalUsersEntityResponse professionalUsersEntityResponse = RefDataUtil.filterUsersByStatus(responseEntity, status);
            responseEntity = new ResponseEntity<>(professionalUsersEntityResponse, responseEntity.getHeaders(), responseEntity.getStatusCode());
        }

        return responseEntity;
    }

    private Page<ProfessionalUser> getPagedListOfUsers(Organisation organisation, Pageable pageable) {
        Page<ProfessionalUser> professionalUsers = professionalUserRepository.findByOrganisation(organisation, pageable);

        if (professionalUsers.getContent().isEmpty()) {
            throw new ResourceNotFoundException("No Users found for page number " + pageable.getPageNumber());
        }

        return professionalUsers;
    }

    private RetrieveUserProfilesRequest generateRetrieveUserProfilesRequest(List<ProfessionalUser> professionalUsers) {
        List<String> usersId = new ArrayList<>();

        professionalUsers.forEach(user -> usersId.add(user.getUserIdentifier()));

        return new RetrieveUserProfilesRequest(usersId);
    }


    @Override
    public ProfessionalUser persistUser(ProfessionalUser updatedProfessionalUser) {
        return professionalUserRepository.save(updatedProfessionalUser);
    }

    @Override
    public ModifyUserRolesResponse modifyRolesForUser(UserProfileUpdatedData userProfileUpdatedData, String userId, Optional<String> origin) {
        ModifyUserRolesResponse modifyUserRolesResponse;

        try (Response response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId, origin.orElse(""))) {
            modifyUserRolesResponse = RefDataUtil.decodeResponseFromUp(response);
        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "Error while invoking modifyRoles API in UP");
        }
        return modifyUserRolesResponse;
    }

    public ResponseEntity<NewUserResponse> findUserStatusByEmailAddress(String emailAddress) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(emailAddress));
        int statusCode = 200;
        NewUserResponse newUserResponse = null;
        if (user == null || user.getOrganisation().getStatus() != OrganisationStatus.ACTIVE) {
            throw new EmptyResultDataAccessException(1);
        }

        newUserResponse  =  RefDataUtil.findUserProfileStatusByEmail(emailAddress, userProfileFeignClient);

        if (!IdamStatus.ACTIVE.name().equalsIgnoreCase(newUserResponse.getIdamStatus())) {
            // If we dont find active user in up will send it to user 404 status code in the header
            statusCode = 404;
            newUserResponse = new NewUserResponse();
        } else {

            newUserResponse.setIdamStatus(null);
        }

        return ResponseEntity
                .status(statusCode)
                .body(newUserResponse);
    }

}