package uk.gov.hmcts.reform.professionalapi.domain.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response.OrganisationResponse;

public class OrganisationServiceTest {

    private final ProfessionalUserRepository      professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final PaymentAccountRepository        paymentAccountRepository   = mock(PaymentAccountRepository.class);
    private final OrganisationRepository          organisationRepository     = mock(OrganisationRepository.class);
    private final UserCreationRequest             superUser                  = new UserCreationRequest("some-fname",
                                                                                                       "some-lname",
                                                                                                       "some-email",
                                                                                                       null);
    private final List<PbaAccountCreationRequest> pbaAccountCreationRequests = new ArrayList<>();
    private final PbaAccountCreationRequest       pbaAccountCreationRequest  =
            new PbaAccountCreationRequest("pbaNumber-1");
    private final OrganisationService             organisationService        =
            new OrganisationService(organisationRepository,
                                    professionalUserRepository,
                                    paymentAccountRepository);

    @Before
    public void setUp() {

        pbaAccountCreationRequests.add(pbaAccountCreationRequest);

        when(professionalUserRepository.save(any(ProfessionalUser.class)))
                .thenAnswer(arg -> {
                    ProfessionalUser user = arg.getArgument(0);
                    if (user.getId() == null) {
                        user.setId(UUID.randomUUID());
                    }
                    return user;
                });

        when(organisationRepository.save(any(Organisation.class)))
                .thenAnswer(arg -> {
                    Organisation org = arg.getArgument(0);
                    if (org.getId() == null) {
                        org.setId(UUID.randomUUID());
                    }
                    return org;
                });

        when(paymentAccountRepository.save(any(PaymentAccount.class)))
                .thenAnswer(arg -> {
                    PaymentAccount account = arg.getArgument(0);
                    if (account.getId() == null) {
                        account.setId(UUID.randomUUID());
                    }
                    return account;
                });

    }

    @Test
    public void saves_an_organisation_with_pba() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest("some-org-name",
                                                superUser,
                                                pbaAccountCreationRequests);

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

        ArgumentCaptor<Organisation> orgCaptor = ArgumentCaptor.forClass(Organisation.class);
        verify(organisationRepository,
               atLeastOnce()).save(orgCaptor.capture());
        ArgumentCaptor<ProfessionalUser> userCaptor = ArgumentCaptor.forClass(ProfessionalUser.class);
        verify(professionalUserRepository,
               atLeastOnce()).save(userCaptor.capture());
        ArgumentCaptor<PaymentAccount> accountCaptor = ArgumentCaptor.forClass(PaymentAccount.class);
        verify(paymentAccountRepository,
               atLeastOnce()).save(accountCaptor.capture());

        Organisation org = orgCaptor.getValue();
        PaymentAccount account = accountCaptor.getValue();
        ProfessionalUser user = userCaptor.getValue();

        // Check response is correct
        assertThat(organisationResponse).isNotNull();
        assertThat(organisationResponse.getName()).isEqualTo("some-org-name");
        assertThat(organisationResponse.getPbaAccounts()).containsAll(pbaAccountCreationRequests.stream()
                .map(it -> it.getPbaNumber())
                .collect(Collectors.toList()));
        assertThat(organisationResponse.getUserIds()).contains(user.getId().toString());

        // Check organisation is correctly saved
        assertThat(org.getPaymentAccounts().contains(account)).isTrue();

        // Check account is correctly saved
        assertThat(account.getOrganisation()).isEqualTo(org);
        assertThat(account.getPbaNumber()).isEqualTo(pbaAccountCreationRequest.getPbaNumber());
        assertThat(account.getUsers()).isNull();

        // Check user is correctly saved
        assertThat(user.getFirstName()).isEqualTo(superUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(superUser.getLastName());
        assertThat(user.getEmailAddress()).isEqualTo(superUser.getEmail());
        assertThat(user.getPaymentAccount()).isNull();
        assertThat(user.getOrganisation()).isEqualTo(org);
    }

    @Test
    public void saves_an_organisation_with_pba_assigned_to_superuser() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest("some-org-name",
                                                new UserCreationRequest("some-fname",
                                                                        "some-lname",
                                                                        "some-email",
                                                                        pbaAccountCreationRequest),
                                                pbaAccountCreationRequests);

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

        ArgumentCaptor<Organisation> orgCaptor = ArgumentCaptor.forClass(Organisation.class);
        verify(organisationRepository,
               atLeastOnce()).save(orgCaptor.capture());
        ArgumentCaptor<ProfessionalUser> userCaptor = ArgumentCaptor.forClass(ProfessionalUser.class);
        verify(professionalUserRepository,
               atLeastOnce()).save(userCaptor.capture());
        ArgumentCaptor<PaymentAccount> accountCaptor = ArgumentCaptor.forClass(PaymentAccount.class);
        verify(paymentAccountRepository,
               atLeastOnce()).save(accountCaptor.capture());

        Organisation org = orgCaptor.getValue();
        PaymentAccount account = accountCaptor.getValue();
        ProfessionalUser user = userCaptor.getValue();

        // Check response is correct
        assertThat(organisationResponse).isNotNull();
        assertThat(organisationResponse.getName()).isEqualTo("some-org-name");
        assertThat(organisationResponse.getPbaAccounts()).containsAll(pbaAccountCreationRequests.stream()
                .map(it -> it.getPbaNumber())
                .collect(Collectors.toList()));
        assertThat(organisationResponse.getUserIds()).contains(user.getId().toString());

        // Check organisation is correctly saved
        assertThat(org.getPaymentAccounts().contains(account)).isTrue();

        // Check account is correctly saved
        assertThat(account.getOrganisation()).isEqualTo(org);
        assertThat(account.getPbaNumber()).isEqualTo(pbaAccountCreationRequest.getPbaNumber());
        assertThat(account.getUsers()).contains(user);

        // Check user is correctly saved
        assertThat(user.getFirstName()).isEqualTo(superUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(superUser.getLastName());
        assertThat(user.getEmailAddress()).isEqualTo(superUser.getEmail());
        assertThat(user.getPaymentAccount()).isEqualTo(account);
        assertThat(user.getOrganisation()).isEqualTo(org);
    }
}