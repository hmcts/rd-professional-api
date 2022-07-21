package uk.gov.hmcts.reform.professionalapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PrdEnumServiceImpl implements PrdEnumService {

    PrdEnumRepository prdEnumRepository;

    List<PrdEnum> enumList = null;

    @Autowired
    public PrdEnumServiceImpl(
            PrdEnumRepository prdEnumRepository) {
        this.prdEnumRepository = prdEnumRepository;
    }

    @Override
    public List<PrdEnum> findAllPrdEnums() {
        if (CollectionUtils.isEmpty(enumList)) {
            enumList = prdEnumRepository.findByEnabled("YES");
        }
        return enumList;
    }

    public List<String> getPrdEnumByEnumType(String enumType) {
        var ignoreEnumList = Arrays.asList(enumType.split(","));
        var enumNameList = new ArrayList<String>();
        var prdEnumList = findAllPrdEnums();
        prdEnumList.forEach(prdEnum -> {
            if (!ignoreEnumList.contains(prdEnum.getPrdEnumId().getEnumType())) {
                enumNameList.add(prdEnum.getEnumName());
            }
        });
        return enumNameList;
    }
}
