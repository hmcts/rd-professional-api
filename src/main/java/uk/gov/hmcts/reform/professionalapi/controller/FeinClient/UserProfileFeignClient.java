package uk.gov.hmcts.reform.professionalapi.controller.FeinClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;

@FeignClient(name = "UserProfileClient", url = "rd-user-profile-api:8091")
public interface UserProfileFeignClient {
    @PostMapping(consumes= MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/profiles" )
    ResponseEntity createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);
}
