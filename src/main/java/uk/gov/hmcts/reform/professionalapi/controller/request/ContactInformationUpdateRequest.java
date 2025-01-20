package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NotNull(message = "ContactInformationUpdateRequest is required")
public class ContactInformationUpdateRequest {

    @JsonProperty(value = "addresses")
    private List<ContactInformationUpdateData> contactInformationUpdateData;

    public ContactInformationUpdateRequest() {

    }

    @Getter
    @Setter
    public static class ContactInformationUpdateData {

        @Valid
        @NotNull(message = " organisation Id is required.")
        private String organisationId;

        boolean dxAddressUpdate;
        boolean contactInformationUpdate;
        String addressid;

        private final String uprn;

        @NotEmpty
        @NotNull(message = "AddressLine1 is required")
        private final String addressLine1;

        private final String addressLine2;

        private final String addressLine3;

        private final String townCity;

        private final String county;

        private final String country;

        private final String postCode;

        private final List<DxAddressUpdateRequest> dxAddress;

        @JsonCreator
        public ContactInformationUpdateData(
            @JsonProperty("organisationId") String organisationId,
            @JsonProperty("dxAddressUpdate") boolean dxAddressUpdate,
            @JsonProperty("contactInformationUpdate") boolean contactInformationUpdate,
            @JsonProperty("addressid") String addressid,
            @JsonProperty("uprn") String uprn,
            @JsonProperty("addressLine1") String addressLine1,
            @JsonProperty("addressLine2") String addressLine2,
            @JsonProperty("addressLine3") String addressLine3,
            @JsonProperty("townCity") String townCity,
            @JsonProperty("county") String county,
            @JsonProperty("country") String country,
            @JsonProperty("postCode") String postCode,
            @JsonProperty("dxAddress") List<DxAddressUpdateRequest> dxAddress) {
            this.organisationId = organisationId;
            this.dxAddressUpdate = dxAddressUpdate;
            this.contactInformationUpdate = contactInformationUpdate;
            this.addressid = addressid;
            this.uprn = uprn;
            this.addressLine1 = addressLine1;
            this.addressLine2 = addressLine2;
            this.addressLine3 = addressLine3;
            this.townCity = townCity;
            this.county = county;
            this.country = country;
            this.postCode = postCode;
            this.dxAddress = dxAddress;
        }
    }
}