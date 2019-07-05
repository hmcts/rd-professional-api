package uk.gov.hmcts.reform.professionalapi.sort;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface ProfessionalApiSort {

    /**
     * Sorts Professional user list with Created date i.e most oldest professional user will appear first
     * @param organisation organisation for which it users need to be sorted
     * @return List of ProfessionalUser
     */
    static List<ProfessionalUser> sortUserListByCreatedDate(Organisation organisation) {
        return organisation.getUsers().stream().sorted((Comparator.comparing(ProfessionalUser::getCreated))).collect(Collectors.toList());
    }

    static List<ProfessionalUser> sortUsers(Organisation organisation) {
        List<ProfessionalUser> userList = organisation.getUsers();

        sortUserListerByDeletedTimestamp(userList);
        return sortUserListByOrgAdmin(userList);
    }

    static List<ProfessionalUser> sortUserListerByDeletedTimestamp(List<ProfessionalUser> userList) {
        userList.sort(nullsFirst(comparing(ProfessionalUser::getDeleted)));
        return userList;
    }

    static List<ProfessionalUser> sortUserListByOrgAdmin(List<ProfessionalUser> userList) {
        List<ProfessionalUser> orgAdminSortedList = new ArrayList<>();

        userList.forEach(user -> {
            user.getUserAttributes().forEach(userAttribute -> {
                if(userAttribute.getPrdEnum().getEnumName() == "ORGANISATION_ADMIN") {
                    orgAdminSortedList.add(user);
                }
            });
        });

        userList.forEach(user -> {
            if (!orgAdminSortedList.contains(user)) {
                orgAdminSortedList.add(user);
            }
        });

        return orgAdminSortedList;
    }
}
