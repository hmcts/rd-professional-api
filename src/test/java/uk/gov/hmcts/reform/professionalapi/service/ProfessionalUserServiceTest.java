package uk.gov.hmcts.reform.professionalapi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.ws.http.HTTPException;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

public class ProfessionalUserServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname",
            "some-email",
            ProfessionalUserStatus.PENDING,
            mock(Organisation.class));
    private final ProfessionalUserService professionalUserService = new ProfessionalUserServiceImpl(
            professionalUserRepository);

    @Test
    public void retrieveUserByEmail() {
        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress("some-email");
        assertEquals(professionalUser.getFirstName(), user.getFirstName());
        assertEquals(professionalUser.getLastName(), user.getLastName());
        assertEquals(professionalUser.getEmailAddress(), user.getEmailAddress());
    }

    @Test(expected = HTTPException.class)
    public void retrieveUserByEmailNotFound() {
        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        professionalUserService.findProfessionalUserByEmailAddress("some-email");
    }

}