package com.doxart.myvpn.Model;

public class VPNModel {
    private String hostName;
    private String ip;
    private int ping;
    private String country;
    private String countryId;
    private String ovpnConfig;
    private String ovpnUsername;
    private String ovpnPassword;

    public VPNModel() {}

    public VPNModel(String hostName, String ip, int ping, String country, String countryId) {
        this.hostName = hostName;
        this.ip = ip;
        this.ping = ping;
        this.country = country;
        this.countryId = countryId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
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

    public String getOvpnConfig() {
        return ovpnConfig;
    }

    public void setOvpnConfig(String ovpnConfig) {
        this.ovpnConfig = ovpnConfig;
    }

    public String getOvpnUsername() {
        return ovpnUsername;
    }

    public void setOvpnUsername(String ovpnUsername) {
        this.ovpnUsername = ovpnUsername;
    }

    public String getOvpnPassword() {
        return ovpnPassword;
    }

    public void setOvpnPassword(String ovpnPassword) {
        this.ovpnPassword = ovpnPassword;
    }
}
