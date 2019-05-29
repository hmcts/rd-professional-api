package uk.gov.hmcts.reform.professionalapi.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.util.ArrayList;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;




@RequestMapping(
        path = "v1/organisations",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RestController
@Slf4j
@AllArgsConstructor
public class ProfessionalUserController {

    private ProfessionalUserService professionalUserService;
    private OrganisationServiceImpl organisationService;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;

    @ApiOperation(
            value = "Retrieves the users with the given organisation",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization")
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
    public ResponseEntity<ProfessionalUsersEntityResponse> findUsersByOrganisation(@PathVariable("orgId") @NotBlank String organisationIdentifier,
                                                                                   @RequestParam(value = "showDeleted", required = false) String showDeleted) {

        log.info("Received request to get users for organisationIdentifier: " + organisationIdentifier);
        organisationCreationRequestValidator.validateOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrganisationIdentifier(organisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, organisationIdentifier);

        if (OrganisationStatus.ACTIVE != existingOrganisation.getStatus()) {
            log.info("Organisation is not Active hence not returning any users");
            return ResponseEntity
                    .status(200)
                    .body(new ProfessionalUsersEntityResponse(new ArrayList<ProfessionalUser>()));
        }

        if (null == showDeleted) {
            log.info("Request param 'showDeleted' not provided or its value is null");
        }

        boolean showDeletedFlag = false;
        if ("True".equalsIgnoreCase(showDeleted)) {
            showDeletedFlag = true;
        }

        return ResponseEntity
                .status(200)
                .body(new ProfessionalUsersEntityResponse(professionalUserService.findProfessionalUsersByOrganisation(existingOrganisation, showDeletedFlag)));
    }
}
