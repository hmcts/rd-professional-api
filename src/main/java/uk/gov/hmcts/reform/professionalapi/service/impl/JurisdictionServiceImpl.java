package uk.gov.hmcts.reform.professionalapi.service.impl;

import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.throwException;

import feign.FeignException;
import feign.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
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
        List<Jurisdiction> jurisdictions = new ArrayList<>();
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
        int responseCode = 500;
        String s2sToken = authTokenGenerator.generate();
        try (Response response = jurisdictionFeignClient.createJurisdictionUserProfile(userId, s2sToken, request)) {
            if (response == null) {
                log.warn("Response returned null while CCD call");
                throwException(responseCode);
            } else if (response.status() > 300) {
                responseCode = response.status();
                throwException(responseCode);
            }
        } catch (FeignException ex) {
            log.warn("Feign exception while CCD call");
            throwException(ex.status() < 0 ? responseCode : ex.status());
        }
    }
}
