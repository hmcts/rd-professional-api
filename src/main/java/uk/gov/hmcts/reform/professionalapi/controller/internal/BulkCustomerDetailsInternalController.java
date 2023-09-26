package uk.gov.hmcts.reform.professionalapi.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.BulkCustomerOrganisationsDetailResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(path = "/refdata/internal/v1/bulkCustomer")
@Validated
@RestController
@Slf4j
@NoArgsConstructor
public class BulkCustomerDetailsInternalController extends SuperController {

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Autowired
    protected OrganisationCreationRequestValidator organisationCreationRequestValidator;

    @Operation(
            summary = "Retrieves organisation details for bulk customer",
            description = "**IDAM Roles to access API** : <br> prd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "Details of Organisation",
            content = @Content(schema = @Schema(implementation = BulkCustomerOrganisationsDetailResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request (Status or ID) provided",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No Organisation(s) found with the given ID",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )


    @Secured("prd-admin")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> retrieveOrganisationDetailsForBulkCustomer(
            @Parameter(name = "bulkCustomerId") @RequestParam(value = "bulkCustomerId") String bulkCustomerId,
            @Parameter(name = "idamId") @RequestParam(value = "idamId") String idamId) {

        organisationCreationRequestValidator.validateForEmptyOrNullInput(bulkCustomerId, idamId);
        organisationCreationRequestValidator.validateInputForSpecialCharacter(bulkCustomerId);
        organisationCreationRequestValidator.validateInputForSpecialCharacter(idamId);

        return retrieveOrganisationDetailsForBulkCustomerId(bulkCustomerId, idamId);

    }
}
