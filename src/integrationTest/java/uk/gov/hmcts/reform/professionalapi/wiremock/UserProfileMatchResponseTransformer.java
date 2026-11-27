package uk.gov.hmcts.reform.professionalapi.wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.MatchResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.Profile;

import java.util.List;

public class UserProfileMatchResponseTransformer extends ResponseTransformer {

    public static final String NAME = "user-profile-match-response";
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        try {
            String userId = mapper.readTree(request.getBodyAsString()).required("userIds").required(0).asText();
            var profile = new Profile(userId, parameters.getString("firstName"), parameters.getString("lastName"),
                parameters.getString("email"));
            return Response.Builder.like(response).but()
                .body(mapper.writeValueAsString(new MatchResponse(List.of(profile), List.of()))).build();
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid profile-match fixture request", ex);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
