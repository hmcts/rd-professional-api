package uk.gov.hmcts.reform.professionalapi.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.professionalapi.configuration.FeignInterceptorConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;

import javax.validation.constraints.NotBlank;


@FeignClient(name = "UserProfileClient", url = "${userProfUrl}", configuration = FeignInterceptorConfiguration.class)
public interface UserProfileFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/v1/userprofile")
    @RequestLine("POST /v1/userprofile")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}", "Content-Type: application/json"})
    Response createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);

    @RequestMapping(method = RequestMethod.GET, value = "/v1/userprofile", params = "userId")
    @RequestLine("GET /v1/userprofile")
    @Headers({"Authorization: {authorization}","ServiceAuthorization: {serviceAuthorization}", "Content-Type: application/json"})
    Response getUserProfileById(@RequestParam("userId") String userId);

    @RequestMapping(method = RequestMethod.POST, value = "/v1/userprofile/users", params = "showdeleted")
    @RequestLine("POST /v1/userprofile/users")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}", "Content-Type: application/json"})
    Response getUserProfiles(@RequestBody RetrieveUserProfilesRequest retrieveUserProfilesRequest,
                                     @RequestParam(value = "showdeleted") String showDeleted);
}
