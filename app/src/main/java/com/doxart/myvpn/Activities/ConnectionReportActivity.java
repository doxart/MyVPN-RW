package com.doxart.myvpn.Activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.doxart.myvpn.App.VPNHandler;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.AdUtils;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityConnectionReportBinding;
import com.ironsource.mediationsdk.IronSource;

import java.util.Locale;

public class ConnectionReportActivity extends AppCompatActivity {

    private final String TAG = "CRA_REPORT";

    private ActivityConnectionReportBinding b;

    private boolean isConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isConnection = getIntent().getBooleanExtra("isConnection", true);

        inflate();
    }

    private void inflate() {
        b = ActivityConnectionReportBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + Utils.dpToPx(15, this));
                return insets;
            });
        }

        setupView();
        init();
    }

    private void setupView() {
        if (VPNHandler.getVPN() != null) {
            if (isConnection) b.connectionTypeTxt.setText(getString(R.string.connection_successful));
            else {
                b.connectionTypeTxt.setText(getString(R.string.connection_report));
                updateConnectionStatus(getIntent().getIntExtra("sessionM", 0), getIntent().getIntExtra("sessionS", 0));
            }

            Glide.with(this).load("https://flagcdn.com/h80/" + VPNHandler.getVPN().getCountryId().toLowerCase(Locale.US) + ".png").centerCrop().into(b.serverFlag);
            b.serverIp.setText(VPNHandler.getVPN().getIp());
            b.serverName.setText(VPNHandler.getVPN().getCountry());
        } else finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        if (!SharePrefs.getInstance(this).isPremium()) {
            b.adContainer.addView(AdUtils.getBannerAdView());
        }

        b.closeBT.setOnClickListener(v -> onBackPressed());

        b.shareBT.setOnClickListener(v -> Utils.shareApp(this));
        b.ratingView.setOnTouchListener((v, event) -> {
            Utils.openAppInPlayStore(ConnectionReportActivity.this);
            return false;
        });

        b.reportBT.setOnClickListener(v -> Utils.openDoxartForReport(this));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            if (intent.getAction().equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    private void updateConnectionStatus(int m, int s) {
        int totalSeconds = m * 60 + s;
        String formattedTime = String.format(Locale.getDefault(), "%02d s", totalSeconds % 60);
        b.serverTime.setText(formattedTime);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("usage_data_updated"));
        IronSource.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        IronSource.onPause(this);
    }

    @Override
    protected void onDestroy() {
        AdUtils.loadBannerAd(this);
        super.onDestroy();
    }
}