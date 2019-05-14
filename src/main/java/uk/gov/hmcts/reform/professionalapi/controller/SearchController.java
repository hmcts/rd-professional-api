package uk.gov.hmcts.reform.professionalapi.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.professionalapi.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUserResponse;

@RequestMapping(
        path = "/search",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class SearchController {

    private final ProfessionalUserService professionalUserService;

    public SearchController(ProfessionalUserService professionalUserService) {
        this.professionalUserService = professionalUserService;
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ProfessionalUserResponse> findUserByEmail(@PathVariable(value = "email") String email) {
        return ResponseEntity
                .status(200)
                .body(new ProfessionalUserResponse(professionalUserService.findProfessionalUserByEmailAddress(email)));
    }
}
