package uk.gov.hmcts.reform.professionalapi.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.SINCE_TIMESTAMP_FORMAT;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.isSystemRoleUser;


@RequestMapping(
        path = "refdata/internal/v1/organisations",
        produces = APPLICATION_JSON_VALUE
)
@Validated
@RestController
@Slf4j
public class ProfessionalUserInternalController extends SuperController {

    @Operation(
            summary = "Retrieves the Users of an Active Organisation based on the showDeleted flag",
            description = "**IDAM Roles to access API** :<br> prd-admin,<br> prd-aac-system",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @Parameter(
            name = "showDeleted",
            description = "Flag (True/False) to decide whether Deleted Users are included in the response"
    )

    @ApiResponse(
            responseCode = "200",
            description = "List of Professional Users and their details",
            content = @Content(array = @ArraySchema(schema =
            @Schema(implementation = ProfessionalUsersEntityResponse.class)))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid Organisation Identifier was provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation or Users found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @GetMapping(
            value = "/{orgId}/users",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"prd-admin", "prd-aac-system"})
    public ResponseEntity<Object> findUsersByOrganisation(
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE)
            @PathVariable("orgId") @NotBlank String organisationIdentifier,
            @RequestParam(value = "userIdentifier", required = false) String userIdentifier,
            @RequestParam(value = "showDeleted", required = false) String showDeleted,
            @Parameter(name = "returnRoles")
            @RequestParam(value = "returnRoles", required = false, defaultValue = "true") Boolean returnRoles,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        var status = EMPTY;
        if (isSystemRoleUser(idamRepository.getUserInfo(getUserToken()).getRoles())) {
            status = ACTIVE;
        }
        return searchUsersByOrganisation(organisationIdentifier, userIdentifier, showDeleted, returnRoles, status, page,
                size);
    }

    @Operation(
            summary = "Modify the Roles or Status of a User",
            description = "**IDAM Roles to access API** :<br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "201",
            description = "The User's Roles/Status have been modified",
            content = @Content(schema = @Schema(implementation = ModifyUserRolesResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation or User found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "412",
            description = "One or more of the Roles provided is already assigned to the User",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PutMapping(
            path = "/{orgId}/users/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @Secured("prd-admin")
    public ResponseEntity<Object> modifyRolesForExistingUserOfOrganisation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "userProfileUpdatedData")
            @RequestBody UserProfileUpdatedData userProfileUpdatedData,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message
                    = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") String orgId,
            @PathVariable("userId") String userId,
            @RequestParam(name = "origin", required = false, defaultValue = "EXUI") String origin
    ) {

        organisationIdentifierValidatorImpl.validateOrganisationExistsWithGivenOrgId(orgId);

        //Received request to update user roles of an organisation
        return professionalUserService.modifyRolesForUser(userProfileUpdatedData, userId, Optional.of(origin));

    }

    @Operation(
            summary = "Retrieves list of users required for a refresh",
            description = "**Bearer token not required to access API. Only a valid s2s token**",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization")
            }
    )
    @Parameter(
            name = "since",
            description = "Timestamp to fetch users before. Expected format: " + SINCE_TIMESTAMP_FORMAT
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of Users and their details",
            content = @Content(array = @ArraySchema(schema =
            @Schema(implementation = GetRefreshUsersResponse.class)))
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

    @GetMapping(
            path = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> getRefreshUsers(
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "size") Integer size
    ) {
        organisationIdentifierValidatorImpl.validateGetRefreshUsersParams(since, userId);

        return fetchUsersForRefresh(since, userId, page, size);
    }
}
