package uk.gov.hmcts.reform.professionalapi.controller.internal;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import javax.validation.constraints.NotBlank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;

@RequestMapping(
        path = "refdata/internal/v1/organisations",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RestController
@Slf4j
public class ProfessionalUserInternalController extends SuperController {


    @ApiOperation(
            value = "Retrieves the internal users with the given organisation",
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
                    code = 404,
                    message = "No organisation was found with the provided organisation identifier"
            )
    })
    @GetMapping(
            value = "/{orgId}/users",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<ProfessionalUsersEntityResponse> findUsersByOrganisation(@PathVariable("orgId") @NotBlank String organisationIdentifier,
                                                                                   @RequestParam(value = "showDeleted", required = false) String showDeleted) {

        log.info("ProfessionalUserInternalController:Received request to get users for internal organisationIdentifier: " + organisationIdentifier);

        return searchUsersByOrganisation(organisationIdentifier, showDeleted);
    }

    @ApiOperation(
            value = "Retrieves the internal user with the given email address if organisation is active",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiParam(
            name = "email",
            type = "string",
            value = "The email address of the user to return",
            required = true
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "A representation of a professional user",
                    response = ProfessionalUsersResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid email address was provided"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No user was found with the provided email address"
            )
    })
    @GetMapping(
            value = "/users",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Secured("prd-admin")
    public ResponseEntity<?> findUserByEmail(@RequestParam(value = "email") String email) {

        return retrieveUserByEmail(email);
    }
}
