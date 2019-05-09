package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;


public class PaymentAccountResponse {

    @JsonProperty
    private final String organisationIdentifier;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final String status;
    @JsonProperty
    private final String sraId;
    @JsonProperty
    private final Boolean sraRegulated;
    @JsonProperty
    private final String companyNumber;
    @JsonProperty
    private final String companyUrl;
    @JsonProperty
    private final List<SuperUserResponse> superUser;
    @JsonProperty
    private final List<PbaAccountResponse> pbaAccounts;

    public PaymentAccountResponse(Organisation organisation) {
        this.organisationIdentifier = organisation.getOrganisationIdentifier().toString();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();

        this.superUser = organisation.getUsers()
                .stream()
                .map(user -> new SuperUserResponse(user))
                .collect(toList());


        this.pbaAccounts = organisation.getPaymentAccounts()
                .stream()
                .map(pbaAcc -> new PbaAccountResponse(pbaAcc))
                .collect(toList());
    }
}