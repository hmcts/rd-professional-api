package uk.gov.hmcts.reform.professionalapi.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;


@FeignClient(name = "UserProfileClient", url = "${user.profile.api.url}")
public interface UserProfileFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/v1/userprofile")
    @RequestLine("POST /v1/userprofile")
    @Headers({"Authorization: {authorization}", "serviceAuthorization: {serviceAuthorization}", "Content-Type: application/x-www-form-urlencoded"})
    Response createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);
}
