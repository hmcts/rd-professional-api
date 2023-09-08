package uk.gov.hmcts.reform.professionalapi.dataload.mapper;

import java.util.Map;

public interface IMapper {

    Map<String, Object> getMap(Object userProfile);
}
