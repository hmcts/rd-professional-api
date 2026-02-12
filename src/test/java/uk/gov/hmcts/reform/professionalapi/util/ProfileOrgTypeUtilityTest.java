package uk.gov.hmcts.reform.professionalapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ProfileOrgTypeUtilityTest {

    /* ------------------------------------------------------------------
     * orgType -> profileIds (FULL MATRIX)
     * ------------------------------------------------------------------ */
    public static Stream<Arguments> orgTypeToProfileIds() {
        return Stream.of(
                Arguments.of(
                        OrganisationTypeConstants.GOVT,
                        List.of(OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE)
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ORG,
                        List.of(OrganisationProfileIdConstants.ORGANISATION_PROFILE)
                ),
                Arguments.of(
                        OrganisationTypeConstants.BARRISTER,
                        List.of(
                                OrganisationProfileIdConstants.BARR_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.SOLICITOR_ORG,
                        List.of(
                                OrganisationProfileIdConstants.SOLICITOR_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.LOCAL_AUTHORITY_ORG,
                        List.of(
                                OrganisationProfileIdConstants.LOCALAUTH_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.PROBATE_PRACTITIONER,
                        List.of(
                                OrganisationProfileIdConstants.PROBPRAC_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),

                // GOVT variants
                Arguments.of(
                        OrganisationTypeConstants.GOVT_DWP_ORG,
                        List.of(
                                OrganisationProfileIdConstants.GOVT_DWP_PROFILE,
                                OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.GOVT_HO_ORG,
                        List.of(
                                OrganisationProfileIdConstants.GOVT_HO_PROFILE,
                                OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.GOVT_HMRC_ORG,
                        List.of(
                                OrganisationProfileIdConstants.GOVT_HMRC_PROFILE,
                                OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.GOVT_CICA_ORG,
                        List.of(
                                OrganisationProfileIdConstants.GOVT_CICA_PROFILE,
                                OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.GOVT_CAFCASS_CYMRU_ORG,
                        List.of(
                                OrganisationProfileIdConstants.GOVT_CAFCASS_CYMRU_PROFILE,
                                OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.GOVT_IBCA,
                        List.of(
                                OrganisationProfileIdConstants.GOVT_IBCA_PROFILE,
                                OrganisationProfileIdConstants.GOVERNMENT_ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ACCOUNT,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ACCOUNT_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_PUBDEF,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_PUBDEF_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ENV,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ENV_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_MARK,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_MARK_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_RETAIL,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_RETAIL_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_TRANSP,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_TRANSP_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ACCOM,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ACCOM_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_SOCIAL,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_SOCIAL_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_SCIENCE,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_SCIENCE_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ENERGY,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ENERGY_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ENGG,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ENGG_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_CHARITY,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_CHARITY_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_MINING,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_MINING_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_PUBADM,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_PUBADM_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_LAW,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_LAW_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_MEDIA,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_MEDIA_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_HOSP,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_HOSP_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_FIN,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_FIN_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_LEISURE,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_LEISURE_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_REC,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_REC_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_CREATI,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_CREATI_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_SALES,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_SALES_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ITCOMM,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ITCOMM_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_HEALTH,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_HEALTH_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_BUSI,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_BUSI_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_ADMIN,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_ADMIN_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_NFP,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_NFP_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_PROP,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_PROP_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_EDU,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_EDU_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                ),
                Arguments.of(
                        OrganisationTypeConstants.OTHER_REALT,
                        List.of(
                                OrganisationProfileIdConstants.OTHER_REALT_PROFILE,
                                OrganisationProfileIdConstants.ORGANISATION_PROFILE
                        )
                )
        );
    }

    @ParameterizedTest(name = "toProfileIds({0}) → {1}")
    @MethodSource("orgTypeToProfileIds")
    void toProfileIdsShouldReturnsExpectedProfiles(String orgType, List<String> expected) {
        List<String> actual = ProfileOrgTypeUtility.toProfileIds(orgType);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    /* ------------------------------------------------------------------
     * Edge cases
     * ------------------------------------------------------------------ */
    @Test
    void toProfileIdsShouldReturnsEmptyForNullOrUnknown() {
        assertTrue(ProfileOrgTypeUtility.toProfileIds(null).isEmpty());
        assertTrue(ProfileOrgTypeUtility.toProfileIds("UNKNOWN").isEmpty());
    }

    @Test
    void toOrgTypesShouldReturnsEmptyForNullOrUnknown() {
        assertTrue(ProfileOrgTypeUtility.toOrgTypes(null).isEmpty());
        assertTrue(ProfileOrgTypeUtility.toOrgTypes("UNKNOWN_PROFILE").isEmpty());
    }

    /* ------------------------------------------------------------------
     * profileId -> orgTypes (reverse mapping validation)
     * ------------------------------------------------------------------ */
    @SuppressWarnings("unchecked")
    static Stream<Arguments> profileIdToOrgTypes() {
        List<String> profiles = orgTypeToProfileIds()
                .flatMap(a -> ((List<String>) a.get()[1]).stream())
                .distinct().toList();

        return profiles.stream()
                .map(profile -> Arguments.of(profile, orgTypeToProfileIds()
                                    .filter(b -> ((
                                            List<String>)b.get()[1]).contains(profile))
                                    .map(c -> c.get()[0])
                                    .toList())
                );
    }

    @ParameterizedTest(name = "toOrgTypes({0}) contains {1}")
    @MethodSource("profileIdToOrgTypes")
    void toOrgTypesShouldContainsExpectedOrgType(String profileId, List<String> expectedOrgTypes) {
        List<String> actual = ProfileOrgTypeUtility.toOrgTypes(profileId);

        assertNotNull(actual);
        assertThat(actual).hasSize(expectedOrgTypes.size());

        assertAll("Expected Orgtype should be present",
                expectedOrgTypes.stream()
                        .map(expectedOrgType -> () ->
                                assertThat(actual).contains(expectedOrgType))
        );

    }

    /* ------------------------------------------------------------------
     * Constructor coverage
     * ------------------------------------------------------------------ */
    @Test
    void private_constructor_is_covered() throws Exception {
        Constructor<ProfileOrgTypeUtility> c =
                ProfileOrgTypeUtility.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }
}