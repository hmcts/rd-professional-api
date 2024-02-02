package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.BulkCustomerOrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponseV2;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface OrganisationService {

    OrganisationResponse createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest);

    OrganisationsDetailResponse retrieveAllOrganisations(Pageable pageable);

    BulkCustomerOrganisationsDetailResponse retrieveOrganisationDetailsForBulkCustomer(String bulkCustId,
                                                                                       String idamId);

    OrganisationsDetailResponseV2 retrieveAllOrganisationsForV2Api(Pageable pageable);

    OrganisationEntityResponseV2 retrieveOrganisationForV2Api(String organisationIdentifier,
                                                              boolean isPendingPbaRequired);

    OrganisationsDetailResponseV2 findByOrganisationStatusForV2Api(String status, Pageable pageable);

    OrganisationEntityResponse retrieveOrganisation(String organisationIdentifier, boolean isPendingPbaRequired);

    OrganisationResponse updateOrganisation(OrganisationCreationRequest organisationCreationRequest,
                                            String organisationIdentifier,Boolean isOrgApprovalRequest);

    Organisation getOrganisationByOrgIdentifier(String organisationIdentifier);

    OrganisationsDetailResponse findByOrganisationStatus(String status, Pageable pageable);

    DeleteOrganisationResponse deleteOrganisation(Organisation organisation, String userId);

    List<Organisation> getOrganisationByStatuses(List<OrganisationStatus> enumStatuses, Pageable pageable);

    List<Organisation> getOrganisationByStatus(OrganisationStatus status);

    List<PaymentAccount> addPbaAccountToOrganisation(Set<String> paymentAccounts, Organisation organisation,
                                                     boolean pbasValidated, boolean isEditPba);

    void deletePaymentsOfOrganisation(Set<String> paymentAccounts, Organisation organisation);

    void updatePaymentAccounts(List<PaymentAccount> pbas);

    ResponseEntity<Object> getOrganisationsByPbaStatus(String pbaStatus);

    ResponseEntity<Object> addPaymentAccountsToOrganisation(PbaRequest pbaRequest,
                                                            String organisationIdentifier, String userId);

    void addContactInformationsToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequest, String organisationIdentifier);

    ResponseEntity<ContactInformationResponse>  updateContactInformationForOrganisation(
        List<ContactInformationCreationRequest> contactInformationCreationRequest, String organisationIdentifier);

    void deleteMultipleAddressOfGivenOrganisation(Set<UUID> idsSet);

    void deleteOrgAttribute(List<OrgAttributeRequest> orgAttributes, String organisationIdentifier);

    ResponseEntity<OrganisationEntityResponse> retrieveOrganisationByUserId(String userId);

}
