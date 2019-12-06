package uk.gov.hmcts.reform.professionalapi.controller.external;

import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator.validateEmail;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
                    message = "A representation of the created organisation",
                    response = OrganisationResponse.class
            )
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<OrganisationResponse> createOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for external user
        return createOrganisationFrom(organisationCreationRequest);
    }

    @ApiOperation(
            value = "Retrieves organisation details based on id",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Details of one organisation",
                    response = OrganisationsDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Invalid  id provided for an organisation"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not Found"
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Secured({"pui-organisation-manager", "pui-finance-manager"})
    public ResponseEntity<OrganisationEntityResponse> retrieveOrganisationUsingOrgIdentifier(
            @ApiParam(hidden = true)@OrgId  String extOrgIdentifier) {

        return retrieveOrganisationOrById(extOrgIdentifier);
    }

    @ApiOperation(
            value = "Retrieves an organisations payment accounts by super user email",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The organisations associated payment accounts",
                    response = OrganisationPbaResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not Found"
            )
    })
    @GetMapping(
            path = "/pbas",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager"})
    public ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountByEmail(@NotNull @RequestParam("email") String email, @ApiParam(hidden = true)@OrgId  String orgId) {
        //Received request to retrieve an organisations payment accounts by email for external

        return retrievePaymentAccountByUserEmail(email, orgId);
    }


    @ApiOperation(
        value = "Add an user to an organisation",
        authorizations = {
            @Authorization(value = "ServiceAuthorization"),
            @Authorization(value = "Authorization")
        }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "User has been added",
                    response = OrganisationResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden"
            )
    })
    @PostMapping(
            path = "/users/",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    @Secured("pui-user-manager")
    public ResponseEntity addUserToOrganisationUsingExternalController(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @ApiParam(hidden = true)@OrgId String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        //Received request to add a new user to an organisation for external

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier, userId);

    }

    protected ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountByUserEmail(String email, String extOrgIdentifier) {
        validateEmail(email);
        //In retrievePaymentAccountByUserEmail method
        Organisation organisation = paymentAccountService.findPaymentAccountsByEmail(email.toLowerCase());
        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisation, extOrgIdentifier);
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        serviceAndUserDetails.getAuthorities();

        organisationIdentifierValidatorImpl.verifyNonPuiFinanceManagerOrgIdentifier(serviceAndUserDetails.getAuthorities(), organisation,extOrgIdentifier);
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
