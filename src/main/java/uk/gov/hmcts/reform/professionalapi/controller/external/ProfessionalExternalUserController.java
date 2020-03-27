package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.FORBIDDEN_ERROR_ACCESS_DENIED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_CASE_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_FINANCE_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_ORGANISATION_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_USER_MANAGER;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.validateEmail;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;


@RequestMapping(
        path = "refdata/external/v1/organisations",
        produces = APPLICATION_JSON_VALUE
)
@RestController
@Slf4j
public class ProfessionalExternalUserController extends SuperController {

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
                    response = ProfessionalUsersEntityResponse.class
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
            value = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({PUI_FINANCE_MANAGER, PUI_USER_MANAGER, PUI_ORGANISATION_MANAGER, PUI_CASE_MANAGER, "caseworker-divorce-financialremedy", "caseworker-divorce-financialremedy-solicitor", "caseworker-divorce-solicitor", "caseworker-divorce", "caseworker"})
    public ResponseEntity findUsersByOrganisation(@ApiParam(hidden = true) @OrgId String organisationIdentifier,
                                                  @ApiParam(name = "showDeleted") @RequestParam(value = "showDeleted", required = false) String showDeleted,
                                                  @ApiParam(name = "status") @RequestParam(value = "status", required = false) String status,
                                                  @RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size) {

        profExtUsrReqValidator.validateRequest(organisationIdentifier, showDeleted, status);
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isRolePuiUserManager = organisationIdentifierValidatorImpl.ifUserRoleExists(serviceAndUserDetails.getAuthorities(), PUI_USER_MANAGER);
        ResponseEntity profUsersEntityResponse;

        if (!isRolePuiUserManager) {
            if (StringUtils.isEmpty(status)) {
                status = "Active";
            }
            profExtUsrReqValidator.validateStatusIsActive(status);
        }

        profUsersEntityResponse = searchUsersByOrganisation(organisationIdentifier, showDeleted, true, status, page, size);

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
                    response = ProfessionalUsersEntityResponse.class
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
    @Secured({PUI_USER_MANAGER})
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
                    message = "No User found with the given ID"
            )
    })
    @PutMapping(
            path = "/users/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @Secured(PUI_USER_MANAGER)
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
            value = "The Email of the desired User who's Status is to be retrieved",
            required = false
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "User Identifier of the User otherwise only Status Code",
                    response = NewUserResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid Email Address was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User belonging to an Active Organisation was found with the given Email Address"
            )
    })
    @GetMapping(
            value = "/users/accountId",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({PUI_FINANCE_MANAGER, PUI_USER_MANAGER, PUI_ORGANISATION_MANAGER, PUI_CASE_MANAGER, "caseworker-publiclaw-courtadmin"})
    public ResponseEntity<NewUserResponse> findUserStatusByEmail(
            @ApiParam(name = "email", required = true) @RequestParam(value = "email") String email) {

        validateEmail(email);
        return professionalUserService.findUserStatusByEmailAddress(email.toLowerCase());
    }
}