package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;


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
    @Secured("pui-user-manager")
    public ResponseEntity<?> findUsersByOrganisation(@ApiParam(hidden = true) @OrgId  String organisationIdentifier,
                                       @ApiParam(name = "showDeleted", required = false)@RequestParam(value = "showDeleted", required = false) String showDeleted,
                                       @ApiParam(name = "email", required = false) @RequestParam (value = "email", required = false) String email) {

        ResponseEntity<?> profUsersEntityResponse = null;
        log.info("ProfessionalExternalUserController::findUsersByOrganisation:" + organisationIdentifier);
        profExtUsrReqValidator.validateRequest(organisationIdentifier,showDeleted,email);

        if (!StringUtils.isEmpty(email)) {
            log.info("email not empty");
            profUsersEntityResponse = retrieveUserByEmail(email);

        } else {
            log.info("showDeleted not empty");
            profUsersEntityResponse = searchUsersByOrganisation(organisationIdentifier, showDeleted);
        }

        return  profUsersEntityResponse;
    }
}