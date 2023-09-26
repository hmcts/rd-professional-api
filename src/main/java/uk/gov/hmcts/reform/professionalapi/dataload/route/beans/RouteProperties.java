package uk.gov.hmcts.reform.professionalapi.dataload.route.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;


@Setter
@Getter
@NoArgsConstructor
public class RouteProperties {

    String startRoute;

    String routeName;

    String childNames;

    String sql;

    Optional<String> updateSql;

    String deleteSql;

    String truncateSql;

    String blobPath;

    String processor;

    String mapper;

    String binder;

    String fileName;

    String tableName;

    String deferredSql;

    String csvHeadersExpected;

    String isHeaderValidationEnabled;

    String parentFileName;

    boolean parentFailureEnabled;
}
