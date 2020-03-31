package uk.gov.hmcts.reform.professionalapi.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.FORBIDDEN_ERROR_ACCESS_DENIED;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.professionalapi.controller.response.LegacyPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.impl.LegacyPbaAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@RequestMapping(
        path = "/search"
)
@RestController
@Slf4j
@AllArgsConstructor
public class LegacyPbaController {

    private LegacyPbaAccountServiceImpl legacyPbaAccountService;
    private ProfessionalUserServiceImpl professionalUserService;

    @ApiOperation(
            value = "Retrieve Payment Accounts by a User's Email Address"
    )
    @ApiParam(
            name = "email",
            type = "string",
            value = "The Email of the User who's Payment Accounts are to be retrieved",
            required = false
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Details of one or more Payment Accounts",
                    response = LegacyPbaResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid Email Address was provided"
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR_ACCESS_DENIED
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Users or Payment Accounts were found with the Email Address provided"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            value = "/pba/{email}",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<LegacyPbaResponse> retrievePbaAccountsByEmail(@PathVariable("email") @NotBlank String email) {
        List<String> pbaNumbers;
        ProfessionalUser professionalUser = professionalUserService.findProfessionalUserByEmailAddress(RefDataUtil.removeEmptySpaces(email));

        if (professionalUser == null) {
            throw new EmptyResultDataAccessException(1);
        }

        pbaNumbers = legacyPbaAccountService.findLegacyPbaAccountByUserEmail(professionalUser);

        if (null == pbaNumbers) {
            pbaNumbers = new ArrayList<>();
        }

        return ResponseEntity
                .status(200)
                .body(new LegacyPbaResponse(pbaNumbers));
    }
}
