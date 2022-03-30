package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface OrganisationService {

    OrganisationResponse createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest);

    OrganisationsDetailResponse retrieveAllOrganisations();

    OrganisationEntityResponse retrieveOrganisation(String organisationIdentifier, boolean isPendingPbaRequired);

    OrganisationResponse updateOrganisation(OrganisationCreationRequest organisationCreationRequest,
                                            String organisationIdentifier,Boolean isOrgApprovalRequest);

    Organisation getOrganisationByOrgIdentifier(String organisationIdentifier);

    OrganisationsDetailResponse findByOrganisationStatus(String status);

    DeleteOrganisationResponse deleteOrganisation(Organisation organisation, String userId);

    List<Organisation> getOrganisationByStatuses(List<OrganisationStatus> enumStatuses);

    List<Organisation> getOrganisationByStatus(OrganisationStatus status);

    void addPbaAccountToOrganisation(Set<String> paymentAccounts, Organisation organisation, boolean pbasValidated,
                                     boolean isEditPba);

    void deletePaymentsOfOrganisation(Set<String> paymentAccounts, Organisation organisation);

    void updatePaymentAccounts(List<PaymentAccount> pbas);

    ResponseEntity<Object> getOrganisationsByPbaStatus(String pbaStatus);

    ResponseEntity<Object> addPaymentAccountsToOrganisation(PbaRequest pbaRequest,
                                                            String organisationIdentifier, String userId);

    void addContactInformationsToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequest, String organisationIdentifier);

    void deleteMultipleAddressOfGivenOrganisation(Set<UUID> idsSet);

}
