package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Getter
public class ContactInformationResponse {

    @JsonProperty
    private  String addressLine1;
    @JsonProperty
    private  String addressLine2;
    @JsonProperty
    private  String addressLine3;
    @JsonProperty
    private  String townCity;
    @JsonProperty
    private  String county;
    @JsonProperty
    private  String country;
    @JsonProperty
    private  String postCode;
    @JsonProperty
    private  List<DxAddressResponse> dxAddress;

    public ContactInformationResponse(ContactInformation contactInfo) {

        getContactInfoMapping(contactInfo);

    }

    private void getContactInfoMapping(ContactInformation contactInfo) {

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getAddressLine1()))) {

            this.addressLine1 = contactInfo.getAddressLine1().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getAddressLine2()))) {

            this.addressLine2 = contactInfo.getAddressLine2().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getAddressLine3()))) {

            this.addressLine3 = contactInfo.getAddressLine3().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getTownCity()))) {

            this.townCity = contactInfo.getTownCity().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getCounty()))) {

            this.county = contactInfo.getCounty().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getCountry()))) {

            this.country = contactInfo.getCountry().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(contactInfo.getPostCode()))) {

            this.postCode = contactInfo.getPostCode().trim();
        }

        if (!CollectionUtils.isEmpty(contactInfo.getDxAddresses())) {

            this.dxAddress = contactInfo.getDxAddresses()
                    .stream()
                    .map(dxAddres ->  new DxAddressResponse(dxAddres))
                    .collect(toList());

        }
    }

}
