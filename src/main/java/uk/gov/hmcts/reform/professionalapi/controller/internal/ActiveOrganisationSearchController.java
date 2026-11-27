package uk.gov.hmcts.reform.professionalapi.controller.internal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.service.impl.ActiveOrganisationSearch;
import uk.gov.hmcts.reform.professionalapi.service.impl.SearchDeadline;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ActiveOrganisationSearchController {

    private static final String DEADLINE_HEADER = "X-Organisation-Search-Deadline";
    private static final long MAX_BUDGET_MILLIS = 25_000;
    private static final int MAX_BODY_BYTES = 6 * 1024 * 1024;

    private final ActiveOrganisationSearch search;
    private final ObjectMapper mapper;
    private final int defaultPageSize;

    public ActiveOrganisationSearchController(ActiveOrganisationSearch search, ObjectMapper mapper,
                                             @Value("${defaultPageSize}") int defaultPageSize) {
        this.search = search;
        this.mapper = mapper;
        this.defaultPageSize = defaultPageSize;
    }

    @Operation(summary = "Searches active organisations",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = Request.class))),
            security = {
                @SecurityRequirement(name = "ServiceAuthorization"),
                @SecurityRequirement(name = "Authorization")
            })
    @Secured("prd-admin")
    @PostMapping(path = "/refdata/internal/v1/organisations/search",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> search(HttpServletRequest servletRequest, @RequestHeader(AUTHORIZATION) String bearer,
                                         @RequestHeader(value = DEADLINE_HEADER, required = false) String supplied) {
        final var deadline = deadline(supplied);
        var request = readRequest(servletRequest);
        deadline.remainingNanos();
        if (!List.of("ACTIVE").equals(request.statuses())) {
            throw new InvalidRequest("statuses must contain only ACTIVE");
        }
        if (request.searchTerm() != null && !request.searchTerm().isNull() && !request.searchTerm().isTextual()) {
            throw new InvalidRequest("searchTerm must be a string");
        }
        String term = request.searchTerm() == null ? "" : request.searchTerm().asText("");
        if (term.indexOf(0) >= 0) {
            throw new InvalidRequest("searchTerm must not contain a null character");
        }
        int page = positiveInteger("page", request.page(), 1);
        int size = positiveInteger("size", request.size(), defaultPageSize);
        var response = search.search(term, page, size, bearer, deadline);
        return ResponseEntity.ok().contentType(APPLICATION_JSON)
                .header("total_records", Long.toString(response.totalRecords()))
                .body(response.json());
    }

    private Request readRequest(HttpServletRequest servletRequest) {
        if (servletRequest.getContentLengthLong() > MAX_BODY_BYTES) {
            throw new ExternalApiException(PAYLOAD_TOO_LARGE, "Organisation search body exceeds 6 MiB");
        }
        try {
            byte[] body = servletRequest.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
            if (body.length > MAX_BODY_BYTES) {
                throw new ExternalApiException(PAYLOAD_TOO_LARGE, "Organisation search body exceeds 6 MiB");
            }
            Request request = mapper.readerFor(Request.class).with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(body);
            if (request != null) {
                return request;
            }
        } catch (IOException ex) {
            throw new InvalidRequest("Invalid organisation search request body");
        }
        throw new InvalidRequest("Missing organisation search request body");
    }

    private static SearchDeadline deadline(String supplied) {
        if (supplied == null || supplied.isBlank()) {
            return new SearchDeadline(Duration.ofMillis(MAX_BUDGET_MILLIS));
        }
        final long expiresAt;
        try {
            if (!supplied.matches("[0-9]+")) {
                throw new NumberFormatException();
            }
            expiresAt = Long.parseLong(supplied);
        } catch (NumberFormatException ex) {
            throw new InvalidRequest(DEADLINE_HEADER + " must be an epoch timestamp in milliseconds");
        }
        long remainingMillis = expiresAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            throw SearchDeadline.expired();
        }
        return new SearchDeadline(Duration.ofMillis(Math.min(remainingMillis, MAX_BUDGET_MILLIS)));
    }

    private static int positiveInteger(String field, JsonNode value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (!value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() < 1) {
            throw new InvalidRequest(field + " must be a positive integer");
        }
        return value.intValue();
    }

    public record Request(JsonNode searchTerm, List<String> statuses, JsonNode page, JsonNode size) {
    }
}
