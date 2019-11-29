package uk.gov.hmcts.reform.professionalapi.controller.external;

import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator.validateEmail;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
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
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RestController
@Slf4j
public class ProfessionalExternalUserController extends SuperController {

    @ApiOperation(
            value = "Retrieves the given organisation based on user with the given email address if organisation is active or showDeleted flag ",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "showDeleted",
            type = "string",
            value = "flag (True/False) to decide deleted users needs to be shown",
            required = false
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "List of a professional users along with details",
                    response = ProfessionalUsersEntityResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid organisation identifier was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Invalid authorization"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No organisation was found with the provided organisation identifier or email address"
            )
    })
    @GetMapping(
            value = "/users",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager", "caseworker-divorce-financialremedy", "caseworker-divorce-financialremedy-solicitor", "caseworker-divorce-solicitor", "caseworker-divorce", "caseworker"})
    public ResponseEntity findUsersByOrganisation(@ApiParam(hidden = true) @OrgId String organisationIdentifier,
                                                     @ApiParam(name = "showDeleted", required = false) @RequestParam(value = "showDeleted", required = false) String showDeleted,
                                                     @ApiParam(name = "status", required = false) @RequestParam(value = "status", required = false) String status,
                                                     @RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "size", required = false) Integer size) {

        profExtUsrReqValidator.validateRequest(organisationIdentifier, showDeleted, status);
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isRolePuiUserManager = organisationIdentifierValidatorImpl.ifUserRoleExists(serviceAndUserDetails.getAuthorities(), "pui-user-manager");
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
            value = "Retrieves the user with the given email address if organisation is active or showDeleted flag ",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "email",
            type = "string",
            value = "The email of the desired user to be retrieved",
            required = false
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "A professional user along with their details",
                    response = ProfessionalUsersEntityResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid email was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Invalid authorization"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No user was found with the provided email address"
            )
    })
    @GetMapping(
            value = "/user",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Secured({"pui-user-manager"})
    public Optional<ResponseEntity> findUserByEmail(@ApiParam(hidden = true) @OrgId String organisationIdentifier,
                                                    @ApiParam(name = "email", required = false) @RequestParam(value = "email", required = false) String email) {

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
            value = "Modify roles for user",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "User Roles has been added",
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
    @PutMapping(
            path = "/users/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    @Secured("pui-user-manager")
    public ResponseEntity<ModifyUserRolesResponse> modifyRolesForExistingUserOfExternalOrganisation(
            @RequestBody UserProfileUpdatedData userProfileUpdatedData,
            @ApiParam(hidden = true) @OrgId String orgId,
            @PathVariable("userId") String userId,
            @RequestParam(name = "origin", required = false, defaultValue = "EXUI") Optional<String> origin
    ) {

        //Received request to update user roles of an organisation
        return modifyRolesForUserOfOrganisation(userProfileUpdatedData, orgId, userId, origin);

    }


    @ApiOperation(
            value = "Retrieves the user status with the given email address if organisation is active",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "email",
            type = "string",
            value = "The status of the desired user to be retrieved",
            required = false
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "User status active will return user identifier else only status code",
                    response = NewUserResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid email was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Invalid authorization"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No user status was found with the provided email address"
            )
    })
    @GetMapping(
            value = "/users/findUserByEmailId",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager"})
    public ResponseEntity<NewUserResponse> findUserStatusByEmail(
                                                    @ApiParam(name = "email", required = true) @RequestParam(value = "email") String email) {

        validateEmail(email);
        //email is valid
        return professionalUserService.findUserStatusByEmailAddress(email.toLowerCase());
    }

}