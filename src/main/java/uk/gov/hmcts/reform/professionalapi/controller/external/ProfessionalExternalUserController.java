package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;

@RequestMapping(
        path = "refdata/external/v1/organisations",
        produces = APPLICATION_JSON_VALUE
)
@RestController
@Slf4j
public class ProfessionalExternalUserController extends SuperController {

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @ApiOperation(
            value = "Retrieves the Users of an Active Organisation based on the showDeleted flag",
            response = ProfessionalUsersResponse.class,
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
            value = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager", "pui-caa", "caseworker-divorce-financialremedy", "caseworker-divorce-financialremedy-solicitor", "caseworker-divorce-solicitor", "caseworker-divorce", "caseworker"})
    public ResponseEntity findUsersByOrganisation(@ApiParam(hidden = true) @OrgId String organisationIdentifier,
                                                  @ApiParam(name = "showDeleted") @RequestParam(value = "showDeleted", required = false) String showDeleted,
                                                  @ApiParam(name = "status") @RequestParam(value = "status", required = false) String status,
                                                  @ApiParam(name = "returnRoles") @RequestParam(value = "returnRoles", required = false, defaultValue = "True") String returnRoles,
                                                  @RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size,
                                                  @ApiParam(hidden = true) @UserId String userId) {
        log.info(" Request param : "+ returnRoles);
        profExtUsrReqValidator.validateRequest(organisationIdentifier, showDeleted, status);
        UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
        ResponseEntity profUsersEntityResponse;
        if (!organisationIdentifierValidatorImpl.ifUserRoleExists(userInfo.getRoles(), ProfessionalApiConstants.PUI_USER_MANAGER)) {
            status = StringUtils.isEmpty(status) ? ProfessionalApiConstants.ACTIVE : status;
            profExtUsrReqValidator.validateStatusIsActive(status);
        }
        // verify the invited user status active or not?
        professionalUserService.checkUserStatusIsActiveByUserId(userId);

        profUsersEntityResponse = searchUsersByOrganisation(organisationIdentifier, showDeleted, returnRoles, status, page, size);
        return profUsersEntityResponse;
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
            value = "The Email Address of the User to be retrieved"
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
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User found with the given Email Address"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            value = "/user",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-user-manager"})
    public Optional<ResponseEntity> findUserByEmail(@ApiParam(hidden = true) @OrgId String organisationIdentifier,
                                                    @ApiParam(name = "email") @RequestParam(value = "email", required = false) String email) {

        Optional<ResponseEntity> optionalResponseEntity;
        validateEmail(email);
        //email is valid
        optionalResponseEntity = Optional.ofNullable(retrieveUserByEmail(email.toLowerCase()));

        if (optionalResponseEntity.isPresent()) {
            return optionalResponseEntity;
        } else {
            throw new ResourceNotFoundException("No user was found with the email provided, please ensure you are using a valid email address");
        }
    }

    @ApiOperation(
            value = "Modify the Roles or Status of a User with the given ID",
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
                    message = "No User found with the given ID"
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
            path = "/users/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Secured("pui-user-manager")
    public ResponseEntity<ModifyUserRolesResponse> modifyRolesForExistingUserOfExternalOrganisation(
            @RequestBody UserProfileUpdatedData userProfileUpdatedData,
            @ApiParam(hidden = true) @OrgId String orgId,
            @PathVariable("userId") String userId,
            @RequestParam(name = "origin", required = false, defaultValue = "EXUI") Optional<String> origin
    ) {

        //Received request to update user roles of an organisation
        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, userId, origin);

    }


    @ApiOperation(
            value = "Retrieves the Status of a User belonging to an Active Organisation with the given Email Address",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "email",
            type = "string",
            value = "The Email of the desired User who's Status is to be retrieved"
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The User Identifier of the User",
                    response = NewUserResponse.class
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
                    message = "No User belonging to an Active Organisation was found with the given Email Address"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            value = "/users/accountId",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager", "caseworker-publiclaw-courtadmin"})
    public ResponseEntity<NewUserResponse> findUserStatusByEmail(
            @ApiParam(name = "email", required = true) @RequestParam(value = "email") String email) {

        validateEmail(email);
        return professionalUserService.findUserStatusByEmailAddress(email.toLowerCase());
    }
}