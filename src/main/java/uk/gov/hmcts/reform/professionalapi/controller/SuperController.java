package uk.gov.hmcts.reform.professionalapi.controller;

import feign.FeignException;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaUpdateRequest;
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
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UpdatePbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserCategory;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.domain.UserType;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEFAULT_PAGE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEFAULT_PAGE_SIZE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ADDRESS_LIST_IS_EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_REQUEST_IS_EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FIRST_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.USER_EMAIL;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.isInputOrganisationStatusValid;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator.validateRoles;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.valueOf;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.checkOrganisationAndMoreThanRequiredAddressExists;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.checkOrganisationAndPbaExists;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.createPageableObject;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.getReturnRolesValue;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.getShowDeletedValue;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.matchAddressIdsWithOrgContactInformationIds;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeAllSpaces;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeEmptySpaces;

@RestController
@Slf4j
public abstract class SuperController {

    @Autowired
    protected OrganisationService organisationService;
    @Autowired
    protected ProfessionalUserService professionalUserService;
    @Autowired
    protected ProfessionalUserRepository professionalUserRepository;
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
    @Autowired
    protected IdamRepository idamRepository;

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

        var organisationResponse = organisationService.createOrganisationFrom(organisationCreationRequest);

        //Received response to create a new organisation
        return ResponseEntity
                .status(201)
                .body(organisationResponse);
    }

    protected ResponseEntity<Object> retrieveAllOrganisationOrById(String organisationIdentifier, String status,
                                                                   Integer page, Integer size) {
        var orgId = removeEmptySpaces(organisationIdentifier);
        var orgStatus = removeEmptySpaces(status);
        long totalRecords = 1;

        Object organisationResponse = null;
        var pageable = createPageable(page, size);

        if (StringUtils.isEmpty(orgId) && StringUtils.isEmpty(orgStatus)) {
            //Received request to retrieve all organisations
            organisationResponse = organisationService.retrieveAllOrganisations(pageable);
            totalRecords = ((OrganisationsDetailResponse) organisationResponse).getTotalRecords();

        } else if (StringUtils.isEmpty(orgStatus) && isNotEmpty(orgId)
                || (isNotEmpty(orgStatus) && isNotEmpty(orgId))) {
            //Received request to retrieve organisation with ID

            organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
            organisationResponse = organisationService.retrieveOrganisation(orgId, true);

        } else if (isNotEmpty(orgStatus) && StringUtils.isEmpty(orgId)) {
            //Received request to retrieve organisation with status

            organisationResponse = organisationService.findByOrganisationStatus(orgStatus.toUpperCase(), pageable);
            totalRecords = ((OrganisationsDetailResponse) organisationResponse).getTotalRecords();
        }

        log.debug("{}:: Received response to retrieve organisation details", loggingComponentName);

        if (pageable != null) {
            return ResponseEntity.status(200).header("total_records",String.valueOf(totalRecords))
                    .body(organisationResponse);
        }
        return ResponseEntity.status(200).body(organisationResponse);
    }

    private Pageable createPageable(Integer page, Integer size) {
        Pageable pageable = null;
        if (page != null || size != null) {
            if (page != null && page < 1) {
                throw new InvalidRequest("Default page number should start with page 1");
            }
            if (page == null) {
                page = DEFAULT_PAGE;
            } else if (size == null) {
                size = DEFAULT_PAGE_SIZE;
            }
            var order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
            pageable = createPageableObject(page - 1, size, Sort.by(order));
        }
        return pageable;
    }

    protected ResponseEntity<Object> retrievePaymentAccountByUserEmail(String email) {

        validateEmail(email);
        var organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());

        checkOrganisationAndPbaExists(organisation);

        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false, true, false));
    }

    protected ResponseEntity<Object> updateOrganisationById(OrganisationCreationRequest organisationCreationRequest,
                                                            String organisationIdentifier) {
        Boolean  isOrgApprovalRequest = false;
        if (isBlank(organisationCreationRequest.getStatus())) {
            throw new InvalidRequest("Mandatory field status is missing");
        }

        organisationCreationRequest.setStatus(organisationCreationRequest.getStatus().toUpperCase());

        var orgId = removeEmptySpaces(organisationIdentifier);

        if (isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        organisationCreationRequestValidator.validate(organisationCreationRequest);
        organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
        var existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);
        updateOrganisationRequestValidator.validateStatus(existingOrganisation, valueOf(organisationCreationRequest
                .getStatus()), orgId);

        var superUser = existingOrganisation.getUsers().get(0);
        var professionalUser = professionalUserService.findProfessionalUserById(superUser.getId());
        if ((existingOrganisation.getStatus().isPending() || existingOrganisation.getStatus().isReview())
                && organisationCreationRequest.getStatus() != null
                && organisationCreationRequest.getStatus().equalsIgnoreCase("ACTIVE")) {
            //Organisation is getting activated
            isOrgApprovalRequest = true;
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
        organisationService.updateOrganisation(organisationCreationRequest, orgId,isOrgApprovalRequest);
        return ResponseEntity.status(200).build();
    }

    private ResponseEntity<Object> createUserProfileFor(ProfessionalUser professionalUser, List<String> roles,
                                                        boolean isAdminUser, boolean isResendInvite) {
        //Creating user...
        var userRoles = isAdminUser ? prdEnumService.getPrdEnumByEnumType(prdEnumRoleType) : roles;
        var userCreationRequest = new UserProfileCreationRequest(
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

        var organisations = organisationService.getOrganisationByStatus(ACTIVE);

        if (isEmpty(organisations)) {
            throw new ResourceNotFoundException("No Organisations found");
        }

        var organisationMinimalInfoResponses =
                organisations.stream()
                        .map(organisation -> new OrganisationMinimalInfoResponse(organisation, address)).toList();

        return ResponseEntity.status(200).body(organisationMinimalInfoResponses);
    }

    protected ResponseEntity<Object> inviteUserToOrganisation(NewUserCreationRequest newUserCreationRequest,
                                                              String organisationIdentifier) {

        var roles = newUserCreationRequest.getRoles();
        var professionalUser = validateInviteUserRequestAndCreateNewUserObject(newUserCreationRequest,
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
        var responseEntity = createUserProfileFor(professionalUser, roles, false,
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
        //gets current user by email in prd
        var existingUser = professionalUserService
                .findProfessionalUserByEmailAddress(newUserCreationRequest.getEmail());
        if (existingUser == null) {
            throw new ResourceNotFoundException("User does not exist");
        } else if (!existingUser.getOrganisation().getOrganisationIdentifier()
                .equalsIgnoreCase(organisationIdentifier)) {
            throw new AccessDeniedException("User does not belong to same organisation");
        }

        //this creates user in user profile
        var responseEntity = createUserProfileFor(professionalUser, roles, false,
                true);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {

            NewUserResponse userProfileResponse = new NewUserResponse((UserProfileCreationResponse)
                    responseEntity.getBody());

            //then check if existing user and idam user id are same
            if (!userProfileResponse.getUserIdentifier().equals(existingUser.getUserIdentifier())) {
                //we need to then update user info
                existingUser.setUserIdentifier(userProfileResponse.getUserIdentifier());
                professionalUserRepository.save(existingUser);
                responseBody = responseEntity.getBody();
            }
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

    protected ResponseEntity<Object> searchUsersByOrganisation(String organisationIdentifier, String userIdentifier,
                                                               String showDeleted, Boolean returnRoles, String status,
                                                               Integer page, Integer size) {

        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(organisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, organisationIdentifier);
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(existingOrganisation, NOT_FOUND);
        ResponseEntity<Object> responseEntity;

        showDeleted = getShowDeletedValue(showDeleted);
        returnRoles = getReturnRolesValue(returnRoles);

        if (page != null && userIdentifier == null) {
            Pageable pageable = createPageableObject(page, size, Sort.by(Sort.DEFAULT_DIRECTION, FIRST_NAME));
            responseEntity = professionalUserService
                    .findProfessionalUsersByOrganisationWithPageable(existingOrganisation, showDeleted, returnRoles,
                            status, pageable);
        } else {
            responseEntity = professionalUserService.findProfessionalUsersByOrganisation(existingOrganisation,
                    userIdentifier, showDeleted, returnRoles, status);
        }
        return responseEntity;
    }

    protected void deletePaymentAccountsOfGivenOrganisation(PbaRequest deletePbaRequest,
                                                            String orgId, String userId) {
        var paymentAccounts = deletePbaRequest.getPaymentAccounts();
        if (ObjectUtils.isEmpty(deletePbaRequest.getPaymentAccounts())) {
            throw new InvalidRequest("No PBA number passed in the request");
        }

        if (paymentAccounts.contains(null) || paymentAccounts.contains(EMPTY)) {
            throw new InvalidRequest("Invalid PBA number passed in the request");
        }

        //check if user status is 'ACTIVE'
        professionalUserService.checkUserStatusIsActiveByUserId(userId);

        var existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);

        //if the organisation is present, check if it is 'ACTIVE'
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(existingOrganisation, BAD_REQUEST);

        //check if organisation is present in the database and that it has payment accounts associated
        checkOrganisationAndPbaExists(existingOrganisation);

        //check the pba account number format, remove any blank strings passed
        //And check if pba belongs to the organisation
        paymentAccountValidator.validatePaymentAccounts(
                deletePbaRequest.getPaymentAccounts(), existingOrganisation, true);

        //delete the passed pba account numbers from the organisation
        paymentAccountService.deletePaymentsOfOrganisation(deletePbaRequest, existingOrganisation);
    }

    protected ResponseEntity<Object> modifyRolesForUserOfOrganisation(UserProfileUpdatedData userProfileUpdatedData,
                                                                      String userId, Optional<String> origin) {

        userProfileUpdatedData = userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData);

        return professionalUserService.modifyRolesForUser(userProfileUpdatedData, userId, origin);
    }

    public UpdatePbaStatusResponse updateAnOrganisationsPbas(List<PbaUpdateRequest> pbaRequestList, String orgId) {

        return paymentAccountService.updatePaymentAccountsStatusForAnOrganisation(pbaRequestList, orgId);
    }

    public void checkUserAlreadyExist(String userEmail) {
        if (professionalUserService.findProfessionalUserByEmailAddress(userEmail) != null) {
            throw new HttpClientErrorException(CONFLICT, "User already exists");
        }
    }

    public Organisation checkOrganisationIsActive(String orgId) {
        organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
        var existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);
        organisationCreationRequestValidator.isOrganisationActive(existingOrganisation);
        return existingOrganisation;
    }

    public String getUserEmail(String email) {
        String userEmail = null;
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (nonNull(servletRequestAttributes)) {

            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (nonNull(request.getHeader(USER_EMAIL))) {

                userEmail = request.getHeader(USER_EMAIL);
            } else if (nonNull(email)) {
                userEmail = email;

            } else {
                throw new InvalidRequest("No User Email provided via header or param");
            }
        }

        return userEmail;
    }

    public String getUserEmailFromHeader() {
        String userEmail = null;
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (nonNull(servletRequestAttributes)) {

            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (nonNull(request.getHeader(USER_EMAIL))) {

                userEmail = request.getHeader(USER_EMAIL);
            } else {
                throw new InvalidRequest("No User Email provided via header");
            }
        }

        return userEmail;
    }

    protected void deleteMultipleAddressOfGivenOrganisation(List<DeleteMultipleAddressRequest> deleteRequest,
                                                            String orgId) {
        var addressIds = deleteRequest.stream()
                .map(DeleteMultipleAddressRequest::getAddressId)
                .collect(Collectors.toSet());

        if (ObjectUtils.isEmpty(addressIds)) {
            throw new InvalidRequest(ERROR_MSG_ADDRESS_LIST_IS_EMPTY);
        }
        if (addressIds.contains(null) || addressIds.contains(EMPTY)
                || addressIds.stream().anyMatch(StringUtils::isBlank)) {
            throw new InvalidRequest(ERROR_MSG_REQUEST_IS_EMPTY);
        }

        var existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);

        //match address id with organisation contact information id's
        matchAddressIdsWithOrgContactInformationIds(existingOrganisation, addressIds);

        //check if organisation is present in the database and that it has more than required address associated
        checkOrganisationAndMoreThanRequiredAddressExists(existingOrganisation, addressIds);

        //delete the passed address id numbers from the request
        var idsSet = addressIds.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        organisationService.deleteMultipleAddressOfGivenOrganisation(idsSet);
    }

    public String getUserToken() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getTokenValue();
    }
}
