package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;

public class OrganisationEntityResponseV2 extends OrganisationMinimalInfoResponse {

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
    @JsonProperty
    private List<String> pendingPaymentAccount = new ArrayList<>();
    @JsonProperty
    @DateTimeFormat
    private LocalDateTime dateReceived;
    @JsonProperty
    @DateTimeFormat
    @JsonInclude(ALWAYS)
    private LocalDateTime dateApproved = null;

    @JsonProperty
    private String orgTypeKey;

    @JsonProperty
    private List<OrgAttributeResponse> orgAttributes;



    public OrganisationEntityResponseV2(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas,Boolean isRequiredOrAttribute) {

        if (nonNull(organisation)) {
            getOrganisationEntityResponse(
                    organisation, isRequiredContactInfo,
                    isRequiredPendingPbas,
                    isRequiredAllPbas,
                    isRequiredOrAttribute);
        }
    }

    @SuppressWarnings("java:S6204")
    private void getOrganisationEntityResponse(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas,Boolean isRequiredOrAttribute) {

        this.organisationIdentifier = ObjectUtils.isEmpty(organisation.getOrganisationIdentifier())
                ? "" : organisation.getOrganisationIdentifier();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.statusMessage = organisation.getStatusMessage();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.orgTypeKey = organisation.getOrgTypeKey();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();
        if (!organisation.getUsers().isEmpty()) {
            this.superUser = new SuperUserResponse(organisation.getUsers().get(0));
        }

        if (Boolean.TRUE.equals(isRequiredOrAttribute)) {
            this.orgAttributes = organisation.getOrgAttributes()
                    .stream()
                    .map(OrgAttributeResponse::new)
                    .toList();
        }

        if (Boolean.TRUE.equals(isRequiredAllPbas)) {
            this.paymentAccount = organisation.getPaymentAccounts()
                    .stream()
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .toList();
        } else {
            this.paymentAccount = organisation.getPaymentAccounts()
                    .stream()
                    .filter(pba -> pba.getPbaStatus().equals(ACCEPTED))
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .toList();
        }

        if (Boolean.TRUE.equals(isRequiredPendingPbas)) {
            this.pendingPaymentAccount = organisation.getPaymentAccounts()
                    .stream()
                    .filter(pba -> pba.getPbaStatus().equals(PENDING))
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .toList();
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