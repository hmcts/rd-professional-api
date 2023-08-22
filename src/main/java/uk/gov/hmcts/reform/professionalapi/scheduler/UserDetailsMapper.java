package uk.gov.hmcts.reform.professionalapi.scheduler;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trim;

@Component
public class UserDetailsMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object userDetailsObj) {

        UserDetails userDetails = (UserDetails) userDetailsObj;
        Map<String, Object> userDetailsParamMap = new HashMap<>();

        userDetailsParamMap.put("id", trim(userDetails.getId()));
        userDetailsParamMap.put("first_name", trim(userDetails.getFirstName()));
        userDetailsParamMap.put("last_name", trim(userDetails.getLastName()));

        return userDetailsParamMap;
    }
}
