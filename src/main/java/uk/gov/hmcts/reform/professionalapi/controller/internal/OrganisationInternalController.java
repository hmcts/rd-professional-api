package uk.gov.hmcts.reform.professionalapi.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdatePbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UpdatePbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE;

@RequestMapping(
        path = "refdata/internal/v1/organisations"
)
@Validated
@RestController
@Slf4j
@NoArgsConstructor
public class OrganisationInternalController extends SuperController {

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Operation(
            summary = "Creates an Organisation",
            description = "**IDAM Roles to access API** : <br> No role restriction",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization")
            }
    )

    @ApiResponse(
            responseCode = "201",
            description = "The Organisation Identifier of the created Organisation",
            content = @Content(schema = @Schema(implementation = OrganisationResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid request has been provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<OrganisationResponse> createOrganisation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "organisationCreationRequest")
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for internal users
        return createOrganisationFrom(organisationCreationRequest);
    }

    @Operation(
            summary = "Retrieves all Organisations filtered by given Status or one Organisation if ID is given",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )


    @ApiResponse(
            responseCode = "200",
            description = "Details of one or more Organisations",
            content = @Content(schema = @Schema(implementation = OrganisationsDetailResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request (Status or ID) provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation(s) found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )


    @Secured("prd-admin")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> retrieveOrganisations(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @Parameter(name = "id") @RequestParam(value = "id", required = false) String id,
            @Parameter(name = "status") @RequestParam(value = "status", required = false) String status,
            @Parameter(name = "page") @RequestParam(value = "page", required = false) Integer page,
            @Parameter(name = "size") @RequestParam(value = "size", required = false) Integer size) {

        return retrieveAllOrganisationOrById(id, status, page, size);
    }


    @Operation(
            summary = "Retrieves an Organisation's Payment Accounts with a User's Email Address",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization"),
                    @SecurityRequirement(name = "UserEmail")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "The Organisation's associated Payment Accounts",
            content = @Content(schema = @Schema(implementation = OrganisationPbaResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid Email Address was provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Payment Accounts found with the given Email Address",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @GetMapping(
            path = "/pbas",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<Object> retrievePaymentAccountBySuperUserEmail() {

        var userEmail = getUserEmailFromHeader();
        //Received request to retrieve an organisations payment accounts by email for internal
        return retrievePaymentAccountByUserEmail(userEmail);
    }

    @Operation(
            summary = "Edit the PBAs of an Organisation by Organisation ID",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "The Payment Account's have been updated",
            content = @Content(schema = @Schema(implementation = PbaResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid request was provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PutMapping(
            path = "/{orgId}/pbas",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<Object> editPaymentAccountsByOrgId(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "pbaEditRequest")
            @Valid @NotNull @RequestBody PbaRequest pbaEditRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX,
                    message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier) {

        log.info("{}:: Received request to edit payment accounts by organisation Id...", loggingComponentName);

        Optional<Organisation> organisation = Optional.ofNullable(organisationService
                .getOrganisationByOrgIdentifier(organisationIdentifier));

        if (organisation.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        paymentAccountValidator.validatePaymentAccounts(
                pbaEditRequest.getPaymentAccounts(), organisation.get(), false);

        var response = paymentAccountService
                .editPaymentAccountsByOrganisation(organisation.get(), pbaEditRequest);

        return ResponseEntity
                .status(200)
                .body(response);
    }

    @Operation(
            summary = "Updates an Organisation",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })

    @ApiResponse(
            responseCode = "200",
            description = "Organisation has been updated",
            content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "If Organisation request sent with null/invalid values for mandatory fields",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PutMapping(
            value = "/{orgId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity<Object> updatesOrganisation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "organisationCreationRequest")
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @Parameter(hidden = true) @UserId String userId) {

        return updateOrganisationById(organisationCreationRequest, organisationIdentifier);
    }

    @Operation(
            summary = "Add a new User to an Organisation",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "201",
            description = "The new User has been added to the Organisation",
            content = @Content(schema = @Schema(implementation = NewUserResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid request was provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation found with the given ID to add new User to",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "A User already exists with the given "
                    + "Email Address or is already active in SIDAM during resend invite",
            content = @Content
    )
    @ApiResponse(
            responseCode = "429",
            description = "Too many requests for resend invite",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PostMapping(
            path = "/{orgId}/users/",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity<Object> addUserToOrganisation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "newUserCreationRequest")
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @Parameter(hidden = true) @UserId String userId) {

        //Received request to add a internal new user to an organisation

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier);
    }

    @Operation(
            summary = "Delete an Organisation",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "204",
            description = "A representation of the Deleted organisation",
            content = @Content(schema = @Schema(implementation = DeleteOrganisationResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "The orgId provided for an organisation is not valid or organisation admin is not in "
                    + "Pending state ",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Error : "
                    + "The requested resource is restricted and requires authentication",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )


    @DeleteMapping(
            value = "/{orgId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity<DeleteOrganisationResponse> deleteOrganisation(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @Parameter(hidden = true) @UserId String userId) {

        Optional<Organisation> organisation = Optional.ofNullable(organisationService
                .getOrganisationByOrgIdentifier(organisationIdentifier));

        if (organisation.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }

        var deleteOrganisationResponse =
                organisationService.deleteOrganisation(organisation.get(), userId);

        return ResponseEntity
                .status(deleteOrganisationResponse.getStatusCode())
                .body(deleteOrganisationResponse);
    }

    @Operation(
            summary = "Updates the MFA preference of an Organisation",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "The MFA preference of the organisation has been successfully updated",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid request was provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Error : "
                    + "The requested resource is restricted and requires authentication",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation was found with the given organisationIdentifier",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PutMapping(
            path = "/{orgId}/mfa",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<Object> updateOrgMfaStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "mfaUpdateRequest")
            @Valid @NotNull @RequestBody MfaUpdateRequest mfaUpdateRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX,
                    message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier) {

        log.info("{}:: Received request to update organisation mfa preference::", loggingComponentName);

        organisationIdentifierValidatorImpl.validateOrganisationExistsWithGivenOrgId(organisationIdentifier);

        var organisation = organisationService.getOrganisationByOrgIdentifier(organisationIdentifier);

        if (isNotTrue(organisation.isOrganisationStatusActive())) {
            throw new InvalidRequest(ORG_NOT_ACTIVE);
        }

        return mfaStatusService.updateOrgMfaStatus(mfaUpdateRequest, organisation);
    }

    @Operation(
            summary = "Retrieves the list of organisations with particular PBA status",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "",
            content = @Content(schema = @Schema(implementation = OrganisationsWithPbaStatusResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "PBA status is not valid",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @GetMapping(
            path = "/pba/{status}",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<Object> retrieveOrgByPbaStatus(@PathVariable("status") @NotBlank String pbaStatus) {

        log.info("{}:: Received request to retrieve organisations by pba status::", loggingComponentName);
        return organisationService.getOrganisationsByPbaStatus(pbaStatus.toUpperCase());
    }

    @Operation(
            summary = "Review (Accept or Reject) an Organisation's registered PBAs ",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "Success: The requested PBAs have been successfully Updated - OR - "
                    + "Partial Success: Some of the requested PBAs have been successfully Updated"
                    + " and the invalid ones returned with individual error message",
            content = @Content(schema = @Schema(implementation = UpdatePbaStatusResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid request was provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "422",
            description = "All requested PBAs failed to update",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PutMapping(
            path = "/{orgId}/pba/status",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity<Object> updateAnOrganisationsRegisteredPbas(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "updatePbaRequest")
            @Valid @NotNull @RequestBody UpdatePbaRequest updatePbaRequest,
            @PathVariable("orgId") @NotBlank String organisationIdentifier) {

        //Received request to update an Organisation's PBAs

        paymentAccountValidator.checkUpdatePbaRequestIsValid(updatePbaRequest);

        organisationIdentifierValidatorImpl.validateOrganisationExistsAndActive(organisationIdentifier);

        var updatePbaStatusResponse =
                updateAnOrganisationsPbas(updatePbaRequest.getPbaRequestList(), organisationIdentifier);

        return ResponseEntity
                .status(updatePbaStatusResponse.getStatusCode())
                .body(updatePbaStatusResponse);
    }

    @Operation(
            summary = "Retrieves the organisation details of a user",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "",
            content = @Content(schema = @Schema(implementation = OrganisationEntityResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @GetMapping(
            path = "/orgDetails/{userId}}",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<Object> retrieveOrganisationByUserId(@PathVariable("userId") @NotBlank String userId) {
        return organisationService.retrieveOrganisationByUserId(userId);
    }
}
