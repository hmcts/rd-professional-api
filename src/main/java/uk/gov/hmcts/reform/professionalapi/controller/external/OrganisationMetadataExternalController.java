package uk.gov.hmcts.reform.professionalapi.controller.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrgId;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/refdata/external/v1/organisations/metadata", produces = APPLICATION_JSON_VALUE)
public class OrganisationMetadataExternalController {

    private final OrganisationService organisationService;
    private final OrganisationIdentifierValidatorImpl organisationIdentifierValidator;

    public OrganisationMetadataExternalController(OrganisationService organisationService,
                                                  OrganisationIdentifierValidatorImpl organisationIdentifierValidator) {
        this.organisationService = organisationService;
        this.organisationIdentifierValidator = organisationIdentifierValidator;
    }

    @GetMapping
    @Operation(summary = "Retrieve the requesting user's active organisation without user profiles",
        security = {@SecurityRequirement(name = "ServiceAuthorization"),
            @SecurityRequirement(name = "Authorization")})
    @Secured({"pui-finance-manager", "pui-user-manager", "pui-organisation-manager", "pui-case-manager",
        "caseworker-divorce-financialremedy", "caseworker-divorce-financialremedy-solicitor",
        "caseworker-divorce-solicitor", "caseworker-divorce", "caseworker", "pui-caa"})
    public OrganisationMinimalInfoResponse getOrganisationMetadata(
        @Parameter(hidden = true) @OrgId String organisationIdentifier) {
        Organisation organisation = organisationService.getOrganisationByOrgIdentifier(organisationIdentifier);
        organisationIdentifierValidator.validate(organisation, null, organisationIdentifier);
        organisationIdentifierValidator.validateOrganisationIsActive(organisation, HttpStatus.NOT_FOUND);
        return new OrganisationMinimalInfoResponse(organisation, false);
    }
}
