package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;

public interface PrdEnumService {

    List<PrdEnum> findAllPrdEnums();

    List<String> getPrdEnumByEnumType(String enumType);
}
