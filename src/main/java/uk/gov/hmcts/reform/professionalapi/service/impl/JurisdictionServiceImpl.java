package uk.gov.hmcts.reform.professionalapi.service.impl;

import feign.FeignException;
import feign.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.JurisdictionFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.controller.request.JurisdictionUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.JurisdictionService;

@Service
@Slf4j
public class JurisdictionServiceImpl implements JurisdictionService {

    @Autowired
    private JurisdictionFeignClient jurisdictionFeignClient;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Override
    public void propagateJurisdictionIdsForSuperUserToCcd(ProfessionalUser user, String userId) {

        JurisdictionUserCreationRequest request = createJurisdictionUserProfileRequestForSuperUser(user);
        callCcd(request, userId);
    }

    @Override
    public void propagateJurisdictionIdsForNewUserToCcd(List<Jurisdiction> jurisdictions, String userId, String email) {
        JurisdictionUserCreationRequest request = new JurisdictionUserCreationRequest(email, jurisdictions);
        callCcd(request, userId);
    }

    public JurisdictionUserCreationRequest createJurisdictionUserProfileRequestForSuperUser(ProfessionalUser user) {
        List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
        user.getUserAttributes().forEach(userAttribute -> {
            if (userAttribute.getPrdEnum().getPrdEnumId().getEnumType().equalsIgnoreCase("JURISD_ID")) {
                Jurisdiction jurisdiction = new Jurisdiction();
                jurisdiction.setId(userAttribute.getPrdEnum().getEnumName());
                jurisdictions.add(jurisdiction);
            }
        });
        return new JurisdictionUserCreationRequest(user.getEmailAddress(), jurisdictions);
    }

    public void callCcd(JurisdictionUserCreationRequest request, String userId) {
        String s2sToken = authTokenGenerator.generate();
        try (Response response = jurisdictionFeignClient.createJurisdictionUserProfile(userId, s2sToken, request)) {
            log.info("Jurisdiction create user profile success!!");
        } catch (FeignException ex) {
            String errorMessage = "Jurisdiction create user profile failed with status code: " + ex.status() + "reason: " + ex.getMessage();
            log.error(errorMessage, ex);
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), errorMessage);
        }
    }
}
