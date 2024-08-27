package uk.gov.hmcts.reform.professionalapi.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.professionalapi.configuration.FeignInterceptorConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

@FeignClient(name = "UserProfileClient", url = "${userProfUrl}", configuration = FeignInterceptorConfiguration.class)
@SuppressWarnings("checkstyle:Indentation")
public interface UserProfileFeignClient {

    @PostMapping(value = "/v1/userprofile")
    @RequestLine("POST /v1/userprofile")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);

    @GetMapping(value = "/v1/userprofile", params = "userId")
    @RequestLine("GET /v1/userprofile")
    @Headers({"Authorization: {authorization}","ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response getUserProfileById(@RequestParam("userId") String userId);

    @GetMapping(value = "/v1/userprofile")
    @RequestLine("GET /v1/userprofile")
    @Headers({"Authorization: {authorization}","ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response getUserProfileByEmail(@RequestHeader("UserEmail") String email);

    @PostMapping(value = "/v1/userprofile/users")
    @RequestLine("POST /v1/userprofile/users")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response getUserProfiles(@RequestBody RetrieveUserProfilesRequest retrieveUserProfilesRequest,
                             @RequestParam(value = "showdeleted") String showDeleted,
                             @RequestParam(value = "rolesRequired")String rolesRequired);

    @PutMapping(value = "/v1/userprofile/{userId}")
    @RequestLine("PUT /v1/userprofile/{userId}")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response modifyUserRoles(@RequestBody UserProfileUpdatedData modifyRoles, @PathVariable("userId") String userId,
                             @RequestParam(value = "origin") String origin);

    @DeleteMapping(value = "/v1/userprofile")
    @RequestLine("DELETE /v1/userprofile")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response deleteUserProfile(@RequestBody DeleteUserProfilesRequest deleteUserProfileRequest);
}
