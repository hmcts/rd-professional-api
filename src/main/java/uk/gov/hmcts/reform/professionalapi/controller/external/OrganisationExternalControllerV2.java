package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_ID_NOTES_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_ID_NOTES_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_ID_NOTES_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_PBA_EMAIL_NOTES_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_PBA_EMAIL_NOTES_2;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.checkOrganisationAndPbaExists;

@RequestMapping(
        path = "refdata/external/v2/organisations"
)
@RestController
@Slf4j
@SuppressWarnings("checkstyle:Indentation")
public class OrganisationExternalControllerV2 extends SuperController {
    @Operation(
            summary = "Creates an Organisation",
            description = "**IDAM Roles to access API**: <br> No role restriction",
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
    public ResponseEntity<OrganisationResponse> createOrganisationUsingExternalController(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "organisationCreationRequest")
            @Validated @NotNull @RequestBody OrganisationOtherOrgsCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for external user
        return createOrganisationFrom(organisationCreationRequest);
    }

    @Operation(
            summary = "Retrieves Organisation details of the requesting User",
            description = GET_ORG_BY_ID_NOTES_1 + GET_ORG_BY_ID_NOTES_2 + GET_ORG_BY_ID_NOTES_3,
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "Details of an Organisation",
            content = @Content(schema = @Schema(implementation = OrganisationEntityResponseV2.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid ID was provided",
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

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Secured({"pui-organisation-manager", "pui-finance-manager", "pui-case-manager", "pui-caa", "pui-user-manager"})
    public ResponseEntity<OrganisationEntityResponseV2> retrieveOrganisationUsingOrgIdentifier(
            @Parameter(hidden = true) @OrgId String extOrgIdentifier,
            @Parameter(name = "pbaStatus") @RequestParam(value = "pbaStatus", required = false) String pbaStatus) {

        boolean isPendingPbaRequired = true;

        if (!isBlank(pbaStatus)
                && pbaStatus.equalsIgnoreCase(ACCEPTED.name())) {
            isPendingPbaRequired = false;
        }

        return retrieveAllOrganisationOrByIdForV2Api(extOrgIdentifier, isPendingPbaRequired);
    }

    protected ResponseEntity<OrganisationEntityResponseV2> retrieveAllOrganisationOrByIdForV2Api(
            String id, boolean isPendingPbaRequired) {
        //Received request to retrieve External organisation with ID

        var organisationResponse = organisationService.retrieveOrganisationForV2Api(id, isPendingPbaRequired);

        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    @Operation(
            summary = "Retrieves an Organisation's Payment Accounts with a User's Email Address",
            description = GET_PBA_EMAIL_NOTES_1 + GET_PBA_EMAIL_NOTES_2,
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
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager"})
    public ResponseEntity<OrganisationPbaResponseV2> retrievePaymentAccountByEmail(@Parameter(hidden = true)
                                                                                       @OrgId String orgId) {
        //Received request to retrieve an organisations payment accounts by email for external
        var userEmail = getUserEmailFromHeader();
        return retrievePaymentAccountByUserEmail(userEmail, orgId);
    }

    protected ResponseEntity<OrganisationPbaResponseV2> retrievePaymentAccountByUserEmail(String email,
                                                                                        String extOrgIdentifier) {
        validateEmail(email);
        var organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());

        checkOrganisationAndPbaExists(organisation);

        var userInfo = idamRepository.getUserInfo(getUserToken());

        organisationIdentifierValidatorImpl.verifyNonPuiFinanceManagerOrgIdentifier(userInfo.getRoles(),
                organisation, extOrgIdentifier);
        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponseV2(organisation,
                        false, true, false,true));
    }

}
