package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class DxAddressCreationRequestValidatorImpl implements RequestValidator {

    Boolean  isDxRequestValid = true;
    //TODO refactor to use validation object

    @Override
    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        List<ContactInformationCreationRequest> contactInformationCreationRequests = organisationCreationRequest.getContactInformation();
        contactInformationCreationRequests.forEach(contactInfo -> {
            List<DxAddressCreationRequest> dxAddresses = contactInfo.getDxAddress();
            if (!CollectionUtils.isEmpty(dxAddresses)) {
                dxAddresses.forEach(dxAdd -> {
                    if (!isDxNumberValid(dxAdd.getDxNumber()) || dxAdd.getDxExchange() == null) {

                        log.error("DX Address Number OR DX Exchange should not be null");
                        isDxRequestValid = false;
                    }
                    dxAdd.setIsDxRequestValid(isDxRequestValid);
                });
            }
        });
    }

    private Boolean isDxNumberValid(String dxNumber) {

        Boolean numberIsValid = true;

        if (dxNumber != null) {

            String regex = "^(?:DX|NI) [0-9]{10}+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(dxNumber);
            numberIsValid = matcher.matches();
        }

        return numberIsValid;

    }

}
