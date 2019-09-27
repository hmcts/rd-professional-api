package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;

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
    public ResponseEntity<?> findUsersByOrganisation(@ApiParam(hidden = true) @OrgId String organisationIdentifier,
                                                     @ApiParam(name = "showDeleted", required = false) @RequestParam(value = "showDeleted", required = false) String showDeleted,
                                                     @ApiParam(name = "email", required = false) @RequestParam(value = "email", required = false) String email,
                                                     @ApiParam(name = "status", required = false) @RequestParam(value = "status", required = false) String status) {

        ResponseEntity<?> profUsersEntityResponse;
        log.info("ProfessionalExternalUserController::findUsersByOrganisation:" + organisationIdentifier);
        profExtUsrReqValidator.validateRequest(organisationIdentifier, showDeleted, email, status);
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isRolePuiUserManager = organisationIdentifierValidatorImpl.ifUserRoleExists(serviceAndUserDetails.getAuthorities(), "pui-user-manager");

        if (!StringUtils.isEmpty(email)) {
            log.info("email not empty");
            if (isRolePuiUserManager) {
                profUsersEntityResponse = retrieveUserByEmail(email);
            } else {
                throw new AccessDeniedException("403 Forbidden");
            }
        } else {

            if (!isRolePuiUserManager) {
                if (StringUtils.isEmpty(status)) {
                    status = "Active";
                }
                profExtUsrReqValidator.validateStatusIsActive(status);
            }

            profUsersEntityResponse = searchUsersByOrganisation(organisationIdentifier, showDeleted, true, status);
        }

        return profUsersEntityResponse;
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
            @RequestBody ModifyUserProfileData modifyUserProfileData,
            @ApiParam(hidden = true) @OrgId String orgId,
            @PathVariable("userId") String userId
    ) {

        log.info("Received request to update user roles of an organisation...");
        profExtUsrReqValidator.validateModifyRolesRequest(modifyUserProfileData, userId);
        return modifyRolesForUserOfOrganisation(modifyUserProfileData, orgId, userId);

    }
}