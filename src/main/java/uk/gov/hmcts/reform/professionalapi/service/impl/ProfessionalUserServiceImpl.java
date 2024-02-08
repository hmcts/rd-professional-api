package uk.gov.hmcts.reform.professionalapi.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccess;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccessId;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_UP_FAILED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_USER_MUST_BE_ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseUtil.toResponseEntity;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.filterUsersByStatus;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.setCaseAccessInGetUserResponse;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.setOrgInfoInGetUserResponse;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.setOrgIdInGetUserResponseAndSort;

@Service
@Slf4j
public class ProfessionalUserServiceImpl implements ProfessionalUserService {

    public static final String ERROR_USER_CONFIGURED_DELETE = "001 error while deleting user access records";
    public static final String ERROR_USER_CONFIGURED_CREATE = "002 error while creating new user access records";

    @Value("${group-access.organisation-profile-ids}")
    protected String organisationProfileIds;

    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;
    UserAttributeServiceImpl userAttributeService;
    UserProfileFeignClient userProfileFeignClient;
    UserConfiguredAccessRepository userConfiguredAccessRepository;
    UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @Autowired
    public ProfessionalUserServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            UserAttributeRepository userAttributeRepository,
            PrdEnumRepository prdEnumRepository,
            UserAttributeServiceImpl userAttributeService,
            UserProfileFeignClient userProfileFeignClient,
            UserConfiguredAccessRepository userConfiguredAccessRepository,
            UserProfileUpdateRequestValidator userProfileUpdateRequestValidator) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.userAttributeRepository = userAttributeRepository;
        this.prdEnumRepository = prdEnumRepository;
        this.userAttributeService = userAttributeService;
        this.userProfileFeignClient = userProfileFeignClient;
        this.userConfiguredAccessRepository = userConfiguredAccessRepository;
        this.userProfileUpdateRequestValidator = userProfileUpdateRequestValidator;
    }

    @Transactional
    @Override
    public NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles,
                                                      List<PrdEnum> prdEnumList) {

        var persistedNewUser = persistUser(newUser);

        userAttributeService.addUserAttributesToUser(persistedNewUser, roles, prdEnumList);

        return new NewUserResponse(persistedNewUser);
    }

    @Override
    public ProfessionalUser findProfessionalUserByEmailAddress(String email) {
        return professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(email));
    }

    @Override
    public ProfessionalUser findProfessionalUserById(UUID id) {
        Optional<ProfessionalUser> professionalUser = professionalUserRepository.findById(id);
        return professionalUser.orElse(null);
    }

    @Override
    public ProfessionalUser findProfessionalUserByUserIdentifier(String id) {
        return professionalUserRepository.findByUserIdentifier(id);
    }

    @Override
    public ResponseEntity<Object> findProfessionalUsersByOrganisationWithPageable(Organisation organisation,
                                                                                  String showDeleted,
                                                                                  boolean rolesRequired,
                                                                                  String status, Pageable pageable) {
        var pagedProfessionalUsers = getPagedListOfUsers(organisation, pageable);

        ResponseEntity<Object> responseEntity
                = retrieveUserProfiles(generateRetrieveUserProfilesRequest(pagedProfessionalUsers.getContent()),
                showDeleted, rolesRequired, status, organisation.getOrganisationIdentifier(),
                organisation.getStatus(),
                pagedProfessionalUsers.toList());

        var headers = RefDataUtil.generateResponseEntityWithPaginationHeader(pageable, pagedProfessionalUsers,
                responseEntity);

        return ResponseEntity.status(responseEntity.getStatusCode()).headers(headers).body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity<Object> findProfessionalUsersByOrganisation(Organisation organisation, String userIdentifier,
                                                                      String showDeleted, boolean rolesRequired,
                                                                      String status) {
        var professionalUsers = userIdentifier != null
                ? professionalUserRepository.findByOrganisationAndUserIdentifier(organisation, userIdentifier)
                : professionalUserRepository.findByOrganisation(organisation);

        if (professionalUsers.isEmpty()) {
            throw new ResourceNotFoundException("No Users were found for the given organisation");
        }
        return retrieveUserProfiles(generateRetrieveUserProfilesRequest(professionalUsers),
                showDeleted, rolesRequired, status, organisation.getOrganisationIdentifier(),
                organisation.getStatus(),
                professionalUsers);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> retrieveUserProfiles(RetrieveUserProfilesRequest retrieveUserProfilesRequest,
                                                        String showDeleted, boolean rolesRequired, String status,
                                                        String organisationIdentifier,
                                                        OrganisationStatus organisationStatus,
                                                        List<ProfessionalUser> professionalUsers) {
        ResponseEntity<Object> responseEntity;
        Object clazz;

        try (Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted,
                Boolean.toString(rolesRequired))) {

            if (response.status() > 300) {
                clazz = ErrorResponse.class;
            } else {
                clazz = rolesRequired ? ProfessionalUsersEntityResponse.class
                        : ProfessionalUsersEntityResponseWithoutRoles.class;
            }

            responseEntity = toResponseEntity(response, clazz);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                responseEntity = setOrgIdInGetUserResponseAndSort(responseEntity, organisationIdentifier);
                responseEntity = setOrgInfoInGetUserResponse(responseEntity, organisationIdentifier,
                        organisationStatus, List.of(organisationProfileIds.split(",")));
                responseEntity = setCaseAccessInGetUserResponse(responseEntity, professionalUsers);
            }

        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ERROR_MESSAGE_UP_FAILED);
        }

        if (!StringUtils.isBlank(status)) {
            //Filtering users by status

            Object response = filterUsersByStatus(responseEntity, status);
            responseEntity = new ResponseEntity<>(response, responseEntity.getHeaders(),
                    responseEntity.getStatusCode());
        }

        return responseEntity;
    }

    private Page<ProfessionalUser> getPagedListOfUsers(Organisation organisation, Pageable pageable) {
        var professionalUsers = professionalUserRepository.findByOrganisation(organisation,
                pageable);

        if (professionalUsers.getContent().isEmpty()) {
            throw new ResourceNotFoundException("No Users found for page number " + pageable.getPageNumber());
        }

        return professionalUsers;
    }

    public RetrieveUserProfilesRequest generateRetrieveUserProfilesRequest(List<ProfessionalUser> professionalUsers) {
        var usersId = new ArrayList<String>();

        professionalUsers.forEach(user -> usersId.add(user.getUserIdentifier()));

        return new RetrieveUserProfilesRequest(usersId);
    }


    @Override
    public ProfessionalUser persistUser(ProfessionalUser updatedProfessionalUser) {
        return professionalUserRepository.save(updatedProfessionalUser);
    }

    private ResponseEntity<Object> modifyRolesForUserOfOrganisation(UserProfileUpdatedData userProfileUpdatedData,
                                                     String userId, Optional<String> origin) {
        try (Response response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId,
                origin.orElse(""))) {
            return toResponseEntity(
                    response, response.status() > 300 ? ErrorResponse.class : ModifyUserRolesResponse.class);
        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status() > 0 ? ex.status() : 500),
                    ERROR_MESSAGE_UP_FAILED);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> modifyUserConfiguredAccessAndRoles(UserProfileUpdatedData userProfileUpdatedData,
                                                                     String userId, Optional<String> origin) {
        checkUserStatusIsActiveByUserId(userId);
        modifyUserConfiguredAccess(userProfileUpdatedData, userId);

        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, userId, origin);
    }

    public ResponseEntity<NewUserResponse> findUserStatusByEmailAddress(String emailAddress) {

        var user = professionalUserRepository.findByEmailAddress(RefDataUtil
                .removeAllSpaces(emailAddress));
        int statusCode = 200;
        if (user == null || user.getOrganisation().getStatus() != OrganisationStatus.ACTIVE) {
            throw new EmptyResultDataAccessException(1);
        }

        var newUserResponse = RefDataUtil.findUserProfileStatusByEmail(emailAddress, userProfileFeignClient);

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

    public void checkUserStatusIsActiveByUserId(String userId) {
        NewUserResponse newUserResponse = null;
        var user = professionalUserRepository.findByUserIdentifier(userId);

        if (null != user) {
            newUserResponse = RefDataUtil.findUserProfileStatusByEmail(user.getEmailAddress(), userProfileFeignClient);
        }

        if (newUserResponse == null || !IdamStatus.ACTIVE.name().equalsIgnoreCase(newUserResponse.getIdamStatus())) {
            throw new AccessDeniedException(ERROR_MESSAGE_USER_MUST_BE_ACTIVE);
        }
    }

    public ResponseEntity<Object> modifyRolesForUser(UserProfileUpdatedData userProfileUpdatedData,
                                                                    String userId, Optional<String> origin) {

        userProfileUpdatedData = userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData);

        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, userId, origin);
    }

    private void modifyUserConfiguredAccess(UserProfileUpdatedData userProfileUpdatedData,
                                            String userId) {

        ProfessionalUser professionalUser = findProfessionalUserByUserIdentifier(userId);
        try {
            List<UserConfiguredAccess> foundAccess = userConfiguredAccessRepository
                    .findByUserConfiguredAccessId_ProfessionalUser_Id(professionalUser.getId());
            if (!foundAccess.isEmpty()) {
                userConfiguredAccessRepository.deleteAll(foundAccess);
            }
        } catch (Exception ex) {
            throw new ExternalApiException(HttpStatus.valueOf(500), ERROR_USER_CONFIGURED_DELETE);
        }

        if (userProfileUpdatedData.getUserAccessTypes() != null) {
            try {
                List<UserConfiguredAccess> all = userProfileUpdatedData.getUserAccessTypes().stream()
                        .map(a -> mapToUserConfiguredAccess(professionalUser, a))
                        .collect(Collectors.toList());
                userConfiguredAccessRepository.saveAll(all);
            } catch (Exception ex) {
                throw new ExternalApiException(HttpStatus.valueOf(500), ERROR_USER_CONFIGURED_CREATE);
            }
        }
    }

    private UserConfiguredAccess mapToUserConfiguredAccess(ProfessionalUser professionalUser,
                                                           UserAccessType userAccessType) {
        UserConfiguredAccess uca = new UserConfiguredAccess();
        UserConfiguredAccessId ucaId = new UserConfiguredAccessId(
                professionalUser, userAccessType.getJurisdictionId(),
                userAccessType.getOrganisationProfileId(), userAccessType.getAccessTypeId()
        );
        uca.setUserConfiguredAccessId(ucaId);
        uca.setEnabled(userAccessType.getEnabled());

        return uca;
    }


}
