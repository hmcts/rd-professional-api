package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;

import java.util.List;

public interface PrdEnumService {

    List<PrdEnum> findAllPrdEnums();

    List<String> getPrdEnumByEnumType(String enumType);
}
