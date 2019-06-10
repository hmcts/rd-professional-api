package uk.gov.hmcts.reform.professionalapi.controller.FeinClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreateResponse;

@FeignClient(name="UserProfileClient", url="localhost:8091/profiles")
public interface UserProfileFeignClient {
        @PostMapping(consumes= MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE )
        UserProfileCreateResponse createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);
}
