package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.isSystemRoleUser;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;


@RequestMapping(
        path = "refdata/internal/v1/organisations",
        produces = APPLICATION_JSON_VALUE
)
@Validated
@RestController
@Slf4j
@RequestScope
public class ProfessionalUserInternalController extends SuperController {

    @ApiOperation(
            value = "Retrieves the Users of an Active Organisation based on the showDeleted flag",
            notes = "**IDAM Roles to access API** :\n prd-admin,\n prd-aac-system",
            response = ProfessionalUsersEntityResponse.class,
            responseContainer = "list",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "showDeleted",
            type = "string",
            value = "Flag (True/False) to decide whether Deleted Users are included in the response"
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "List of Professional Users and their details"
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid Organisation Identifier was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation or Users found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            value = "/{orgId}/users",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"prd-admin", "prd-aac-system"})
    public ResponseEntity<Object> findUsersByOrganisation(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @RequestParam(value = "showDeleted", required = false) String showDeleted,
            @ApiParam(name = "returnRoles")
            @RequestParam(value = "returnRoles", required = false, defaultValue = "true") Boolean returnRoles,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        var status = EMPTY;
        if (isSystemRoleUser(jwtGrantedAuthoritiesConverter.getUserInfo().getRoles())) {
            status = ACTIVE;
        }
        return searchUsersByOrganisation(organisationIdentifier, showDeleted, returnRoles, status, page, size);
    }

    @ApiOperation(
            value = "Modify the Roles or Status of a User",
            notes = "**IDAM Roles to access API** :\n prd-admin",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "The User's Roles/Status have been modified",
                    response = ModifyUserRolesResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Invalid request provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation or User found with the given ID"
            ),
            @ApiResponse(
                    code = 412,
                    message = "One or more of the Roles provided is already assigned to the User"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @PutMapping(
            path = "/{orgId}/users/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @Secured("prd-admin")
    public ResponseEntity<Object> modifyRolesForExistingUserOfOrganisation(
            @RequestBody UserProfileUpdatedData userProfileUpdatedData,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message
                    = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") String orgId,
            @PathVariable("userId") String userId,
            @RequestParam(name = "origin", required = false, defaultValue = "EXUI") Optional<String> origin
    ) {

        organisationIdentifierValidatorImpl.validateOrganisationExistsWithGivenOrgId(orgId);

        //Received request to update user roles of an organisation
        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, userId, origin);

    }
}
