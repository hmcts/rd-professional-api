package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ActiveOrganisationSearchController;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Page;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Representative;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Row;
import uk.gov.hmcts.reform.professionalapi.service.impl.ActiveOrganisationSearch;
import uk.gov.hmcts.reform.professionalapi.service.impl.SearchCancellation;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.MatchResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.Profile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ActiveOrganisationSearchProviderStates {
    private final ObjectMapper mapper = new ProviderTestConfiguration().defaultObjectMapper();
    private final ActiveOrganisationSearchRepository repository = mock(ActiveOrganisationSearchRepository.class);
    private final UserProfileMatchClient profiles = mock(UserProfileMatchClient.class);
    private final ActiveOrganisationSearch search = new ActiveOrganisationSearch(repository, profiles, mapper,
            mock(SearchCancellation.class));


    ActiveOrganisationSearchController controller() {
        return new ActiveOrganisationSearchController(search, mapper, 10);
    }

    void stopWorkers() {
        ReflectionTestUtils.invokeMethod(search, "shutdown");
    }

    @State("Active combined search has a current administrator")
    public void combined() {
        var rep = new Representative(new UUID(0, 1), "synthetic-a");
        when(repository.candidates(any())).thenReturn(List.of(rep));
        when(profiles.serviceToken(any())).thenReturn("Bearer s2s-token");
        when(profiles.match(eq(List.of("synthetic-a")), eq("smith SW1A 1AA"), eq("Bearer test-token"),
                eq("Bearer s2s-token"), any())).thenReturn(new MatchResponse(List.of(
                        new Profile("synthetic-a", "Ada", "Smith", "ada@example.invalid")), List.of()));
        when(repository.loadPage(eq("smith SW1A 1AA"), eq(0L), eq(10), eq(Map.of(rep, true)), any()))
                .thenReturn(new Page(List.of(new Row(organisation(), rep)), 1, false));
    }

    @State("Active browse includes an organisation without an administrator")
    public void browse() {
        when(repository.loadPage(eq(""), eq(0L), eq(10), eq(Map.of()), any()))
                .thenReturn(new Page(List.of(new Row(organisation(), new Representative(new UUID(0, 1), null))),
                        1, false));
    }

    @State("Active search beyond the last page preserves its exact total")
    public void pastEnd() {
        when(repository.candidates(any())).thenReturn(List.of());
        when(repository.loadPage(eq("smith SW1A 1AA"), eq(130L), eq(10), eq(Map.of()), any()))
                .thenReturn(new Page(List.of(), 123, false));
    }

    @State("Active search has no matches")
    public void empty() {
        when(repository.candidates(any())).thenReturn(List.of());
        when(repository.loadPage(eq("absent"), eq(0L), eq(10), eq(Map.of()), any()))
                .thenReturn(new Page(List.of(), 0, false));
    }

    private OrganisationEntityResponse organisation() {
        var org = new Organisation();
        org.setOrganisationIdentifier("SYN0001");
        org.setName("Smith Legal");
        org.setStatus(OrganisationStatus.ACTIVE);
        org.setCompanyNumber("A1000");
        org.setCompanyUrl("https://example.invalid");
        org.setSraId("synthetic-sra");
        org.setSraRegulated(true);
        org.setCreated(LocalDateTime.parse("2026-01-01T00:00:00"));
        var contact = new ContactInformation();
        contact.setAddressLine1("1 Synthetic Street");
        contact.setPostCode("SW1A 1AA");
        contact.addDxAddress(new DxAddress("DX1", "Synthetic", contact));
        org.addContactInformation(contact);
        var accepted = new PaymentAccount();
        accepted.setPbaNumber("PBA0000001");
        accepted.setPbaStatus(PbaStatus.ACCEPTED);
        var pending = new PaymentAccount();
        pending.setPbaNumber("PBA0000002");
        pending.setPbaStatus(PbaStatus.PENDING);
        org.setPaymentAccounts(List.of(accepted, pending));
        return new OrganisationEntityResponse(org, true, true, false);
    }
}
