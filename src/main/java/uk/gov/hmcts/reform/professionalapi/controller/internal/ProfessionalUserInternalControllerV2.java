package uk.gov.hmcts.reform.professionalapi.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.SuperController;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.UsersInOrganisationsByOrganisationIdentifiersRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.UsersInOrganisationsByOrganisationIdentifiersResponse;

import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(
        path = "refdata/internal/v2/organisations",
        produces = APPLICATION_JSON_VALUE
)
@Validated
@RestController
@Slf4j
public class ProfessionalUserInternalControllerV2 extends SuperController {
    @Autowired
    protected UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl usersInOrgByIdentifierValidatorImpl;

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Operation(
            summary = "Retrieves users in organisations by provided organisation identifiers",
            description = "**Bearer token not required to access API. Only a valid s2s token**",
            security = {
                @SecurityRequirement(name = "ServiceAuthorization")
            }
    )

    @ApiResponse(
            responseCode = "200",
            description = "List of users and organisations",
            content = @Content(schema =
            @Schema(implementation = UsersInOrganisationsByOrganisationIdentifiersResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request (PageSize, searchAfterOrg or searchAfterUser) provided",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden Error: Access denied",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content
    )

    @PostMapping(
            path = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> findUsersByOrganisations(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Get users in organisations by "
                    + "organisation identifiers")
            @Valid @NotNull
            @RequestBody UsersInOrganisationsByOrganisationIdentifiersRequest organisationByProfileIdsRequest,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "searchAfterOrg", required = false) UUID searchAfterOrg,
            @RequestParam(value = "searchAfterUser", required = false) UUID searchAfterUser
    ) {
        usersInOrgByIdentifierValidatorImpl.validate(pageSize, searchAfterOrg, searchAfterUser);

        UsersInOrganisationsByOrganisationIdentifiersResponse response =
                professionalUserService.retrieveUsersByOrganisationIdentifiersWithPageable(
                        organisationByProfileIdsRequest.getOrganisationIdentifiers(), pageSize, searchAfterUser,
                        searchAfterOrg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
