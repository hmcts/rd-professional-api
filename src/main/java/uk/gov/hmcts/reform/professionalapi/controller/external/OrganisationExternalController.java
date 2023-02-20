package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_400_MESSAGE_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_400_MESSAGE_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_400_MESSAGE_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_400_MESSAGE_4;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_404_MESSAGE_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_404_MESSAGE_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_404_MESSAGE_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_404_MESSAGE_4;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_404_MESSAGE_5;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETE_ORG_ADD_404_MESSAGE_6;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEL_ORG_PBA_NOTES_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEL_ORG_PBA_NOTES_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEL_ORG_PBA_NOTES_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEL_ORG_PBA_NOTES_4;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DEL_ORG_PBA_NOTES_5;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_ID_NOTES_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_ID_NOTES_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_ID_NOTES_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_STATUS_NOTES_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_STATUS_NOTES_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_ORG_BY_STATUS_NOTES_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_PBA_EMAIL_NOTES_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_PBA_EMAIL_NOTES_2;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.checkOrganisationAndPbaExists;

@RequestMapping(
        path = "refdata/external/v1/organisations"
)
@RestController
@Slf4j
public class OrganisationExternalController extends SuperController {


    @Operation(
            summary = "Creates an Organisation",
            description = "**IDAM Roles to access API**: <br> No role restriction",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "The Organisation Identifier of the created Organisation",
                    content = @Content(schema = @Schema(implementation = OrganisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
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

    @Operation(
            summary = "Retrieves Organisation details of the requesting User",
            description = GET_ORG_BY_ID_NOTES_1 + GET_ORG_BY_ID_NOTES_2 + GET_ORG_BY_ID_NOTES_3,
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Details of an Organisation",
                    content = @Content(schema = @Schema(implementation = OrganisationsDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid ID was provided"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No Organisation found with the given ID"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Secured({"pui-organisation-manager", "pui-finance-manager", "pui-case-manager", "pui-caa", "pui-user-manager"})
    public ResponseEntity<OrganisationEntityResponse> retrieveOrganisationUsingOrgIdentifier(
            @Parameter(hidden = true) @OrgId String extOrgIdentifier,
            @Parameter(name = "pbaStatus") @RequestParam(value = "pbaStatus", required = false) String pbaStatus) {

        boolean isPendingPbaRequired = true;

        if (!isBlank(pbaStatus)
                && pbaStatus.equalsIgnoreCase(ACCEPTED.name())) {
            isPendingPbaRequired = false;
        }

        return retrieveOrganisationOrById(extOrgIdentifier, isPendingPbaRequired);
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The Organisation's associated Payment Accounts",
                    content = @Content(schema = @Schema(implementation = OrganisationPbaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid Email Address was provided"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No Payment Accounts found with the given Email Address"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @GetMapping(
            path = "/pbas",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager"})
    public ResponseEntity<OrganisationPbaResponse>
        retrievePaymentAccountByEmail(@Parameter(hidden = true) @OrgId String orgId) {
        //Received request to retrieve an organisations payment accounts by email for external
        var userEmail = getUserEmailFromHeader();
        return retrievePaymentAccountByUserEmail(userEmail, orgId);
    }

    @Operation(
            summary = "Add a new User to an Organisation",
            description = "**IDAM Roles to access API** :<br> pui-user-manager",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "The new User has been added to the Organisation",
                    content = @Content(schema = @Schema(implementation = NewUserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request was provided"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No Organisation found with the given ID to add new User to"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A User already exists with the given Email Address or is already active in SIDAM "
                            + "during resend invite"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests for resend invite"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @PostMapping(
            path = "/users/",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("pui-user-manager")
    public ResponseEntity<Object> addUserToOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @Parameter(hidden = true) @OrgId String organisationIdentifier,
            @Parameter(hidden = true) @UserId String userId) {

        //Received request to add a new user to an organisation for external
        professionalUserService.checkUserStatusIsActiveByUserId(userId);

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier);

    }

    @Operation(
            summary = "Retrieves all Active Organisations of requested status for user"
                    + " with minimal e.g. organisationIdentifier, name and contact information if address flag is true",
            description = GET_ORG_BY_STATUS_NOTES_1 + GET_ORG_BY_STATUS_NOTES_2 + GET_ORG_BY_STATUS_NOTES_3,
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of all Organisations of"
                            + " requested status with minimal information",
                    content = @Content(array = @ArraySchema(schema =
                                        @Schema(implementation = OrganisationMinimalInfoResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied for either invalid permissions or user is pending"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No Organisation found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : "
                            + "The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @GetMapping(
            path = "/status/{status}",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-organisation-manager", "pui-finance-manager", "pui-case-manager", "pui-caa", "pui-user-manager",
            "citizen", "caseworker"})
    public ResponseEntity<List<OrganisationMinimalInfoResponse>>
        retrieveOrganisationsByStatusWithAddressDetailsOptional(
            @PathVariable("status") String status,
            @RequestParam(value = "address", required = false, defaultValue = "false") boolean address) {

        return retrieveAllOrganisationsByStatus(status, address);
    }

    @Operation(
            summary = "Deletes the provided list of payment accounts from the organisation.",
            description = "**IDAM Roles to access API** : <br> - pui-finance-manager",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully deleted the list of provided payment accounts from the organisation."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = DEL_ORG_PBA_NOTES_1 + DEL_ORG_PBA_NOTES_2 + DEL_ORG_PBA_NOTES_3
                            + DEL_ORG_PBA_NOTES_4 + DEL_ORG_PBA_NOTES_5
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : "
                            + "The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: "
                            + "Access denied for either invalid permissions or user is pending"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource Not Found Error: The Organisation does not exist"
                            + " to delete Payment Accounts from"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @DeleteMapping(path = "/pba")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Secured({"pui-finance-manager"})
    public void deletePaymentAccountsOfOrganisation(
            @Valid @NotNull @RequestBody PbaRequest deletePbaRequest,
            @Parameter(hidden = true) @OrgId String organisationIdentifier,
            @Parameter(hidden = true) @UserId String userId) {

        deletePaymentAccountsOfGivenOrganisation(deletePbaRequest, organisationIdentifier, userId);

    }

    protected ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountByUserEmail(String email,
                                                                                        String extOrgIdentifier) {
        validateEmail(email);
        var organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());

        checkOrganisationAndPbaExists(organisation);

        var userInfo = idamRepository.getUserInfo(getUserToken());

        organisationIdentifierValidatorImpl.verifyNonPuiFinanceManagerOrgIdentifier(userInfo.getRoles(),
                organisation, extOrgIdentifier);
        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false, true, false));
    }


    protected ResponseEntity<OrganisationEntityResponse> retrieveOrganisationOrById(
            String id, boolean isPendingPbaRequired) {
        //Received request to retrieve External organisation with ID

        var organisationResponse = organisationService.retrieveOrganisation(id, isPendingPbaRequired);

        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    @Operation(
            summary = "Add multiple PBAs associated with their organisation",
            description = "**IDAM Roles to access API** :<br> pui-finance-manager",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "All PBAs got added successfully or Partial success",
                    content = @Content(schema = @Schema(implementation = AddPbaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "PBA number invalid or Duplicate PBA or Organisation is not active"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "S2S unauthorised"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @PostMapping(
            path = "/pba",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("pui-finance-manager")
    public ResponseEntity<Object> addPaymentAccountsToOrganisation(
            @Valid @NotNull @RequestBody PbaRequest pbaRequest,
            @Parameter(hidden = true) @OrgId String organisationIdentifier,
            @Parameter(hidden = true) @UserId String userId) {

        log.info("Received request to add payment accounts to organisation Id");

        return organisationService.addPaymentAccountsToOrganisation(pbaRequest, organisationIdentifier, userId);

    }

    @Operation(
            summary = "Adds contact informations(address details) to organisation",
            description = "**IDAM Roles to access API** :<br> pui-organisation-manager",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }

    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = ""
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : "
                            + "The requested resource is restricted and requires authentication"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })

    @PostMapping(
            path = "/addresses",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured({"pui-organisation-manager"})
    public ResponseEntity<Void> addContactInformationsToOrganisation(
            @Valid @NotNull @RequestBody List<ContactInformationCreationRequest> contactInformationCreationRequests,
            @Parameter(hidden = true) @OrgId String organisationIdentifier) {


        organisationCreationRequestValidator.validateContactInformations(contactInformationCreationRequests);


        var organisation = Optional.ofNullable(organisationService
                .getOrganisationByOrgIdentifier(organisationIdentifier));

        if (organisation.isEmpty()) {
            throw new ResourceNotFoundException("Organisation does not exist");
        }

        organisationService.addContactInformationsToOrganisation(
                contactInformationCreationRequests,
                organisationIdentifier);

        return ResponseEntity
                .status(201)
                .body(null);

    }

    @Operation(
            summary = "Deletes the Contact Information Address of an Organisation.",
            description = "**IDAM Roles to access API** : <br> - pui-organisation-manager",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully deleted the list of Contact Information Address of an Organisation."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = DELETE_ORG_ADD_400_MESSAGE_1 + DELETE_ORG_ADD_400_MESSAGE_2
                            + DELETE_ORG_ADD_400_MESSAGE_3 + DELETE_ORG_ADD_400_MESSAGE_4
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : "
                            + "The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied for either invalid permissions or user is pending"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = DELETE_ORG_ADD_404_MESSAGE_1 + DELETE_ORG_ADD_404_MESSAGE_2
                            + DELETE_ORG_ADD_404_MESSAGE_3
                            + DELETE_ORG_ADD_404_MESSAGE_4 + DELETE_ORG_ADD_404_MESSAGE_5
                            + DELETE_ORG_ADD_404_MESSAGE_6
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @DeleteMapping(path = "/addresses")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Secured({"pui-organisation-manager"})
    public void deleteMultipleAddressesOfOrganisation(
            @Valid @NotNull @RequestBody List<DeleteMultipleAddressRequest> deleteRequest,
            @Parameter(hidden = true) @OrgId String organisationIdentifier) {

        deleteMultipleAddressOfGivenOrganisation(deleteRequest, organisationIdentifier);

    }


}
