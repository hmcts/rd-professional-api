package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.FORBIDDEN_ERROR_ACCESS_DENIED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PRD_ADMIN;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;

@RequestMapping(
        path = "refdata/internal/v1/organisations"
)
@Validated
@RestController
@Slf4j
@NoArgsConstructor
public class OrganisationInternalController extends SuperController {

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
    @ResponseBody
    public ResponseEntity<OrganisationResponse> createOrganisation(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for internal users
        return createOrganisationFrom(organisationCreationRequest);
    }

    @ApiOperation(
            value = "Retrieves all Organisations filtered by given Status or one Organisation if ID is given",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            allowEmptyValue = true,
            required = true
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Details of one or more Organisations",
                    response = OrganisationsDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Invalid request (Status or ID) provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation(s) found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @Secured(PRD_ADMIN)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveOrganisations(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @ApiParam(name = "id") @RequestParam(value = "id", required = false) String id,
            @ApiParam(name = "status") @RequestParam(value = "status", required = false) String status) {

        return retrieveAllOrganisationOrById(id, status);
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
    @Secured(PRD_ADMIN)
    public ResponseEntity retrievePaymentAccountBySuperUserEmail(@NotNull @RequestParam("email") String email) {
        //Received request to retrieve an organisations payment accounts by email for internal
        return retrievePaymentAccountByUserEmail(email);
    }

    @ApiOperation(
            value = "Edit the PBAs of an Organisation by Organisation ID",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The Organisation's associated Payment Accounts",
                    response = PbaResponse.class
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
                    message = "No Organisation found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @PutMapping(
            path = "/{orgId}/pbas",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured(PRD_ADMIN)
    public ResponseEntity editPaymentAccountsByOrgId(@Valid @NotNull @RequestBody PbaEditRequest pbaEditRequest,
                                                     @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier) {
        log.info("Received request to edit payment accounts by organisation Id...");

        paymentAccountValidator.validatePaymentAccounts(pbaEditRequest.getPaymentAccounts(), organisationIdentifier);
        Optional<Organisation> organisation = Optional.ofNullable(organisationService.getOrganisationByOrgIdentifier(organisationIdentifier));

        if (!organisation.isPresent()) {
            throw new EmptyResultDataAccessException(1);
        }

        paymentAccountService.deleteUserAccountMaps(organisation.get());
        paymentAccountService.deletePaymentAccountsFromOrganisation(organisation.get());
        paymentAccountService.addPaymentAccountsToOrganisation(pbaEditRequest, organisation.get());
        PbaResponse response = paymentAccountService.addUserAndPaymentAccountsToUserAccountMap(organisation.get());

        return ResponseEntity
                .status(200)
                .body(response);
    }

    @ApiOperation(
            value = "Updates an Organisation",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Organisation has been updated"
            ),
            @ApiResponse(code = 400,
                    message = "If Organisation request sent with null/invalid values for mandatory fields"
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
    @PutMapping(
            value = "/{orgId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @Secured(PRD_ADMIN)
    public ResponseEntity updatesOrganisation(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        return updateOrganisationById(organisationCreationRequest, organisationIdentifier, userId);
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
            path = "/{orgId}/users/",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @Secured(PRD_ADMIN)
    public ResponseEntity addUserToOrganisation(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        //Received request to add a internal new user to an organisation

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier, userId);
    }
}
