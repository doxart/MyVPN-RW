package com.doxart.myvpn.Interfaces;

import com.android.volley.VolleyError;

public interface OnVPNServersLoadedListener {
    void onLoaded();
    void onError(VolleyError error);
}
