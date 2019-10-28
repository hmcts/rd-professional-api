package uk.gov.hmcts.reform.professionalapi.sample;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "citizenservice")
public interface CitizenClient {

  @RequestMapping(method = RequestMethod.GET, path = "/citizen-service/citizens/{id}")
  Citizen getCitizen(@PathVariable("id") Long id);

  @RequestMapping(method = RequestMethod.PUT, path = "/citizen-service/citizens/{id}")
  Citizen updateCitizen(@PathVariable("id") Long id, @RequestBody Citizen citizen);

  @RequestMapping(method = RequestMethod.POST, path = "/citizen-service/citizens")
  IdObject createCitizen(@RequestBody Citizen citizen);

}
