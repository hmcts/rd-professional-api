package uk.gov.hmcts.reform.professionalapi.sort;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface ProfessionalApiSort {

    static List<ProfessionalUser> sortUsers(Organisation organisation) {
        List<ProfessionalUser> userList = organisation.getUsers();

        userList = sortUserListByDeletedTimestamp(userList);
        return sortUserListByOrgAdminsFirst(userList);
    }

    static List<ProfessionalUser> sortUserListByDeletedTimestamp(List<ProfessionalUser> userList) {
        userList.sort(nullsFirst(
                comparing(ProfessionalUser::getDeleted, nullsFirst(naturalOrder()))));

        return userList;
    }

    static List<ProfessionalUser> sortUserListByOrgAdminsFirst(List<ProfessionalUser> userList) {
        List<ProfessionalUser> adminList = new ArrayList<>();

        userList.forEach(user -> {
            user.getUserAttributes().forEach(userAttribute -> {
                if (user.getDeleted() == null && userAttribute.getPrdEnum().getEnumName() == "organisation-admin") {
                    adminList.add(user);
                }
            });
        });

        List<ProfessionalUser> sortedAdminList = adminList.stream().sorted((Comparator.comparing(ProfessionalUser::getCreated))).collect(Collectors.toList());

        userList.forEach(user -> {
            if (!sortedAdminList.contains(user)) {
                sortedAdminList.add(user);
            }
        });

        return sortedAdminList;
    }
}