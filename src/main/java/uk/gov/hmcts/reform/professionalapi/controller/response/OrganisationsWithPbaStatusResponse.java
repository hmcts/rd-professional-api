package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class OrganisationsWithPbaStatusResponse {

    @JsonProperty
    private String organisationIdentifier;

    @JsonProperty
    private OrganisationStatus organisationStatus;

    @JsonProperty
    private List<FetchPbaByStatusResponse> pbaNumbers;

    public OrganisationsWithPbaStatusResponse(String organisationIdentifier, OrganisationStatus organisationStatus,
                                              List<PaymentAccount> paymentAccounts) {

        this.organisationIdentifier = organisationIdentifier;
        this.organisationStatus = organisationStatus;
        this.pbaNumbers = paymentAccounts
                .stream()
                .map(FetchPbaByStatusResponse::new)
                .collect(Collectors.toList());
    }
}
