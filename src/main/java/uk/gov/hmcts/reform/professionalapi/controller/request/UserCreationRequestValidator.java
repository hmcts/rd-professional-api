package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;

@Slf4j
public class UserCreationRequestValidator {

    private UserCreationRequestValidator(){
    }

    static List<String> verifiedUserRoles;

    public static List<String> validateRoles(List<String> roles, List<PrdEnum> prdEnumList) {

        verifiedUserRoles = roles.stream().map(role -> verifyRole(role, prdEnumList)).collect(Collectors.toList());
        List<String> finalList = verifiedUserRoles.stream().filter(role -> !role.equals("false")).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(finalList)) {
            log.error("Invalid/No user role(s) provided");
            throw new InvalidRequest("Invalid roles provided");
        }
        return finalList;
    }

    public static String verifyRole(String amendedRole, List<PrdEnum> prdEnumList) {
        AtomicReference<String> verifiedRole = new AtomicReference<>("false");

        prdEnumList.forEach(prdEnum -> {
            if (prdEnum.getEnumName().equals(amendedRole)) {
                verifiedRole.set(amendedRole);
            }
        });
        return verifiedRole.get();
    }
}

