package uk.gov.hmcts.reform.sysrefdataapi.domain.entities;

public class Country implements SystemRefData {

    private String id;
    private String countryName;

    public Country() {
        //noop
    }

    public Country(String id, String countryName) {
        this.id = id;
        this.countryName = countryName;
    }

    public String getId() {
        return id;
    }

    public String getCountryName() {
        return countryName;
    }
}
