package uk.gov.hmcts.reform.professionalapi.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
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
            value = "Retrieve pba numbers by user email address"
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Details of one or more payment accounts",
                    response = LegacyPbaResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid email was provided"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No payment users was found with the email"
            )
    })
    @GetMapping(
            value = "/pba/{email}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<LegacyPbaResponse> retrievePbaAccountsByEmail(@PathVariable("email") @NotBlank String email) {

        List<String> pbaNumbers;
        ProfessionalUser professionalUser =  professionalUserService.findProfessionalUserByEmailAddress(email);
        if (professionalUser == null) {

            throw new EmptyResultDataAccessException(1);
        }

        pbaNumbers =  legacyPbaAccountService.findLegacyPbaAccountByUserEmail(professionalUser);
        if (null == pbaNumbers) {

            pbaNumbers = new ArrayList<>();
        }
        return ResponseEntity
                .status(200)
                .body(new LegacyPbaResponse(pbaNumbers));
    }
}
