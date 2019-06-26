package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProfessionalUserReqValidator {


    public void validateRequest(String orgId, String showDeleted, String email) {

        if (null == orgId && null == showDeleted && null == email) {
            log.error("No input values for the request");
            throw new EmptyResultDataAccessException(1);
        }
    }
}
