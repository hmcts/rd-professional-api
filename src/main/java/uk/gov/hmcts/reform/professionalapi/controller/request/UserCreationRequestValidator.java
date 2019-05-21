package uk.gov.hmcts.reform.professionalapi.controller.request;

import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class UserCreationRequestValidator {

    static List<String> verifiedUserRoles;

    public static List<String> contains(List<String> roles, List<PrdEnum> prdEnumList) {

        List<String> amendedRoles = roles.stream().map(role -> role.replace("-", "_").toUpperCase()).collect(Collectors.toList());

        verifiedUserRoles = amendedRoles.stream().map(role -> verifyRole(role, prdEnumList)).collect(Collectors.toList());

        return verifiedUserRoles;
    }

    public static String verifyRole(String amendedRole, List<PrdEnum> prdEnumList){
        AtomicReference<String> verifiedRole = new AtomicReference<>("");

        prdEnumList.forEach(prdEnum -> {
            if(prdEnum.getEnumName().equals(amendedRole)){
                verifiedRole.set(amendedRole);

//                UserAttribute userAttribute = new UserAttribute(prdEnum);
//
//                PrdEnumId prdEnumId = prdEnum.getPrdEnumId();
//
//                prdEnumId.getEnumCode();
//
//                prdEnumId.getEnumType();
            }
        });

        return verifiedRole.get();

    }

//    public static List<String> verifyRole(List<String> amendedRoles, List<PrdEnum> prdEnumList){
//
//        List<String> verifiedList = new ArrayList<>();
//
//        amendedRoles.forEach(amendedRole ->
//                prdEnumList.stream()
//                        .filter(prdEnum -> amendedRole.contains(prdEnum.getEnumName())).forEach(verifiedList::add));
//
//        return verifiedList;
//
//    }
}

