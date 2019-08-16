package uk.gov.hmcts.reform.professionalapi.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.professionalapi.controller.request.JurisdictionUserCreationRequest;

@FeignClient(name = "JurisdictionCreateUSerClient", url = "${ccdUrl}")
public interface JurisdictionFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/user-profile/users")
    @RequestLine("POST /user-profile/users")
    @Headers("Content-Type: application/json")
    Response createJurisdictionUserProfile(@RequestHeader("actionedBy") String email,
                                           @RequestHeader("ServiceAuthorization") String auth,
                                           @RequestBody JurisdictionUserCreationRequest jurisdictionUserCreationRequest);
}
