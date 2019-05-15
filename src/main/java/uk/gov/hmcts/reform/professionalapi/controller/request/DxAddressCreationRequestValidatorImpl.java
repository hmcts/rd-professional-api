package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class DxAddressCreationRequestValidatorImpl implements RequestValidator {

    //TODO refactor to use validation object

    @Override
    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        Boolean  isDxRequestValid;
        List<ContactInformationCreationRequest> contactInformationCreationRequests = organisationCreationRequest.getContactInformation();
        contactInformationCreationRequests.forEach(contactInfo -> {
            List<DxAddressCreationRequest> dxAddresses = contactInfo.getDxAddress();
            if (!CollectionUtils.isEmpty(dxAddresses)) {
                dxAddresses.forEach(dxAdd -> {
                    if (dxAdd.getDxNumber() != null && dxAdd.getDxExchange() != null) {
                        if ((dxAdd.getDxNumber().startsWith("DX ") || dxAdd.getDxNumber().startsWith("NI "))) {
                            String[] dxNumberToken = dxAdd.getDxNumber().split(" ");
                            String dxNumberDigits = dxNumberToken[1];
                            if (dxNumberDigits == null || dxNumberDigits.length() != 10) {
                                log.error("DX Address Number should contain ten numerical digits");
                                dxAdd.setIsDxRequestValid(false);
                            }
                            try {
                                Integer.valueOf(dxNumberDigits);
                            } catch (NumberFormatException ne) {
                                log.error("DX Address Number is invalid format");
                                dxAdd.setIsDxRequestValid(false);
                            }
                        } else {
                            log.error("DX Address Number should start with either 'DX ' or 'NI ' and be followed by a space");
                            dxAdd.setIsDxRequestValid(false);
                        }
                    } else {
                        log.error("DX Address Number OR DX Exchange should not be null");
                        dxAdd.setIsDxRequestValid(false);
                    }
                });
            }
        });
    }
}
