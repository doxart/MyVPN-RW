package com.doxart.myvpn.App;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doxart.myvpn.Interfaces.OnVPNServersLoadedListener;
import com.doxart.myvpn.Model.VPNModel;

import java.util.ArrayList;
import java.util.List;

public class VPNHandler {
    public static final String VPN_CONNECTED = "CONNECTED";
    public static final String VPN_DISCONNECTED = "DISCONNECTED";
    public static final String VPN_CONNECTING = "WAIT";

    public static VPNModel VPN;

    public static VPNModel getVPN() {
        return VPN;
    }

    public static void setVPN(VPNModel VPN) {
        VPNHandler.VPN = VPN;
    }

    public static boolean CONNECTED = false;
    public static String VPN_STATUS = VPN_DISCONNECTED;

    public static void setVpnStatus(String vpnStatus) {
        VPN_STATUS = vpnStatus;
    }

    public static boolean isCONNECTED() {
        return CONNECTED;
    }

    public static void setCONNECTED(boolean CONNECTED) {
        VPNHandler.CONNECTED = CONNECTED;
    }

    public static List<String> countryList = new ArrayList<>();
    public static List<VPNModel> vpnList = new ArrayList<>();

    public static void getServersFromVPNGate(Context context, OnVPNServersLoadedListener listener) {
        String apiUrl = "https://www.vpngate.net/api/iphone/";

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                response -> parseCSV(response, listener), listener::onError);

        queue.add(stringRequest);
    }

    private static void parseCSV(String csvData, OnVPNServersLoadedListener listener) {
        String[] lines = csvData.split("\n");
        VPNModel minPing = null;

        for (String line : lines) {
            String[] columns = line.split(",");

            if (columns.length > 7 & !columns[0].startsWith("#")) {
                int ping = 999;

                try {
                    ping = Integer.parseInt(columns[3]);
                } catch (NumberFormatException e) {
                    Log.d("ParseCSV", "parseCSV: " + e.getMessage());
                }

                VPNModel vpnModel = new VPNModel(columns[0], columns[1], ping, columns[5], columns[6]);
                vpnModel.setOvpnConfig(columns[columns.length - 1]);
                vpnModel.setOvpnUsername("vpn");
                vpnModel.setOvpnPassword("vpn");

                if (minPing == null) minPing = vpnModel;

                if (ping < minPing.getPing()) minPing = vpnModel;

                vpnList.add(vpnModel);
            }
        }

        VPNHandler.setVPN(minPing);

        listener.onLoaded();
    }

    public static List<String> getCountryList() {
        return countryList;
    }

    public static void setCountryList(List<String> countryList) {
        VPNHandler.countryList = countryList;
    }

    public static List<VPNModel> getVpnList() {
        return vpnList;
    }

    public static void setVpnList(List<VPNModel> vpnList) {
        VPNHandler.vpnList = vpnList;
    }
}
