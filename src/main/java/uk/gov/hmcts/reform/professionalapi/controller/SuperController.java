package uk.gov.hmcts.reform.professionalapi.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;


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

    @Value("${exui.role.hmcts-admin:}")
    private String prdAdmin;

    @Value("${exui.role.pui-user-manager:}")
    protected String puiUserManager;

    @Value("${exui.role.pui-organisation-manager:}")
    protected String puiOrgManager;

    @Value("${exui.role.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${exui.role.pui-case-manager:}")
    protected String puiCaseManager;


    protected ResponseEntity<?>  createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest) {

        log.info("ROLE::" + puiCaseManager + ":" + prdAdmin);
        organisationCreationRequestValidator.validate(organisationCreationRequest);

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

        log.info("Received response to create a new organisation..." + organisationResponse);
        return ResponseEntity
                .status(201)
                .body(organisationResponse);
    }

    protected ResponseEntity<?> retrieveAllOrganisationOrById(String id) {

        Object organisationResponse;
        if (id == null) {
            log.info("Received request to retrieve all organisations");
            organisationResponse =
                    organisationService.retrieveOrganisations();
        } else {
            log.info("Received request to retrieve organisation with ID " + id);

            organisationCreationRequestValidator.validateOrganisationIdentifier(id);
            organisationResponse =
                    organisationService.retrieveOrganisation(id);
        }

        log.debug("Received response to retrieve organisation details" + organisationResponse);
        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    protected ResponseEntity<ProfessionalUsersResponse> retrieveUserByEmail(String email) {

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress(email);

        if (user == null || user.getOrganisation().getStatus() != OrganisationStatus.ACTIVE) {
            throw new EmptyResultDataAccessException(1);
        }
        return ResponseEntity
                .status(200)
                .body(new ProfessionalUsersResponse(user));
    }

    protected ResponseEntity<?> retrievePaymentAccountByUserEmail(String email) {

        Organisation organisation = paymentAccountService.findPaymentAccountsByEmail(email);
        if (null == organisation || organisation.getPaymentAccounts().isEmpty()) {

            throw new EmptyResultDataAccessException(1);
        }
        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false));
    }

    protected ResponseEntity<?> updateOrganisationById(OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {

        organisationCreationRequestValidator.validate(organisationCreationRequest);
        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrganisationIdentifier(organisationIdentifier);
        updateOrganisationRequestValidator.validateStatus(existingOrganisation, organisationCreationRequest.getStatus(), organisationIdentifier);

        OrganisationResponse organisationResponse =
                organisationService.updateOrganisation(organisationCreationRequest, organisationIdentifier);
        log.info("Received response to update organisation..." + organisationResponse);
        return ResponseEntity.status(200).build();
    }

    protected ResponseEntity<?> retrieveAllOrganisationsByStatus(String status) {

        OrganisationsDetailResponse organisationsDetailResponse;
        if (organisationCreationRequestValidator.contains(status.toUpperCase())) {

            organisationsDetailResponse =
                    organisationService.findByOrganisationStatus(OrganisationStatus.valueOf(status.toUpperCase()));
        } else {
            log.error("Invalid Request param for status field");
            throw new InvalidRequest("400");
        }
        log.info("Received response for status...");
        return ResponseEntity.status(200).body(organisationsDetailResponse);
    }

    protected ResponseEntity<?> inviteUserToOrganisation(NewUserCreationRequest newUserCreationRequest, String organisationIdentifier) {

        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrganisationIdentifier(organisationIdentifier);
        updateOrganisationRequestValidator.validateStatus(existingOrganisation, null, organisationIdentifier);
        List<PrdEnum> prdEnumList = prdEnumService.findAllPrdEnums();

        if (UserCreationRequestValidator.contains(newUserCreationRequest.getRoles(), prdEnumList).isEmpty()) {
            log.error("Invalid/No user role(s) provided");
            throw new InvalidRequest("404");
        } else {
            NewUserResponse newUserResponse =
                    professionalUserService.addNewUserToAnOrganisation(newUserCreationRequest, organisationIdentifier);

            log.info("Received request to add a new user to an organisation..." + newUserResponse);
            return ResponseEntity
                    .status(201)
                    .body(newUserResponse);
        }
    }

    protected ResponseEntity<ProfessionalUsersEntityResponse> searchUsersByOrganisation(String organisationIdentifier, String showDeleted) {

        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrganisationIdentifier(organisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, organisationIdentifier);

        if (OrganisationStatus.ACTIVE != existingOrganisation.getStatus()) {
            log.error("Organisation is not Active hence not returning any users");
            throw new EmptyResultDataAccessException(1);
        }

        if (null == showDeleted) {
            log.info("Request param 'showDeleted' not provided or its value is null");
        }

        boolean showDeletedFlag = false;
        if ("True".equalsIgnoreCase(showDeleted)) {
            showDeletedFlag = true;
        }

        return ResponseEntity
                .status(200)
                .body(new ProfessionalUsersEntityResponse(professionalUserService
                        .findProfessionalUsersByOrganisation(existingOrganisation, showDeletedFlag)));
    }
}
