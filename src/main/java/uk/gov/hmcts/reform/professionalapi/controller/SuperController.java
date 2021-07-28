package uk.gov.hmcts.reform.professionalapi.controller;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FIRST_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.isInputOrganisationStatusValid;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator.validateRoles;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.valueOf;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.checkOrganisationAndPbaExists;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.createPageableObject;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.getReturnRolesValue;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.getShowDeletedValue;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeAllSpaces;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeEmptySpaces;

import feign.FeignException;
import feign.Response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserCategory;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.domain.UserType;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseUtil;

import javax.servlet.http.HttpServletRequest;

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
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;
    @Autowired
    protected MfaStatusService mfaStatusService;

    @Value("${prd.security.roles.hmcts-admin:}")
    protected String prdAdmin;

    @Value("${prd.security.roles.pui-user-manager:}")
    protected String puiUserManager;

    @Value("${prd.security.roles.pui-organisation-manager:}")
    protected String puiOrgManager;

    @Value("${prd.security.roles.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${prd.security.roles.pui-case-manager:}")
    protected String puiCaseManager;

    @Value("${prdEnumRoleType}")
    protected String prdEnumRoleType;

    @Value("${resendInviteEnabled}")
    private boolean resendInviteEnabled;

    @Value("${allowedStatus}")
    private String allowedOrganisationStatus;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    protected JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private static final String SRA_REGULATED_FALSE = "false";
    private static final String IDAM_ERROR_MESSAGE = "{}:: Idam register user failed with status code : %s";


    protected ResponseEntity<OrganisationResponse> createOrganisationFrom(
            OrganisationCreationRequest organisationCreationRequest) {

        organisationCreationRequestValidator.validate(organisationCreationRequest);

        if (isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        if (null != organisationCreationRequest.getSuperUser()) {
            validateEmail(organisationCreationRequest.getSuperUser().getEmail());
        }

        if (organisationCreationRequest.getCompanyNumber() != null) {
            organisationCreationRequestValidator.validateCompanyNumber(organisationCreationRequest);
        }

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

        //Received response to create a new organisation
        return ResponseEntity
                .status(201)
                .body(organisationResponse);
    }

    protected ResponseEntity<Object> retrieveAllOrganisationOrById(String organisationIdentifier, String status) {
        String orgId = removeEmptySpaces(organisationIdentifier);
        String orgStatus = removeEmptySpaces(status);

        Object organisationResponse = null;
        if (StringUtils.isEmpty(orgId) && StringUtils.isEmpty(orgStatus)) {
            //Received request to retrieve all organisations
            organisationResponse =
                    organisationService.retrieveAllOrganisations();

        } else if (StringUtils.isEmpty(orgStatus) && isNotEmpty(orgId)
                || (isNotEmpty(orgStatus) && isNotEmpty(orgId))) {
            //Received request to retrieve organisation with ID

            organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
            organisationResponse =
                    organisationService.retrieveOrganisation(orgId);

        } else if (isNotEmpty(orgStatus) && StringUtils.isEmpty(orgId)) {

            if (OrganisationCreationRequestValidator.contains(orgStatus.toUpperCase())) {

                //Received request to retrieve organisation with status
                organisationResponse =
                        organisationService.findByOrganisationStatus(valueOf(orgStatus.toUpperCase()));
            } else {
                log.error("{}:: Invalid Request param for status field", loggingComponentName);
                throw new InvalidRequest("400");
            }
        }
        log.debug("{}:: Received response to retrieve organisation details", loggingComponentName);
        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    protected ResponseEntity<Object> retrievePaymentAccountByUserEmail(String email) {

        validateEmail(email);
        Organisation organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());

        checkOrganisationAndPbaExists(organisation);

        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false));
    }

    protected ResponseEntity<Object> updateOrganisationById(OrganisationCreationRequest organisationCreationRequest,
                                                            String organisationIdentifier) {
        organisationCreationRequest.setStatus(organisationCreationRequest.getStatus().toUpperCase());

        String orgId = removeEmptySpaces(organisationIdentifier);

        if (isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        organisationCreationRequestValidator.validate(organisationCreationRequest);
        organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);
        updateOrganisationRequestValidator.validateStatus(existingOrganisation, valueOf(organisationCreationRequest
                .getStatus()), orgId);

        SuperUser superUser = existingOrganisation.getUsers().get(0);
        ProfessionalUser professionalUser = professionalUserService.findProfessionalUserById(superUser.getId());
        if (existingOrganisation.getStatus().isPending() && organisationCreationRequest.getStatus() != null
                && organisationCreationRequest.getStatus().equalsIgnoreCase("ACTIVE")) {
            //Organisation is getting activated

            ResponseEntity<Object> responseEntity = createUserProfileFor(professionalUser, null, true,
                    false);
            if (responseEntity.getStatusCode().is2xxSuccessful() && null != responseEntity.getBody()) {
                UserProfileCreationResponse userProfileCreationResponse
                        = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());
                //Idam registration success
                professionalUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
                superUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
                professionalUserService.persistUser(professionalUser);
            } else {
                log.error("{}:: " + String.format(IDAM_ERROR_MESSAGE, responseEntity.getStatusCode().value()),
                        loggingComponentName);
                return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
            }
        }
        organisationService.updateOrganisation(organisationCreationRequest, orgId);
        return ResponseEntity.status(200).build();
    }

    private ResponseEntity<Object> createUserProfileFor(ProfessionalUser professionalUser, List<String> roles,
                                                        boolean isAdminUser, boolean isResendInvite) {
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
            Object clazz = response.status() > 300 ? ErrorResponse.class : UserProfileCreationResponse.class;
            return JsonFeignResponseUtil.toResponseEntity(response, clazz);
        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {}", loggingComponentName, ex.status());
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "UserProfile api failed!!");
        }
    }

    protected ResponseEntity<List<OrganisationMinimalInfoResponse>> retrieveAllOrganisationsByStatus(
            String status, boolean address) {

        isInputOrganisationStatusValid(status, allowedOrganisationStatus);

        List<Organisation> organisations = organisationService.getOrganisationByStatus(ACTIVE);

        if (isEmpty(organisations)) {
            throw new ResourceNotFoundException("No Organisations found");
        }

        List<OrganisationMinimalInfoResponse> organisationMinimalInfoResponses =
                organisations.stream().map(organisation -> new OrganisationMinimalInfoResponse(organisation, address))
                        .collect(Collectors.toList());

        return ResponseEntity.status(200).body(organisationMinimalInfoResponses);
    }

    protected ResponseEntity<Object> inviteUserToOrganisation(NewUserCreationRequest newUserCreationRequest,
                                                              String organisationIdentifier, String userId) {

        List<String> roles = newUserCreationRequest.getRoles();
        ProfessionalUser professionalUser = validateInviteUserRequestAndCreateNewUserObject(newUserCreationRequest,
                removeEmptySpaces(organisationIdentifier), roles);
        if (newUserCreationRequest.isResendInvite() && resendInviteEnabled) {
            return reInviteExpiredUser(newUserCreationRequest, professionalUser, roles, organisationIdentifier);
        } else {
            return inviteNewUserToOrganisation(newUserCreationRequest, professionalUser, roles);
        }
    }

    private ResponseEntity<Object> inviteNewUserToOrganisation(NewUserCreationRequest newUserCreationRequest,
                                                               ProfessionalUser professionalUser, List<String> roles) {

        Object responseBody = null;
        checkUserAlreadyExist(newUserCreationRequest.getEmail());
        ResponseEntity<Object> responseEntity = createUserProfileFor(professionalUser, roles, false,
                false);
        if (responseEntity.getStatusCode().is2xxSuccessful() && null != responseEntity.getBody()) {
            UserProfileCreationResponse userProfileCreationResponse
                    = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());
            //Idam registration success
            professionalUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
            responseBody = professionalUserService.addNewUserToAnOrganisation(professionalUser, roles,
                    prdEnumService.findAllPrdEnums());
        } else {
            log.error(loggingComponentName + String.format(IDAM_ERROR_MESSAGE,
                    responseEntity.getStatusCode().value()));
            responseBody = responseEntity.getBody();
        }

        return ResponseEntity
                .status(responseEntity.getStatusCode().value())
                .body(responseBody);
    }

    private ResponseEntity<Object> reInviteExpiredUser(NewUserCreationRequest newUserCreationRequest,
                                                       ProfessionalUser professionalUser, List<String> roles,
                                                       String organisationIdentifier) {

        Object responseBody = null;
        ProfessionalUser existingUser = professionalUserService
                .findProfessionalUserByEmailAddress(newUserCreationRequest.getEmail());
        if (existingUser == null) {
            throw new ResourceNotFoundException("User does not exist");
        } else if (!existingUser.getOrganisation().getOrganisationIdentifier()
                .equalsIgnoreCase(organisationIdentifier)) {
            throw new AccessDeniedException("User does not belong to same organisation");
        }

        ResponseEntity<Object> responseEntity = createUserProfileFor(professionalUser, roles, false,
                true);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            responseBody = new NewUserResponse((UserProfileCreationResponse) responseEntity.getBody());
        } else {
            log.error(loggingComponentName + String.format(IDAM_ERROR_MESSAGE,
                    responseEntity.getStatusCode().value()));
            responseBody = responseEntity.getBody();
        }

        return ResponseEntity
                .status(responseEntity.getStatusCode().value())
                .body(responseBody);
    }

    private ProfessionalUser validateInviteUserRequestAndCreateNewUserObject(
            NewUserCreationRequest newUserCreationRequest, String organisationIdentifier, List<String> roles) {

        validateNewUserCreationRequestForMandatoryFields(newUserCreationRequest);
        final Organisation existingOrganisation = checkOrganisationIsActive(removeEmptySpaces(organisationIdentifier));
        validateRoles(roles);
        return new ProfessionalUser(
                removeEmptySpaces(newUserCreationRequest.getFirstName()),
                removeEmptySpaces(newUserCreationRequest.getLastName()),
                removeAllSpaces(newUserCreationRequest.getEmail()),
                existingOrganisation);

    }

    protected ResponseEntity<Object> searchUsersByOrganisation(String organisationIdentifier, String showDeleted,
                                                               Boolean returnRoles, String status, Integer page,
                                                               Integer size) {

        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(organisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, organisationIdentifier);
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(existingOrganisation);
        ResponseEntity<Object> responseEntity;

        showDeleted = getShowDeletedValue(showDeleted);
        returnRoles = getReturnRolesValue(returnRoles);

        if (page != null) {
            Pageable pageable = createPageableObject(page, size, Sort.by(Sort.DEFAULT_DIRECTION, FIRST_NAME));
            responseEntity = professionalUserService
                    .findProfessionalUsersByOrganisationWithPageable(existingOrganisation, showDeleted, returnRoles,
                            status, pageable);
        } else {
            responseEntity = professionalUserService.findProfessionalUsersByOrganisation(existingOrganisation,
                    showDeleted, returnRoles, status);
        }
        return responseEntity;
    }

    protected ResponseEntity<Object> modifyRolesForUserOfOrganisation(UserProfileUpdatedData userProfileUpdatedData,
                                                                      String userId, Optional<String> origin) {

        userProfileUpdatedData = userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData);

        return professionalUserService.modifyRolesForUser(userProfileUpdatedData, userId, origin);
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

    public String getUserEmail(String email) {
        String userEmail = null;
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (nonNull(servletRequestAttributes)) {

            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (nonNull(request.getHeader("UserEmail"))) {

                userEmail = request.getHeader("UserEmail");
                log.warn("** Setting user email on the header  ** referer - {} ", request.getHeader("Referer"));
            } else if (nonNull(email)) {
                userEmail = email;
                log.warn("** [DEPRECATED USAGE] Setting user email on the path variable will be deprecated soon !"
                        + " ** referer - {} ", request.getHeader("Referer"));

            } else {
                throw new InvalidRequest("No User Email provided via header or param");
            }
        }

        return userEmail;
    }
}
