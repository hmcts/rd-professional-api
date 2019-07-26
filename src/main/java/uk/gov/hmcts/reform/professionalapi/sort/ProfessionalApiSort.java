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
        List<ProfessionalUser> users = organisation.getUsers();

        users = sortUserListByDeletedTimestamp(users);
        return sortUserListByOrgAdminsFirst(users);
    }

    static List<ProfessionalUser> sortUserListByDeletedTimestamp(List<ProfessionalUser> users) {
        users.sort(nullsFirst(
                comparing(ProfessionalUser::getDeleted, nullsFirst(naturalOrder()))));

        return users;
    }

    static List<ProfessionalUser> sortUserListByOrgAdminsFirst(List<ProfessionalUser> users) {
        List<ProfessionalUser> admins = new ArrayList<>();

        users.forEach(user -> {
            if (user.getDeleted() == null) {
                user.getUserAttributes().forEach(userAttribute -> {
                    if (userAttribute.getPrdEnum().getEnumName().equals("organisation-admin")) {
                        admins.add(user);
                    }
                });
            }
        });

        List<ProfessionalUser> sortedAdmins = admins.stream()
                .sorted((Comparator.comparing(ProfessionalUser::getCreated)))
                .collect(Collectors.toList());

        users.forEach(user -> {
            if (!sortedAdmins.contains(user)) {
                sortedAdmins.add(user);
            }
        });

        return sortedAdmins;
    }
}