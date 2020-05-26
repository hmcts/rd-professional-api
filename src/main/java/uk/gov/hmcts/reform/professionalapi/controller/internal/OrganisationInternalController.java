package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
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
                    message = "Forbidden Error: Access denied"
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
                    message = "Forbidden Error: Access denied"
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

    @Secured("prd-admin")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveOrganisations(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @ApiParam(name = "id", required = false) @RequestParam(value = "id", required = false) String id,
            @ApiParam(name = "status", required = false) @RequestParam(value = "status", required = false) String status) {

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
                    message = "Forbidden Error: Access denied"
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
    @Secured("prd-admin")
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
                    message = "The Payment Account's have been updated",
                    response = PbaResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
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
    @Secured("prd-admin")
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
                    message = "Organisation has been updated",
                    response = String.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "If Organisation request sent with null/invalid values for mandatory fields"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
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
    @Secured("prd-admin")
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
                    response = NewUserResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation found with the given ID to add new User to"
            ),
            @ApiResponse(
                    code = 409,
                    message = "A User already exists with the given Email Address or is already active in SIDAM during resend invite"
            ),
            @ApiResponse(
                    code = 429,
                    message = "Too many requests for resend invite"
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
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity addUserToOrganisation(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        //Received request to add a internal new user to an organisation

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier, userId);
    }

    @ApiOperation(
            value = "Delete an Organisation",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "A representation of the Deleted organisation",
                    response = DeleteOrganisationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "The orgId provided for an organisation is not valid or organisation admin is not in Pending state "
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not Found"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )

    })
    @DeleteMapping(
            value = "/{orgId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity<DeleteOrganisationResponse> deleteOrganisation(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier) {

        DeleteOrganisationResponse deleteOrganisationResponse;
        //Received request to delete an organisation for internal user
        Optional<Organisation> organisation = Optional.ofNullable(organisationService.getOrganisationByOrgIdentifier(organisationIdentifier));

        if (!organisation.isPresent()) {

            throw new EmptyResultDataAccessException(1);

        }

        deleteOrganisationResponse = organisationService.deleteOrganisation(organisation.get());
        return ResponseEntity
                .status(deleteOrganisationResponse.getStatusCode())
                .body(deleteOrganisationResponse);
    }
}
