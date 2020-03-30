package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.FORBIDDEN_ERROR_ACCESS_DENIED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORG_ID_VALIDATION_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PRD_ADMIN;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;


@RequestMapping(
        path = "refdata/internal/v1/organisations",
        produces = APPLICATION_JSON_VALUE
)
@Validated
@RestController
@Slf4j
public class ProfessionalUserInternalController extends SuperController {

    @ApiOperation(
            value = "Retrieves the Users of an Active Organisation based on the showDeleted flag",
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
                    message = "List of Professional Users and their details",
                    response = ProfessionalUsersResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid Organisation Identifier was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation or Users found with the given ID"
            )
    })
    @GetMapping(
            value = "/{orgId}/users",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured(PRD_ADMIN)
    public ResponseEntity findUsersByOrganisation(@Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId") @NotBlank String organisationIdentifier,
                                                      @RequestParam(value = "showDeleted", required = false) String showDeleted,
                                                      @RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "size", required = false) Integer size) {

        return searchUsersByOrganisation(organisationIdentifier, showDeleted, true, "", page, size);
    }

    @ApiOperation(
            value = "Retrieves an Active User with the given Email Address",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "email",
            type = "string",
            value = "The Email Address of the User to be retrieved",
            required = false
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "A User and their details",
                    response = ProfessionalUsersResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid Email Address has been provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User found with the given Email Address"
            )
    })
    @GetMapping(
            value = "/user",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured(PRD_ADMIN)
    public ResponseEntity findUserByEmail(@RequestParam(value = "email") String email) {

        return retrieveUserByEmail(email);
    }

    @ApiOperation(
            value = "Modify the Roles of a User",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "The User's Roles have been modified",
                    response = OrganisationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Invalid request provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Organisation or User found with the given ID"
            )
    })
    @PutMapping(
            path = "/{orgId}/users/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured(PRD_ADMIN)
    public ResponseEntity<ModifyUserRolesResponse> modifyRolesForExistingUserOfOrganisation(
            @RequestBody UserProfileUpdatedData userProfileUpdatedData,
            @Pattern(regexp = ORGANISATION_IDENTIFIER_FORMAT_REGEX, message = ORG_ID_VALIDATION_ERROR_MESSAGE) @PathVariable("orgId")  String orgId,
            @PathVariable("userId") String userId,
            @RequestParam(name = "origin", required = false, defaultValue = "EXUI") Optional<String> origin
    ) {

        organisationIdentifierValidatorImpl.validateOrganisationExistsWithGivenOrgId(orgId);

        //Received request to update user roles of an organisation
        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, userId, origin);

    }
}
