package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;

@Service
@Slf4j
public class PrdEnumServiceImpl implements PrdEnumService {

    PrdEnumRepository prdEnumRepository;

    @Autowired
    public PrdEnumServiceImpl(
            PrdEnumRepository prdEnumRepository) {
        this.prdEnumRepository = prdEnumRepository;
    }

    @Override
    public List<PrdEnum> findAllPrdEnums() {
        return prdEnumRepository.findAll();
    }
}
