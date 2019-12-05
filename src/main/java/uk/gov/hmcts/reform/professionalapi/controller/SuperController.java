package uk.gov.hmcts.reform.professionalapi.controller;

import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator.validateEmail;

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
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.*;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.JurisdictionServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseHelper;
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
    protected OrganisationIdentifierIdentifierValidatorImpl organisationIdentifierValidatorImpl;
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


    static final String SRA_REGULATED_FALSE = "false";


    protected ResponseEntity<OrganisationResponse>  createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest) {

        organisationCreationRequestValidator.validate(organisationCreationRequest);
        organisationCreationRequestValidator.validateJurisdictions(organisationCreationRequest.getSuperUser().getJurisdictions(), prdEnumService.getPrdEnumByEnumType(jurisdictionIds));

        if (StringUtils.isBlank(organisationCreationRequest.getSraRegulated())) {
            organisationCreationRequest.setSraRegulated(SRA_REGULATED_FALSE);
        }

        if (null != organisationCreationRequest.getSuperUser()) {
            validateEmail(organisationCreationRequest.getSuperUser().getEmail());
        }

        organisationCreationRequestValidator.validateJurisdictions(organisationCreationRequest.getSuperUser().getJurisdictions(), prdEnumService.getPrdEnumByEnumType(jurisdictionIds));

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
        String orgId = RefDataUtil.removeEmptySpaces(organisationIdentifier);
        String orgStatus = RefDataUtil.removeEmptySpaces(status);

        Object organisationResponse = null;
        if (StringUtils.isEmpty(orgId) && StringUtils.isEmpty(orgStatus)) {
            //Received request to retrieve all organisations
            organisationResponse =
                    organisationService.retrieveOrganisations();

        } else if (StringUtils.isEmpty(orgStatus) && StringUtils.isNotEmpty(orgId)
                || (StringUtils.isNotEmpty(orgStatus) && StringUtils.isNotEmpty(orgId))) {
            //Received request to retrieve organisation with ID

            organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
            organisationResponse =
                    organisationService.retrieveOrganisation(orgId);

        } else if (StringUtils.isNotEmpty(orgStatus) && StringUtils.isEmpty(orgId)) {

            if (organisationCreationRequestValidator.contains(orgStatus.toUpperCase())) {

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

        ProfessionalUser user = professionalUserService.findProfessionalUserProfileByEmailAddress(RefDataUtil.removeEmptySpaces(email));

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(user);
        return ResponseEntity
                .status(200)
                .body(professionalUsersResponse);
    }

    protected ResponseEntity retrievePaymentAccountByUserEmail(String email) {

        Organisation organisation = paymentAccountService.findPaymentAccountsByEmail(RefDataUtil.removeEmptySpaces(email));
        if (null == organisation || organisation.getPaymentAccounts().isEmpty()) {

            throw new EmptyResultDataAccessException(1);
        }

        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false));
    }

    protected ResponseEntity updateOrganisationById(OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier, String userId) {
        organisationCreationRequest.setStatus(organisationCreationRequest.getStatus().toUpperCase());

        String orgId = RefDataUtil.removeEmptySpaces(organisationIdentifier);

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
            ResponseEntity responseEntity = createUserProfileFor(professionalUser, null, true);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                UserProfileCreationResponse userProfileCreationResponse = (UserProfileCreationResponse) responseEntity.getBody();
                //Idam registration success
                professionalUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
                superUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
                professionalUserService.persistUser(professionalUser);
            } else {
                log.error("Idam register user failed with status code : " + responseEntity.getStatusCode());
                return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
            }
        }
        organisationService.updateOrganisation(organisationCreationRequest, orgId);
        return ResponseEntity.status(200).build();
    }

    private ResponseEntity createUserProfileFor(ProfessionalUser professionalUser, List<String> roles, boolean isAdminUser) {
        //Creating user...
        List<String> userRoles = isAdminUser ? prdEnumService.getPrdEnumByEnumType(prdEnumRoleType) : roles;
        UserProfileCreationRequest userCreationRequest = new UserProfileCreationRequest(
                professionalUser.getEmailAddress(),
                professionalUser.getFirstName(),
                professionalUser.getLastName(),
                LanguagePreference.EN,
                UserCategory.PROFESSIONAL,
                UserType.EXTERNAL,
                userRoles);

        try (Response response = userProfileFeignClient.createUserProfile(userCreationRequest)) {
            Class clazz = response.status() > 300 ? ErrorResponse.class : UserProfileCreationResponse.class;
            return JsonFeignResponseHelper.toResponseEntity(response, clazz);
        }  catch (FeignException ex) {
            log.error("UserProfile api failed:: status code ::" + ex.status());
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "UserProfile api failed!!");
        }
    }

    protected ResponseEntity retrieveAllOrganisationsByStatus(String status) {
        String orgStatus = RefDataUtil.removeEmptySpaces(status);

        OrganisationsDetailResponse organisationsDetailResponse;
        if (organisationCreationRequestValidator.contains(orgStatus.toUpperCase())) {

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
        String orgId = RefDataUtil.removeEmptySpaces(organisationIdentifier);

        Object responseBody = null;
        OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields(newUserCreationRequest);
        validateEmail(newUserCreationRequest.getEmail());
        organisationCreationRequestValidator.validateOrganisationIdentifier(orgId);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(orgId);
        organisationCreationRequestValidator.isOrganisationActive(existingOrganisation);

        organisationCreationRequestValidator.validateJurisdictions(newUserCreationRequest.getJurisdictions(), prdEnumService.getPrdEnumByEnumType(jurisdictionIds));

        List<PrdEnum> prdEnumList = prdEnumService.findAllPrdEnums();
        List<String> roles = newUserCreationRequest.getRoles();
        UserCreationRequestValidator.validateRoles(roles, prdEnumList);

        ProfessionalUser newUser = new ProfessionalUser(
                RefDataUtil.removeEmptySpaces(newUserCreationRequest.getFirstName()),
                RefDataUtil.removeEmptySpaces(newUserCreationRequest.getLastName()),
                RefDataUtil.removeAllSpaces(newUserCreationRequest.getEmail()),
                existingOrganisation);

        jurisdictionService.propagateJurisdictionIdsForNewUserToCcd(newUserCreationRequest.getJurisdictions(), userId, newUserCreationRequest.getEmail());

        ResponseEntity responseEntity = createUserProfileFor(newUser, roles, false);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            UserProfileCreationResponse userProfileCreationResponse = (UserProfileCreationResponse) responseEntity.getBody();
            //Idam registration success
            newUser.setUserIdentifier(userProfileCreationResponse.getIdamId());
            responseBody = professionalUserService.addNewUserToAnOrganisation(newUser, roles, prdEnumList);
        } else {
            log.error("Idam register user failed with status code : " + responseEntity.getStatusCode());
            responseBody = responseEntity.getBody();
        }

        return ResponseEntity
                .status(responseEntity.getStatusCode().value())
                .body(responseBody);
    }

    protected ResponseEntity searchUsersByOrganisation(String organisationIdentifier, String showDeleted, boolean rolesRequired, String status, Integer page, Integer size) {

        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrgIdentifier(organisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, organisationIdentifier);
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(existingOrganisation);
        ResponseEntity responseEntity;

        showDeleted = RefDataUtil.getShowDeletedValue(showDeleted);

        if (page != null) {
            Pageable pageable = RefDataUtil.createPageableObject(page, size, new Sort(Sort.DEFAULT_DIRECTION,"firstName"));
            responseEntity = professionalUserService.findProfessionalUsersByOrganisationWithPageable(existingOrganisation, showDeleted, rolesRequired, status, pageable);
        } else {
            responseEntity = professionalUserService.findProfessionalUsersByOrganisation(existingOrganisation, showDeleted, rolesRequired, status);
        }
        return responseEntity;
    }

    //TODO refactor
    protected ResponseEntity<ModifyUserRolesResponse> modifyRolesForUserOfOrganisation(UserProfileUpdatedData userProfileUpdatedData, String organisationIdentifier, String userId, Optional<String> origin) {

        userProfileUpdatedData = userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData);

        ModifyUserRolesResponse rolesResponse = professionalUserService.modifyRolesForUser(userProfileUpdatedData, userId, origin);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rolesResponse);
    }
}
