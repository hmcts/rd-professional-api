package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.checkOrganisationAndPbaExists;

@RequestMapping(
        path = "refdata/external/v1/organisations"
)
@RestController
@Slf4j
public class OrganisationExternalController extends SuperController {


    @ApiOperation(
            value = "Creates an Organisation",
            notes = "**IDAM Roles to access API**: \n No role restriction",
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
    public ResponseEntity<OrganisationResponse> createOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for external user
        return createOrganisationFrom(organisationCreationRequest);
    }

    @ApiOperation(
            value = "Retrieves Organisation details of the requesting User",
            notes = "**IDAM Roles to access API** :\n pui-organisation-manager,\n pui-finance-manager,"
                    + "\n pui-case-manager,\n pui-caa,\n pui-user-manager",
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
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Secured({"pui-organisation-manager", "pui-finance-manager", "pui-case-manager", "pui-caa", "pui-user-manager"})
    public ResponseEntity<OrganisationEntityResponse> retrieveOrganisationUsingOrgIdentifier(
            @ApiParam(hidden = true) @OrgId String extOrgIdentifier,
            @ApiParam(name = "pbaStatus") @RequestParam(value = "pbaStatus", required = false) String pbaStatus) {

        boolean isPendingPbaRequired = true;

        if (!isBlank(pbaStatus)
            && pbaStatus.equalsIgnoreCase(ACCEPTED.name())) {
            isPendingPbaRequired = false;
        }

        return retrieveOrganisationOrById(extOrgIdentifier, isPendingPbaRequired);
    }

    @ApiOperation(
            value = "Retrieves an Organisation's Payment Accounts with a User's Email Address",
            notes = "**IDAM Roles to access API** : \n pui-finance-manager,\n pui-user-manager,"
                    + "\n pui-organisation-manager,\n pui-case-manager",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization"),
                    @Authorization(value = "UserEmail")
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
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager"})
    public ResponseEntity<OrganisationPbaResponse>
        retrievePaymentAccountByEmail(@ApiParam(hidden = true) @OrgId String orgId) {
        //Received request to retrieve an organisations payment accounts by email for external
        var userEmail = getUserEmailFromHeader();
        return retrievePaymentAccountByUserEmail(userEmail, orgId);
    }

    @ApiOperation(
            value = "Add a new User to an Organisation",
            notes = "**IDAM Roles to access API** :\n pui-user-manager",
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
                    message = "A User already exists with the given Email Address or is already active in SIDAM "
                            + "during resend invite"
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
            path = "/users/",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("pui-user-manager")
    public ResponseEntity<Object> addUserToOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @ApiParam(hidden = true) @OrgId String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        //Received request to add a new user to an organisation for external
        professionalUserService.checkUserStatusIsActiveByUserId(userId);

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier);

    }

    @ApiOperation(
            value = "Retrieves all Active Organisations of requested status for user"
                    + " with minimal e.g. organisationIdentifier, name and contact information if address flag is true",
            notes = "**IDAM Roles to access API** : \n pui-organisation-manager,\n pui-finance-manager,"
                    + "\n pui-case-manager,pui-caa,\n pui-user-manager,citizen,caseworker",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully retrieved list of all Organisations of"
                            + " requested status with minimal information",
                    response = OrganisationMinimalInfoResponse.class,
                    responseContainer = "list"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied for either invalid permissions or user is pending"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation found"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
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

    @ApiOperation(
            value = "Deletes the provided list of payment accounts from the organisation.",
            notes = "**IDAM Roles to access API** : \n - pui-finance-manager",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "Successfully deleted the list of provided payment accounts from the organisation."
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request Error: One of the below reasons: \n"
                            + "- Organisation is not ACTIVE.\n"
                            + "- No payment accounts passed to be deleted in the request body.\n"
                            + "- Passed payment account numbers are in an invalid format.\n"
                            + "- The payment accounts are not associated with users organisation"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied for either invalid permissions or user is pending"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Resource Not Found Error: The Organisation does not exist"
                            + " to delete Payment Accounts from"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @DeleteMapping(path = "/pba")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Secured({"pui-finance-manager"})
    public void deletePaymentAccountsOfOrganisation(
            @Valid @NotNull @RequestBody PbaRequest deletePbaRequest,
            @ApiParam(hidden = true) @OrgId String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        deletePaymentAccountsOfGivenOrganisation(deletePbaRequest, organisationIdentifier, userId);

    }

    protected ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountByUserEmail(String email,
                                                                                        String extOrgIdentifier) {
        validateEmail(email);
        var organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());

        checkOrganisationAndPbaExists(organisation);

        var userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();

        organisationIdentifierValidatorImpl.verifyNonPuiFinanceManagerOrgIdentifier(userInfo.getRoles(),
                organisation, extOrgIdentifier);
        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false, false, true));
    }


    protected ResponseEntity<OrganisationEntityResponse> retrieveOrganisationOrById(
            String id, boolean isPendingPbaRequired) {
        //Received request to retrieve External organisation with ID

        var organisationResponse = organisationService.retrieveOrganisation(id, isPendingPbaRequired);

        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    @ApiOperation(
            value = "Add multiple PBAs associated with their organisation",
            notes = "**IDAM Roles to access API** :\n pui-finance-manager",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "All PBAs got added successfully or Partial success",
                    response = AddPbaResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "PBA number invalid or Duplicate PBA or Organisation is not active"
            ),
            @ApiResponse(
                    code = 401,
                    message = "S2S unauthorised"
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
            path = "/pba",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("pui-finance-manager")
    public ResponseEntity<Object> addPaymentAccountsToOrganisation(
            @Valid @NotNull @RequestBody PbaRequest pbaRequest,
            @ApiParam(hidden = true) @OrgId String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        log.info("Received request to add payment accounts to organisation Id");

        return organisationService.addPaymentAccountsToOrganisation(pbaRequest, organisationIdentifier, userId);

    }

    @ApiOperation(
            value = "Adds contact informations(address details) to organisation",
            notes = "**IDAM Roles to access API** :\n pui-organisation-manager",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }

    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = ""
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
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
                    code = 500,
                    message = "Internal Server Error"
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
            @ApiParam(hidden = true) @OrgId String organisationIdentifier) {


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

    @ApiOperation(
            value = "Deletes the Contact Information Address of an Organisation.",
            notes = "**IDAM Roles to access API** : \n - pui-organisation-manager",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "Successfully deleted the list of Contact Information Address of an Organisation."
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request Error: One of the below reasons: \n"
                            + "- Request is malformed.\n"
                            + "- Organisation id is missing.\n"
                            + "- Organisation should have at least one address."
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied for either invalid permissions or user is pending"
            ),
            @ApiResponse(
                    code = 404,
                    message = "NOT FOUND Error: One of the below reasons: \n"
                            + "- Organisation does not exist.\n"
                            + "- Request is empty.\n"
                            + "- id1, id2 does not exist\n"
                            + "OR\n"
                            + "id1, id2 does not belong to given org."
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @DeleteMapping(path = "/addresses")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Secured({"pui-organisation-manager"})
    public void deleteMultipleAddressesOfOrganisation(
            @Valid @NotNull @RequestBody List<DeleteMultipleAddressRequest> deleteRequest,
            @ApiParam(hidden = true) @OrgId String organisationIdentifier) {

        deleteMultipleAddressOfGivenOrganisation(deleteRequest, organisationIdentifier);

    }


}
