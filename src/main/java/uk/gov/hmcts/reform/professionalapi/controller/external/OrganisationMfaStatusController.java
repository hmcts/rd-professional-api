package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(
        path = "/refdata/external/v1/organisations/mfa"
)
@RestController
public class OrganisationMfaStatusController extends SuperController {

    @Operation(
            summary = "Retrieves the mfa status of the organisation a user belongs to",
            description = "**IDAM Roles to access API** : <br> No role restriction"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The mfa status of the organisation the user belongs to",
                    content = @Content(schema = @Schema(implementation = MfaStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The requested user does not exist"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MfaStatusResponse> retrieveMfaStatusByUserId(
            @RequestParam(value = "user_id", required = true) String userId) {

        return mfaStatusService.findMfaStatusByUserId(userId);
    }

}
