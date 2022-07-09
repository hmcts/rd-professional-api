package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(
        path = "/refdata/external/v1/organisations/mfa"
)
@RestController
@RequestScope
public class OrganisationMfaStatusController extends SuperController {

    @ApiOperation(
            value = "Retrieves the mfa status of the organisation a user belongs to",
            notes = "**IDAM Roles to access API** : \n No role restriction"
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The mfa status of the organisation the user belongs to",
                    response = MfaStatusResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 404,
                    message = "The requested user does not exist"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MfaStatusResponse> retrieveMfaStatusByUserId(
            @RequestParam(value = "user_id", required = true) String userId) {

        return mfaStatusService.findMfaStatusByUserId(userId);
    }

}
