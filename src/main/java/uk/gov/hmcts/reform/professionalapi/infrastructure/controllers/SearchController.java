package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.professionalapi.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response.ProfessionalUserResponse;

@RequestMapping(
        path = "/search",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class SearchController {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisationController.class);

    private final ProfessionalUserService professionalUserService;

    public SearchController(ProfessionalUserService professionalUserService) {
        this.professionalUserService = professionalUserService;
    }

    @RequestMapping("/user/{email}")
    public ResponseEntity<ProfessionalUserResponse> findUserByEmail(@PathVariable(value = "email") String email) {
        return ResponseEntity
                .status(200)
                .body(new ProfessionalUserResponse(professionalUserService.findProfessionalUserByEmailAddress(email)));
    }
}
