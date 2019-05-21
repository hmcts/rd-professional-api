package uk.gov.hmcts.reform.professionalapi.controller;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@RequestMapping(
        path = "v1/organisations",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
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
                                                                                   @RequestParam(value = "showDeleted") String showDeleted) {

        log.info("Received request to get users for organisationIdentifier: " + organisationIdentifier);
        UUID inputOrganisationIdentifier = organisationCreationRequestValidator.validateAndReturnInputOrganisationIdentifier(organisationIdentifier);
        Organisation existingOrganisation = organisationService.getOrganisationByOrganisationIdentifier(inputOrganisationIdentifier);
        organisationIdentifierValidatorImpl.validate(existingOrganisation, null, inputOrganisationIdentifier);

        boolean showDeletedFlag = false;
        if("True".equalsIgnoreCase(showDeleted)){
            showDeletedFlag = true;
        }

        return ResponseEntity
                .status(200)
                .body(new ProfessionalUsersEntityResponse(professionalUserService.findProfessionalUsersByOrganisation(existingOrganisation, showDeletedFlag)));
    }
}
