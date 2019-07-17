package uk.gov.hmcts.reform.professionalapi.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.professionalapi.configuration.FeignInterceptorConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;


@FeignClient(name = "UserProfileClient", url = "${userProfUrl}", configuration = FeignInterceptorConfiguration.class)
public interface UserProfileFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/v1/userprofile")
    @RequestLine("POST /v1/userprofile")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}", "Content-Type: application/json"})
    Response createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);

    @RequestMapping(method = RequestMethod.GET, value = "/v1/userprofile", params = "userId")
    @RequestLine("GET /v1/userprofile")
    @Headers({"Authorization: {authorization}","ServiceAuthorization: {serviceAuthorization}", "Content-Type: application/json"})
    Response getUserProfileByEmail(@RequestParam("userId") String userId);

}
