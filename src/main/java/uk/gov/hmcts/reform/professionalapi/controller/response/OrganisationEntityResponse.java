package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;

public class OrganisationEntityResponse extends OrganisationMinimalInfoResponse {

    @JsonProperty
    protected OrganisationStatus status;
    @JsonProperty
    protected String statusMessage;
    @JsonProperty
    protected String sraId;
    @JsonProperty
    protected Boolean sraRegulated;
    @JsonProperty
    protected String companyNumber;
    @JsonProperty
    protected String companyUrl;
    @JsonProperty
    protected SuperUserResponse superUser;
    @JsonProperty
    protected List<String> paymentAccount;
    @JsonProperty
    protected List<String> pendingPaymentAccount = new ArrayList<>();
    @JsonProperty
    @DateTimeFormat
    protected LocalDateTime dateReceived;
    @JsonProperty
    @DateTimeFormat
    @JsonInclude(ALWAYS)
    protected LocalDateTime dateApproved = null;
    @JsonProperty
    @DateTimeFormat
    @JsonInclude(ALWAYS)
    protected LocalDateTime lastUpdated = null;
    @JsonProperty
    protected List<String> organisationProfileIds;

    private static final String DEFAULT_ORG_PROFILE_ID = "SOLICITOR_PROFILE";
    private static final Map<String, List<String>> ORG_TYPE_TO_ORG_PROFILE_IDS = Map.ofEntries(
            new SimpleEntry<String, List<String>>("SOLICITOR_ORG", List.of("SOLICITOR_PROFILE")),
            new SimpleEntry<String, List<String>>("LOCAL_AUTHORITY_ORG", List.of("SOLICITOR_PROFILE")),
            new SimpleEntry<String, List<String>>("OTHER_ORG", List.of("SOLICITOR_PROFILE")),
            new SimpleEntry<String, List<String>>("OGD_DWP_ORG", List.of("OGD_DWP_PROFILE")),
            new SimpleEntry<String, List<String>>("OGD_HO_ORG", List.of("OGD_HO_PROFILE")),
            new SimpleEntry<String, List<String>>("OGD_OTHER_ORG", List.of("SOLICITOR_PROFILE"))
    );

    public OrganisationEntityResponse(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas) {

        if (nonNull(organisation)) {
            getOrganisationEntityResponse(
                    organisation, isRequiredContactInfo, isRequiredPendingPbas, isRequiredAllPbas);
        }
    }

    @SuppressWarnings("java:S6204")
    protected void getOrganisationEntityResponse(
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
        this.lastUpdated = organisation.getLastUpdated();
        this.organisationProfileIds = getOrganisationProfileIds(organisation);

    }

    private List<String> getOrganisationProfileIds(Organisation organisation) {
        if (organisation.getOrgType() == null) {
            return Arrays.asList(DEFAULT_ORG_PROFILE_ID);
        }
        return ORG_TYPE_TO_ORG_PROFILE_IDS.get(organisation.getOrgType());
    }
}
