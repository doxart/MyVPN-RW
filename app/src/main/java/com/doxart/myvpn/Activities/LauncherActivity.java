package com.doxart.myvpn.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.doxart.myvpn.App.VPNHandler;
import com.doxart.myvpn.BuildConfig;
import com.doxart.myvpn.Interfaces.OnAnswerListener;
import com.doxart.myvpn.Interfaces.OnVPNServersLoadedListener;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityLauncherBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;

public class LauncherActivity extends AppCompatActivity {

    private final String TAG = "LAUNCHER_PROCESS";
    private ActivityLauncherBinding b;

    private SharePrefs sharePrefs;
    private Dialog connectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inflate();
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (Utils.checkConnection(LauncherActivity.this)) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
                inflate();
            } else showConnectionDialog();
        }
    });

    private void showConnectionDialog() {
        connectionDialog = Utils.askQuestion(this, getString(R.string.no_connection), getString(R.string.no_connection_detail), getString(R.string.go_network_settings),
                getString(R.string.try_again), getString(R.string.exit), null, false, new OnAnswerListener() {
                    @Override
                    public void onPositive() {
                        resultLauncher.launch(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    @Override
                    public void onNegative() {
                        inflate();
                    }

                    @Override
                    public void onOther() {
                        System.exit(1);
                    }
                });

        connectionDialog.show();
    }

    private void inflate() {
        sharePrefs = new SharePrefs(this);

        b.adjustingTxt.setText(getString(R.string.launch_phase_0));
        if (!Utils.checkConnection(this)) {
            showConnectionDialog();
            return;
        }

        b.adjustingTxt.setText(getString(R.string.launch_phase_1));

        checkPremium();
    }

    private void checkPremium() {
        b.adjustingTxt.setText(getString(R.string.launch_phase_4));
        Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                EntitlementInfo entitlementInfo = customerInfo.getEntitlements().get("premium");

                boolean isPremium = false;

                if (entitlementInfo != null) isPremium = entitlementInfo.isActive();

                sharePrefs.putBoolean("premium", isPremium);

                getConfig();
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                getConfig();
            }
        });
    }

    private void getConfig() {
        b.adjustingTxt.setText(getString(R.string.launch_phase_2));
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 0 : 3600)
                .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sharePrefs.putBoolean("showPaywall", mFirebaseRemoteConfig.getBoolean("showPaywall"));
                sharePrefs.putBoolean("showSpecialPaywall", mFirebaseRemoteConfig.getBoolean("showSpecialPaywall"));

                sharePrefs.putBoolean("showIAds", mFirebaseRemoteConfig.getBoolean("showIAds"));
                sharePrefs.putBoolean("showBannerAds", mFirebaseRemoteConfig.getBoolean("showBannerAds"));

                sharePrefs.putBoolean("fetchVPNGate", mFirebaseRemoteConfig.getBoolean("fetchVPNGate"));

                sharePrefs.putLong("paywallDelay", mFirebaseRemoteConfig.getLong("paywallDelay"));

                sharePrefs.putInt("paywallDiscountItem", ((Long)mFirebaseRemoteConfig.getLong("paywallDiscountItem")).intValue());
                sharePrefs.putInt("paywallItem", ((Long)mFirebaseRemoteConfig.getLong("paywallItem")).intValue());
            }

            b.adjustingTxt.setText(getString(R.string.launch_phase_3));
            if (SharePrefs.getInstance(this).getBoolean("fetchVPNGate")) {
                VPNHandler.getServersFromVPNGate(LauncherActivity.this, new OnVPNServersLoadedListener() {
                    @Override
                    public void onLoaded() {
                        b.adjustingTxt.setText(getString(R.string.launch_phase_5));
                        openApp();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        b.adjustingTxt.setText(getString(R.string.launch_phase_5));
                        openApp();
                    }
                });
            } else openApp();
        });
    }

    private void openApp() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }
}