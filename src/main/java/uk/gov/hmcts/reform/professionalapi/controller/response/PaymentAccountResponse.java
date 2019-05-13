package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;


public class PaymentAccountResponse {

    @JsonProperty
    private String organisationIdentifier;
    @JsonProperty
    private String name;
    @JsonProperty
    private String status;
    @JsonProperty
    private String sraId;
    @JsonProperty
    private Boolean sraRegulated;
    @JsonProperty
    private String companyNumber;
    @JsonProperty
    private String companyUrl;
    @JsonProperty
    private List<SuperUserResponse> superUser;
    @JsonProperty
    private List<PbaAccountResponse> pbaAccounts;

    public PaymentAccountResponse(Organisation organisation) {

        getPaymentAccountResponse(organisation);

    }

    private void getPaymentAccountResponse(Organisation organisation) {

        this.organisationIdentifier = StringUtils.isEmpty(organisation.getOrganisationIdentifier())
                ? "" : organisation.getOrganisationIdentifier().toString();
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