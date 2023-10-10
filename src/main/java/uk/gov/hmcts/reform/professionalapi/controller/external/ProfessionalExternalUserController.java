package uk.gov.hmcts.reform.professionalapi.controller.external;


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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.Optional;
import javax.validation.constraints.Size;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_USERS_BY_ORG_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_USERS_BY_ORG_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_USERS_BY_ORG_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_USER_STATUS_EMAIL_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_USER_STATUS_EMAIL_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.GET_USER_STATUS_EMAIL_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PUI_USER_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;

@RequestMapping(
        path = "refdata/external/v1/organisations",
        produces = APPLICATION_JSON_VALUE
)
@RestController
@Slf4j
public class ProfessionalExternalUserController extends SuperController {

    @Operation(
            summary = "Retrieves the Users of an Active Organisation based on the showDeleted flag and without roles if"
                    + " returnRoles is False",
            description = GET_USERS_BY_ORG_1 + GET_USERS_BY_ORG_2 + GET_USERS_BY_ORG_3,
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
            description = "An invalid Organisation Identifier was provided"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Error : "
                    + "The requested resource is restricted and requires authentication"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied"
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation or Users found with the given ID"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error"
    )

    @GetMapping(
            value = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager",
            "caseworker-divorce-financialremedy", "caseworker-divorce-financialremedy-solicitor",
            "caseworker-divorce-solicitor", "caseworker-divorce", "caseworker", "pui-caa"})
    public ResponseEntity<Object> findUsersByOrganisation(
            @Parameter(hidden = true) @OrgId String organisationIdentifier,
            @Parameter(name = "showDeleted") @RequestParam(value = "showDeleted",
                    required = false) String showDeleted,
            @Parameter(name = "status") @RequestParam(value = "status",
                    required = false) String status,
            @Parameter(name = "returnRoles") @RequestParam(value = "returnRoles",
                    required = false, defaultValue = "true") Boolean returnRoles,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "userIdentifier", required = false) String userIdentifier,
            @Parameter(hidden = true) @UserId String userId,
            @RequestParam(value = "searchString", required = false)
                @Size(min = 3,message = "SearchString must have at least 3 character")
                String searchString) {


        profExtUsrReqValidator.validateRequest(organisationIdentifier, showDeleted, status);

        if (!organisationIdentifierValidatorImpl.ifUserRoleExists(idamRepository.getUserInfo(getUserToken())
                .getRoles(), PUI_USER_MANAGER)) {
            status = isBlank(status) ? ACTIVE : status;
            profExtUsrReqValidator.validateStatusIsActive(status);
        }
        if (userIdentifier != null) {
            profExtUsrReqValidator.validateUuid(userIdentifier);
            ProfessionalUser fetchingUser = professionalUserService.findProfessionalUserByUserIdentifier(
                    userIdentifier);
            profExtUsrReqValidator.validateOrganisationMatch(organisationIdentifier, fetchingUser);
        }

        return searchUsersByOrganisation(organisationIdentifier, userIdentifier, showDeleted, returnRoles, status,
                page, size,searchString);
    }

    @Operation(
            summary = "Modify the Roles or Status of a User with the given ID",
            description = "**IDAM Roles to access API** : <br> pui-user-manager",
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
            description = "Invalid request provided"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied"
    )
    @ApiResponse(
            responseCode = "404",
            description = "No User found with the given ID"
    )
    @ApiResponse(
            responseCode = "412",
            description = "One or more of the Roles provided is already assigned to the User"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error"
    )

    @PutMapping(
            path = "/users/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("pui-user-manager")
    public ResponseEntity<Object> modifyRolesForExistingUserOfExternalOrganisation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "userProfileUpdatedData")
            @RequestBody UserProfileUpdatedData userProfileUpdatedData,
            @Parameter(hidden = true) @OrgId String orgId,
            @PathVariable("userId") String userId,
            @RequestParam(name = "origin", required = false, defaultValue = "EXUI") String origin
    ) {

        professionalUserService.checkUserStatusIsActiveByUserId(userId);
        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, userId, Optional.of(origin));

    }

    @Operation(
            summary = "Retrieves the Status of a User belonging to an Active Organisation with the given Email Address",
            description = GET_USER_STATUS_EMAIL_1 + GET_USER_STATUS_EMAIL_2 + GET_USER_STATUS_EMAIL_3,
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization"),
                    @SecurityRequirement(name = "UserEmail")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "The User Identifier of the User",
            content = @Content(schema = @Schema(implementation = NewUserResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid Email Address was provided"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied"
    )
    @ApiResponse(
            responseCode = "404",
            description = "No User belonging to an Active Organisation was found with the given Email Address"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error"
    )

    @GetMapping(
            value = "/users/accountId",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager",
            "caseworker-publiclaw-courtadmin"})
    public ResponseEntity<NewUserResponse> findUserStatusByEmail(
            @RequestParam(value = "email", required = false) String email) {

        var userEmail = getUserEmail(email);
        validateEmail(userEmail);
        return professionalUserService.findUserStatusByEmailAddress(userEmail.toLowerCase());
    }
}