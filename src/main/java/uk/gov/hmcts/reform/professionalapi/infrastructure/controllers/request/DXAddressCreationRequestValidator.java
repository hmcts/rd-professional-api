package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static java.util.Objects.requireNonNull;

import java.util.List;

@Service
@Slf4j
public class DXAddressCreationRequestValidator implements OrganisationRequestValidator {

    Boolean  isDxRequestValid = true;

    @Override
    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        List<ContactInformationCreationRequest> contactInformationCreationRequests = organisationCreationRequest.getContactInformation();
        contactInformationCreationRequests.forEach(contactInfo -> {
            List<DXAddressCreationRequest> dxAddresses = contactInfo.getDxAddress();
            if (!CollectionUtils.isEmpty(dxAddresses)) {
                dxAddresses.forEach(dxAdd -> {
                    if (dxAdd.getDxNumber() != null && dxAdd.getDxExchange() != null) {
                        if ((dxAdd.getDxNumber().startsWith("DX ") || dxAdd.getDxNumber().startsWith("NI "))) {
                            String dxNumberToken[] = dxAdd.getDxNumber().split(" ");
                            String dxNumberDigits = dxNumberToken[1];
                            if (dxNumberDigits == null || dxNumberDigits.length() != 10) {
                                log.error("DX Address Number should contain 10 numerical digits");
                                isDxRequestValid = false;
                            }
                            try {
                                Integer.valueOf(dxNumberDigits);
                            } catch (NumberFormatException ne) {
                                log.error("DX Address Number is invalid format");
                                isDxRequestValid = false;
                            }
                        } else {
                            log.error("DX Address Number should start with either 'DX ' or 'NI ' and be followed by a space");
                            isDxRequestValid = false;
                        }
                    } else {
                        log.error("DX Address Number OR DX Exchange should not be null");
                        isDxRequestValid = false;
                    }
                   dxAdd.setIsDXRequestValid(isDxRequestValid);
                });
            }
        });
    }

}
