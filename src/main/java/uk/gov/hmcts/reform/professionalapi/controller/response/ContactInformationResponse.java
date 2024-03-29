package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class ContactInformationResponse {
    protected UUID addressId;
    @JsonProperty
    protected String uprn;
    @JsonProperty
    protected LocalDateTime created;
    @JsonProperty
    protected String addressLine1;
    @JsonProperty
    protected String addressLine2;
    @JsonProperty
    protected String addressLine3;
    @JsonProperty
    protected String townCity;
    @JsonProperty
    protected String county;
    @JsonProperty
    protected String country;
    @JsonProperty
    protected String postCode;

    public ContactInformationResponse(ContactInformation contactInfo) {
        this.uprn = contactInfo.getUprn();
        this.addressLine1 = contactInfo.getAddressLine1();
        this.addressLine2 = contactInfo.getAddressLine2();
        this.addressLine3 = contactInfo.getAddressLine3();
        this.townCity = contactInfo.getTownCity();
        this.county = contactInfo.getCounty();
        this.country = contactInfo.getCountry();
        this.addressId = contactInfo.getId();
        this.uprn = contactInfo.getUprn();
        this.created = contactInfo.getCreated();
        this.postCode = contactInfo.getPostCode();
    }
}
