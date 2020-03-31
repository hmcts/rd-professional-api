package uk.gov.hmcts.reform.professionalapi.controller;

import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator.validateRoles;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeAllSpaces;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeEmptySpaces;

import feign.FeignException;
import feign.Response;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserCategory;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.domain.UserType;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.JurisdictionServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseUtil;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@RestController
@Slf4j
public abstract class SuperController {

    @Autowired
    protected OrganisationService organisationService;
    @Autowired
    protected ProfessionalUserService professionalUserService;
    @Autowired
    protected PaymentAccountService paymentAccountService;
    @Autowired
    protected PrdEnumService prdEnumService;
    @Autowired
    protected UpdateOrganisationRequestValidator updateOrganisationRequestValidator;
    @Autowired
    protected OrganisationCreationRequestValidator organisationCreationRequestValidator;
    @Autowired
    protected OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    @Autowired
    protected ProfessionalUserReqValidator profExtUsrReqValidator;
    @Autowired
    protected PaymentAccountValidator paymentAccountValidator;
    @Autowired
    private UserProfileFeignClient userProfileFeignClient;
    @Autowired
    private JurisdictionServiceImpl jurisdictionService;
    @Autowired
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @Value("${exui.role.hmcts-admin:}")
    protected String prdAdmin;

    @Value("${exui.role.pui-user-manager:}")
    protected String puiUserManager;

    @Value("${exui.role.pui-organisation-manager:}")
    protected String puiOrgManager;

    @Value("${exui.role.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${exui.role.pui-case-manager:}")
    protected String puiCaseManager;

    @Value("${prdEnumRoleType}")
    protected String prdEnumRoleType;

    @Value("${jurisdictionIdType}")
    private String jurisdictionIds;

    @Value("${resendInviteEnabled}")
    private boolean resendInviteEnabled;

    private static final String SRA_REGULATED_FALSE = "false";
    private static final String IDAM_ERROR_MESSAGE = "Idam register user failed with status code : %s";


    protected ResponseEntity<OrganisationResponse> createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest) {

        organisationCreationRequestValidator.validate(organisationCreationRequest);

        if (StringUtils.isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        if (null != organisationCreationRequest.getSuperUser()) {
            validateEmail(organisationCreationRequest.getSuperUser().getEmail());
        }

        if (organisationCreationRequest.getCompanyNumber() != null) {
            organisationCreationRequestValidator.validateCompanyNumber(organisationCreationRequest);
        }

        if (StringUtils.isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

        //Received response to create a new organisation
        return ResponseEntity
                .status(201)
                .body(organisationResponse);
    }

    protected ResponseEntity retrieveAllOrganisationOrById(String organisationIdentifier, String status) {
        String orgId = removeEmptySpaces(organisationIdentifier);
        String orgStatus = removeEmptySpaces(status);

        Object organisationResponse = null;
        if (StringUtils.isEmpty(orgId) && StringUtils.isEmpty(orgStatus)) {
            //Received request to retrieve all organisations
            organisationResponse =
                    organisationService.retrieveAllOrganisations();

        } else if (StringUtils.isEmpty(orgStatus) && StringUtils.isNotEmpty(orgId)
                || (StringUtils.isNotEmpty(orgStatus) && StringUtils.isNotEmpty(orgId))) {
            //Received request to retrieve organisation with ID

            organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
            organisationResponse =
                    organisationService.retrieveOrganisation(orgId);

        } else if (StringUtils.isNotEmpty(orgStatus) && StringUtils.isEmpty(orgId)) {

            if (OrganisationCreationRequestValidator.contains(orgStatus.toUpperCase())) {

                //Received request to retrieve organisation with status
                organisationResponse =
                        organisationService.findByOrganisationStatus(OrganisationStatus.valueOf(orgStatus.toUpperCase()));
            } else {
                log.error("Invalid Request param for status field");
                throw new InvalidRequest("400");
            }
        }
        log.debug("Received response to retrieve organisation details");
        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    protected ResponseEntity retrieveUserByEmail(String email) {
        validateEmail(email);

        ProfessionalUser user = professionalUserService.findProfessionalUserProfileByEmailAddress(removeEmptySpaces(email).toLowerCase());

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(user);
        return ResponseEntity
                .status(200)
                .body(professionalUsersResponse);
    }

    protected ResponseEntity retrievePaymentAccountByUserEmail(String email) {

        validateEmail(email);
        Organisation organisation = paymentAccountService.findPaymentAccountsByEmail(removeEmptySpaces(email).toLowerCase());
        if (null == organisation || organisation.getPaymentAccounts().isEmpty()) {

            throw new EmptyResultDataAccessException(1);
        }

        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false));
    }

    protected ResponseEntity updateOrganisationById(OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier, String userId) {
        organisationCreationRequest.setStatus(organisationCreationRequest.getStatus().toUpperCase());

        String orgId = removeEmptySpaces(organisationIdentifier);

        if (StringUtils.isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        organisationCreationRequestValidator.validate(organisationCreationRequest);
        organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);
        updateOrganisationRequestValidator.validateStatus(existingOrganisation, OrganisationStatus.valueOf(organisationCreationRequest.getStatus()), orgId);

        SuperUser superUser = existingOrganisation.getUsers().get(0);
        ProfessionalUser professionalUser = professionalUserService.findProfessionalUserById(superUser.getId());
        if (existingOrganisation.getStatus().isPending() && organisationCreationRequest.getStatus() != null
                && organisationCreationRequest.getStatus().equalsIgnoreCase("ACTIVE")) {
            //Organisation is getting activated

            jurisdictionService.propagateJurisdictionIdsForSuperUserToCcd(professionalUser, userId);
            ResponseEntity responseEntity = createUserProfileFor(professionalUser, null, true, false);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                UserProfileCreationResponse userProfileCreationResponse = (UserProfileCreationResponse) responseEntity.getBody();
                //Idam registration success
                professionalUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
                superUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
                professionalUserService.persistUser(professionalUser);
            } else {
                log.error(String.format(IDAM_ERROR_MESSAGE, responseEntity.getStatusCode().value()));
                return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
            }
        }
        organisationService.updateOrganisation(organisationCreationRequest, orgId);
        return ResponseEntity.status(200).build();
    }

    private ResponseEntity createUserProfileFor(ProfessionalUser professionalUser, List<String> roles, boolean isAdminUser, boolean isResendInvite) {
        //Creating user...
        List<String> userRoles = isAdminUser ? prdEnumService.getPrdEnumByEnumType(prdEnumRoleType) : roles;
        UserProfileCreationRequest userCreationRequest = new UserProfileCreationRequest(
                professionalUser.getEmailAddress(),
                professionalUser.getFirstName(),
                professionalUser.getLastName(),
                LanguagePreference.EN,
                UserCategory.PROFESSIONAL,
                UserType.EXTERNAL,
                userRoles,
                isResendInvite);

        try (Response response = userProfileFeignClient.createUserProfile(userCreationRequest)) {
            Class clazz = response.status() > 300 ? ErrorResponse.class : UserProfileCreationResponse.class;
            return JsonFeignResponseUtil.toResponseEntity(response, clazz);
        } catch (FeignException ex) {
            log.error("UserProfile api failed:: status code ::" + ex.status());
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "UserProfile api failed!!");
        }
    }

    protected ResponseEntity retrieveAllOrganisationsByStatus(String status) {
        String orgStatus = removeEmptySpaces(status);

        OrganisationsDetailResponse organisationsDetailResponse;
        if (OrganisationCreationRequestValidator.contains(orgStatus.toUpperCase())) {

            organisationsDetailResponse =
                    organisationService.findByOrganisationStatus(OrganisationStatus.valueOf(orgStatus.toUpperCase()));
        } else {
            log.error("Invalid Request param for status field");
            throw new InvalidRequest("400");
        }
        //Received response for status...
        return ResponseEntity.status(200).body(organisationsDetailResponse);
    }

    protected ResponseEntity inviteUserToOrganisation(NewUserCreationRequest newUserCreationRequest, String organisationIdentifier, String userId) {

        List<String> roles = newUserCreationRequest.getRoles();
        ProfessionalUser professionalUser = validateInviteUserRequestAndCreateNewUserObject(newUserCreationRequest, removeEmptySpaces(organisationIdentifier), roles);
        if (newUserCreationRequest.isResendInvite() && resendInviteEnabled) {
            return reInviteExpiredUser(newUserCreationRequest, professionalUser, roles);
        } else {
            return inviteNewUserToOrganisation(newUserCreationRequest, userId, professionalUser, roles);
        }
    }

    private ResponseEntity inviteNewUserToOrganisation(NewUserCreationRequest newUserCreationRequest, String userId, ProfessionalUser professionalUser, List<String> roles) {

        Object responseBody = null;
        checkUserAlreadyExist(newUserCreationRequest.getEmail());
        jurisdictionService.propagateJurisdictionIdsForNewUserToCcd(newUserCreationRequest.getJurisdictions(), userId, newUserCreationRequest.getEmail());
        ResponseEntity responseEntity = createUserProfileFor(professionalUser, roles, false, false);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            UserProfileCreationResponse userProfileCreationResponse = (UserProfileCreationResponse) responseEntity.getBody();
            //Idam registration success
            professionalUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
            responseBody = professionalUserService.addNewUserToAnOrganisation(professionalUser, roles, prdEnumService.findAllPrdEnums());
        } else {
            log.error(String.format(IDAM_ERROR_MESSAGE, responseEntity.getStatusCode().value()));
            responseBody = responseEntity.getBody();
        }

        return ResponseEntity
                .status(responseEntity.getStatusCode().value())
                .body(responseBody);
    }

    private ResponseEntity reInviteExpiredUser(NewUserCreationRequest newUserCreationRequest, ProfessionalUser professionalUser, List<String> roles) {

        Object responseBody = null;
        if (professionalUserService.findProfessionalUserByEmailAddress(newUserCreationRequest.getEmail()) == null) {
            throw new ResourceNotFoundException("User does not exist");
        }

        ResponseEntity responseEntity = createUserProfileFor(professionalUser, roles, false, true);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            responseBody = new NewUserResponse((UserProfileCreationResponse) responseEntity.getBody());
        } else {
            log.error(String.format(IDAM_ERROR_MESSAGE, responseEntity.getStatusCode().value()));
            responseBody = responseEntity.getBody();
        }

        return ResponseEntity
                .status(responseEntity.getStatusCode().value())
                .body(responseBody);
    }

    private ProfessionalUser validateInviteUserRequestAndCreateNewUserObject(NewUserCreationRequest newUserCreationRequest, String organisationIdentifier, List<String> roles) {

        validateNewUserCreationRequestForMandatoryFields(newUserCreationRequest);
        final Organisation existingOrganisation = checkOrganisationIsActive(removeEmptySpaces(organisationIdentifier));
        validateRoles(roles);
        return new ProfessionalUser(
                removeEmptySpaces(newUserCreationRequest.getFirstName()),
                removeEmptySpaces(newUserCreationRequest.getLastName()),
                removeAllSpaces(newUserCreationRequest.getEmail()),
                existingOrganisation);

    }

    protected ResponseEntity searchUsersByOrganisation(String organisationIdentifier, String showDeleted, boolean rolesRequired, String status, Integer page, Integer size) {

        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(organisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, organisationIdentifier);
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(existingOrganisation);
        ResponseEntity responseEntity;

        showDeleted = RefDataUtil.getShowDeletedValue(showDeleted);
        if (page != null) {
            Pageable pageable = RefDataUtil.createPageableObject(page, size, Sort.by(Sort.DEFAULT_DIRECTION,"firstName"));
            responseEntity = professionalUserService.findProfessionalUsersByOrganisationWithPageable(existingOrganisation, showDeleted, rolesRequired, status, pageable);
        } else {
            responseEntity = professionalUserService.findProfessionalUsersByOrganisation(existingOrganisation, showDeleted, rolesRequired, status);
        }
        return responseEntity;
    }

    protected ResponseEntity<ModifyUserRolesResponse> modifyRolesForUserOfOrganisation(UserProfileUpdatedData userProfileUpdatedData, String userId, Optional<String> origin) {

        userProfileUpdatedData = userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData);

        ModifyUserRolesResponse rolesResponse = professionalUserService.modifyRolesForUser(userProfileUpdatedData, userId, origin);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rolesResponse);
    }

    public void checkUserAlreadyExist(String userEmail) {
        if (professionalUserService.findProfessionalUserByEmailAddress(userEmail) != null) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "User already exists");
        }
    }

    public Organisation checkOrganisationIsActive(String orgId) {
        organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);
        organisationCreationRequestValidator.isOrganisationActive(existingOrganisation);
        return existingOrganisation;
    }
}
