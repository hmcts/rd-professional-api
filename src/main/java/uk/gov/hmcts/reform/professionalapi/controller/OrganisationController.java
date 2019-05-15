package uk.gov.hmcts.reform.professionalapi.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.professionalapi.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationPbaResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
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
    private ProfessionalUserService professionalUserService;

    private OrganisationCreationRequestValidator validator;

    private PaymentAccountService paymentAccountservice;


    @ApiOperation("Creates an organisation")
    @ApiResponses({
            @ApiResponse(
                    code = 201,
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

        validator.validate(organisationCreationRequest);

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

    @ApiOperation("Retrieves the user with the given email address")
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
            response = ProfessionalUserResponse.class
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
    @GetMapping("/users")
    public ResponseEntity<ProfessionalUserResponse> findUserByEmail(@RequestParam(value = "email") String email) {
        return ResponseEntity
                .status(200)
                .body(new ProfessionalUserResponse(professionalUserService.findProfessionalUserByEmailAddress(email)));
    }

    @ApiOperation("Retrieves an organisations payment accounts by super user email")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The organisations associated payment accounts",
                    response = OrganisationPbaResponse.class
            )
    })
    @GetMapping(path = "/pbas")
    public ResponseEntity<OrganisationPbaResponse> retrievePaymentAccountBySuperUserEmail(@NotNull @RequestParam("email") String email) {
        log.info("Received request to retrieve an organisations payment accounts by email...");

        Organisation organisation = paymentAccountservice.findPaymentAccountsByEmail(email);

        return ResponseEntity
                .status(200)
                .body(new OrganisationPbaResponse(organisation, false));
    }
}
