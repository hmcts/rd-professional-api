package uk.gov.hmcts.reform.professionalapi.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

@RequestMapping(
    path = "v1/organisations",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RestController
@Slf4j
@AllArgsConstructor
public class OrganisationController {

    private OrganisationServiceImpl organisationService;
    private UpdateOrganisationRequestValidator updateOrganisationRequestValidator;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;

    @ApiOperation("Creates an organisation")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "A representation of the created organisation",
            response = OrganisationResponse.class
        )
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<OrganisationResponse> createOrganisation(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest) {

        log.info("Received request to create a new organisation...");

        organisationCreationRequestValidator.validate(organisationCreationRequest);

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

        log.info("Received response to create a new organisation..." + organisationResponse);
        return ResponseEntity
                .status(201)
                .body(organisationResponse);
    }

    @ApiOperation("Retrieves an organisation")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "A representation of the retrieve organisation",
                    response = OrganisationsDetailResponse.class
            )
    })
    @GetMapping
    public ResponseEntity<OrganisationsDetailResponse> retrieveOrganisations() {

        log.info("Received request to retrieve a new organisation...");

        OrganisationsDetailResponse organisationResponse =
                organisationService.retrieveOrganisations();

        log.debug("Received response to retrieve an organisation details..." + organisationResponse);
        return ResponseEntity
                .status(200)
                .body(organisationResponse);
    }

    @ApiOperation(value = "Updates an organisation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Updated an organisation"),
        @ApiResponse(code = 404, message = "If Organisation is not found"),
        @ApiResponse(code = 400, message = "If Organisation request sent with null/invalid values for mandatory fields")
    })
    @PutMapping(
            value = "/{orgId}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity updatesOrganisation(
            @Valid @NotNull @RequestBody OrganisationCreationRequest organisationCreationRequest,
            @PathVariable("orgId") @NotBlank String organisationIdentifier) {

        log.info("Received request to update organisation for organisationIdentifier: " + organisationIdentifier);

        organisationCreationRequestValidator.validate(organisationCreationRequest);
        updateOrganisationRequestValidator.validate(organisationCreationRequest, organisationIdentifier);

        OrganisationResponse organisationResponse =
                organisationService.updateOrganisation(organisationCreationRequest, UUID.fromString(organisationIdentifier));
        log.info("Received response to update organisation..." + organisationResponse);
        return ResponseEntity.status(200).build();
    }
}
