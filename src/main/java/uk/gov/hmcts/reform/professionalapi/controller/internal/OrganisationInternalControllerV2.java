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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponseV2;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;

@RequestMapping(
        path = "refdata/internal/v2/organisations"
)
@Validated
@RestController
@Slf4j
@NoArgsConstructor
public class OrganisationInternalControllerV2 extends SuperController {
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
            @Valid @NotNull @RequestBody OrganisationOtherOrgsCreationRequest organisationCreationRequest) {

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
            content = @Content(schema = @Schema(implementation = OrganisationsDetailResponseV2.class))
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

        return retrieveAllOrganisationOrByIdForV2Api(id, status, page, size);
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
            content = @Content(schema = @Schema(implementation = OrganisationPbaResponseV2.class))
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
        return retrievePaymentAccountByUserEmailForV2Api(userEmail);
    }
}