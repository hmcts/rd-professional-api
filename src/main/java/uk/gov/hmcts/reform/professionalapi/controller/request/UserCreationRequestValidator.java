package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;

public class UserCreationRequestValidator {

    private UserCreationRequestValidator(){
    }

    static List<String> verifiedUserRoles;

    public static List<String> contains(List<String> roles, List<PrdEnum> prdEnumList) {
        List<String> amendedRoles = roles.stream().map(role -> role.replace("-", "_").toUpperCase()).collect(Collectors.toList());

        verifiedUserRoles = amendedRoles.stream().map(role -> verifyRole(role, prdEnumList)).collect(Collectors.toList());

        List<String> finalList = verifiedUserRoles.stream().filter(role -> !role.equals("false")).collect(Collectors.toList());

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

