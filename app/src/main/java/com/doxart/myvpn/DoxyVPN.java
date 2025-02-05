package com.doxart.myvpn;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.doxart.myvpn.Util.Config;
import com.doxart.myvpn.Util.SharePrefs;
import com.ironsource.mediationsdk.IronSource;
import com.revenuecat.purchases.LogLevel;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;

import java.util.List;


public class DoxyVPN extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        String uid = SharePrefs.getInstance(this).getUserID();

        Purchases.setLogLevel(LogLevel.DEBUG);
        Purchases.configure(new PurchasesConfiguration.Builder(this, Config.REVENUE_CAT_APP_ID).appUserID(uid).build());

        List<LevelPlay.AdFormat> legacyAdFormats = List.of(LevelPlay.AdFormat.BANNER, LevelPlay.AdFormat.INTERSTITIAL);

        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(Config.IRONSOURCE_AD_ID)
                .withLegacyAdFormats(legacyAdFormats)
                .withUserId(uid)
                .build();
        IronSource.setMetaData("is_test_suite", "enable");

        LevelPlayInitListener initListener = new LevelPlayInitListener() {
            @Override
            public void onInitFailed(@NonNull LevelPlayInitError error) {
            }
            @Override
            public void onInitSuccess(@NonNull LevelPlayConfiguration configuration) {

            }
        };

        LevelPlay.init(getApplicationContext(), initRequest, initListener);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
