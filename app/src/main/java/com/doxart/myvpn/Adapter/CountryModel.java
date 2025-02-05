package com.doxart.myvpn.Adapter;

public class CountryModel {
    private String country;
    private String countryId;

    public CountryModel(String country, String countryId) {
        this.country = country;
        this.countryId = countryId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }
}
