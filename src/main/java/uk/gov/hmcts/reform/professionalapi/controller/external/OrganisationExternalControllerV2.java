package uk.gov.hmcts.reform.professionalapi.controller.external;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(
        path = "refdata/external/v2/organisations"
)
@RestController
@Slf4j
public class OrganisationExternalControllerV2 extends SuperController {
    @Operation(
            summary = "Creates an Organisation",
            description = "**IDAM Roles to access API**: <br> No role restriction",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization")
            }
    )

    @ApiResponse(
            responseCode = "201",
            description = "The Organisation Identifier of the created Organisation",
            content = @Content(schema = @Schema(implementation = OrganisationResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "An invalid request has been provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<OrganisationResponse> createOrganisationUsingExternalController(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "organisationCreationRequest")
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        //Received request to create a new organisation for external user
        return createOrganisationFrom(organisationCreationRequest);
    }
}
