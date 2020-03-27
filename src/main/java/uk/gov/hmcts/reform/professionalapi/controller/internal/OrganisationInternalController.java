package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;

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
                    message = "A representation of the created organisation",
                    response = OrganisationResponse.class
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
            value = "Retrieves all organisation details for external users if no value entered then get all org details or based on id or status, if both values present then get the details based on id",
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
                    message = "Details of one or more organisations",
                    response = OrganisationsDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Invalid status or id provided for an organisation"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Data not found"
            )
    })

    @Secured("prd-admin")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveOrganisations(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @ApiParam(name = "id", required = false) @RequestParam(value = "id", required = false) String id,
            @ApiParam(name = "status", required = false) @RequestParam(value = "status", required = false) String status) {

        return retrieveAllOrganisationOrById(id, status);
    }


    @ApiOperation(
            value = "Retrieves an organisations payment accounts by super user email for user",
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
                    message = "Data not found"
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
                    message = "The Organisation's associated payment accounts",
                    response = PbaResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Data not found"
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
            value = "Updates an organisation",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated an organisation"),
            @ApiResponse(code = 404, message = "If Organisation is not found"),
            @ApiResponse(code = 403, message = "Forbidden Error"),
            @ApiResponse(code = 400, message = "If Organisation request sent with null/invalid values for mandatory fields")
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
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not Found"
            )
    })
    @PostMapping(
            path = "/{orgId}/users/",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @Secured("prd-admin")
    public ResponseEntity addUserToOrganisation(
            @Valid @NotNull @RequestBody NewUserCreationRequest newUserCreationRequest,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @ApiParam(hidden = true) @UserId String userId) {

        //Received request to add a internal new user to an organisation

        return inviteUserToOrganisation(newUserCreationRequest, organisationIdentifier, userId);
    }
}
