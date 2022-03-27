package uk.gov.hmcts.reform.professionalapi.controller.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationEntityResponse extends OrganisationMinimalInfoResponse {

    @JsonProperty
    private OrganisationStatus status;
    @JsonProperty
    private String statusMessage;
    @JsonProperty
    private String sraId;
    @JsonProperty
    private Boolean sraRegulated;
    @JsonProperty
    private String companyNumber;
    @JsonProperty
    private String companyUrl;
    @JsonProperty
    private SuperUserResponse superUser;
    @JsonProperty
    private List<String> paymentAccount;
    @JsonInclude(NON_EMPTY)
    @JsonProperty
    private List<String> pendingPaymentAccount;
    @JsonProperty
    @DateTimeFormat
    private LocalDateTime dateReceived;
    @JsonProperty
    @DateTimeFormat
    private LocalDateTime dateApproved;



    public OrganisationEntityResponse(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas) {

        if (nonNull(organisation)) {
            getOrganisationEntityResponse(
                    organisation, isRequiredContactInfo, isRequiredPendingPbas, isRequiredAllPbas);
        }
    }

    private void getOrganisationEntityResponse(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas) {

        this.organisationIdentifier = ObjectUtils.isEmpty(organisation.getOrganisationIdentifier())
                ? "" : organisation.getOrganisationIdentifier();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.statusMessage = organisation.getStatusMessage();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();
        if (!organisation.getUsers().isEmpty()) {
            this.superUser = new SuperUserResponse(organisation.getUsers().get(0));
        }

        if (Boolean.TRUE.equals(isRequiredAllPbas)) {
            this.paymentAccount = organisation.getPaymentAccounts()
                    .stream()
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .collect(toList());
        } else {
            this.paymentAccount = organisation.getPaymentAccounts()
                    .stream()
                    .filter(pba -> pba.getPbaStatus().equals(ACCEPTED))
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .collect(toList());
        }

        if (Boolean.TRUE.equals(isRequiredPendingPbas)) {
            this.pendingPaymentAccount = organisation.getPaymentAccounts()
                    .stream()
                    .filter(pba -> pba.getPbaStatus().equals(PENDING))
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .collect(toList());
        }

        if (Boolean.TRUE.equals(isRequiredContactInfo)) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(ContactInformationResponseWithDxAddress::new)
                    .collect(toList());
        }

        this.dateReceived = organisation.getCreated();
        this.dateApproved = organisation.getDateApproved();

    }
}