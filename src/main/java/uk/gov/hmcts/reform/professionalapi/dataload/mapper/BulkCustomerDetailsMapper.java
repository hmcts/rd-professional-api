package uk.gov.hmcts.reform.professionalapi.dataload.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.binder.BulkCustomerDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.generateRandomUUID;

@Component
public class BulkCustomerDetailsMapper implements IMapper {


    @Override
    public Map<String, Object> getMap(Object bulkCustomerDetailsObj) {
        BulkCustomerDetails bulkCustomerDetails = (BulkCustomerDetails) bulkCustomerDetailsObj;
        Map<String, Object> bulkCustomerDetailsParamMap = new HashMap<>();
        bulkCustomerDetailsParamMap.put("id", generateRandomUUID());
        bulkCustomerDetailsParamMap.put("organisation_id", bulkCustomerDetails.getOrganisationId());
        bulkCustomerDetailsParamMap.put("bulk_customer_id", trim(bulkCustomerDetails.getBulkCustomerId()));
        bulkCustomerDetailsParamMap.put("sidam_id", trim(bulkCustomerDetails.getSidamId()));
        bulkCustomerDetailsParamMap.put("pba_number", trim(bulkCustomerDetails.getPbaNumber()));
        return bulkCustomerDetailsParamMap;
    }
}
