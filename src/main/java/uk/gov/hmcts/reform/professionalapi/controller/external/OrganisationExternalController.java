package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.FORBIDDEN_ERROR_ACCESS_DENIED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_CASE_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_FINANCE_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_ORGANISATION_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_USER_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

@RequestMapping(
        path = "refdata/external/v1/organisations"
)
@RestController
@Slf4j
public class OrganisationExternalController extends SuperController {

    @ApiOperation(
            value = "Creates an Organisation",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "The Organisation Identifier of the created Organisation",
                    response = OrganisationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<OrganisationResponse> createOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for external user
        return createOrganisationFrom(organisationCreationRequest);
    }

    @ApiOperation(
            value = "Retrieves Organisation details of the requesting User",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Details of an Organisation",
                    response = OrganisationsDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid ID was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Secured({PUI_ORGANISATION_MANAGER, PUI_FINANCE_MANAGER, PUI_CASE_MANAGER})
    public ResponseEntity<OrganisationEntityResponse> retrieveOrganisationUsingOrgIdentifier(
            @ApiParam(hidden = true) @OrgId String extOrgIdentifier) {

        return retrieveOrganisationOrById(extOrgIdentifier);
    }

    @ApiOperation(
            value = "Retrieves an Organisation's Payment Accounts with a User's Email Address",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The Organisation's associated Payment Accounts",
                    response = OrganisationPbaResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid Email Address was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Payment Accounts found with the given Email Address"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            path = "/pbas",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({PUI_FINANCE_MANAGER, PUI_USER_MANAGER, PUI_ORGANISATION_MANAGER, PUI_CASE_MANAGER})
    public ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountByEmail(@NotNull @RequestParam("email") String email, @ApiParam(hidden = true) @OrgId String orgId) {
        //Received request to retrieve an organisations payment accounts by email for external

        return retrievePaymentAccountByUserEmail(email, orgId);
    }

    @ApiOperation(
            value = "Add a new User to an Organisation",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "The new User has been added to the Organisation",
                    response = OrganisationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation found with the given ID to add new User to"
            ),
            @ApiResponse(
                    code = 409,
                    message = "A User already exists with the given Email Address"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @PostMapping(
            path = "/users/",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured(PUI_USER_MANAGER)
    public ResponseEntity addUserToOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @ApiParam(hidden = true) @OrgId String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        //Received request to add a new user to an organisation for external

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier, userId);

    }

    protected ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountByUserEmail(String email, String extOrgIdentifier) {
        validateEmail(email);
        Organisation organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());

        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        serviceAndUserDetails.getAuthorities();

        organisationIdentifierValidatorImpl.verifyNonPuiFinanceManagerOrgIdentifier(serviceAndUserDetails.getAuthorities(), organisation, extOrgIdentifier);
        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false));
    }


    protected ResponseEntity<OrganisationEntityResponse> retrieveOrganisationOrById(String id) {

        OrganisationEntityResponse organisationResponse = null;
        //Received request to retrieve External organisation with ID
        organisationResponse =
                organisationService.retrieveOrganisation(id);
        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }
}
