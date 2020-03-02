package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;

@Slf4j
public class UserCreationRequestValidator {

    private UserCreationRequestValidator() {
    }


    public static List<String> validateRoles(List<String> roles, List<PrdEnum> prdEnumList) {

        List<String> rolesWithoutDuplicates = roles.stream()
                .distinct()
                .collect(Collectors.toList());
        List<String> verifiedUserRoles = rolesWithoutDuplicates.stream()
                .map(role -> verifyRole(role, prdEnumList))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(verifiedUserRoles) || verifiedUserRoles.contains("false")) {
            log.error("Invalid/No user role(s) provided");
            throw new InvalidRequest("Invalid roles provided");
        }

        return verifiedUserRoles;
    }

    public static String verifyRole(String amendedRole, List<PrdEnum> prdEnumList) {
        AtomicReference<String> verifiedRole = new AtomicReference<>("false");
        prdEnumList.forEach(prdEnum -> {
            if (!StringUtils.isEmpty(amendedRole) && prdEnum.getEnumName().equals(amendedRole.toLowerCase().trim())) {
                verifiedRole.set(amendedRole.toLowerCase().trim());
            }
        });
        return verifiedRole.get();
    }
}
