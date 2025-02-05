package com.doxart.myvpn.Activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.doxart.myvpn.App.VPNHandler;
import com.doxart.myvpn.Fragments.ServersFragment;
import com.doxart.myvpn.R;
import com.doxart.myvpn.RetroFit.GetIPDataService;
import com.doxart.myvpn.RetroFit.MyIP;
import com.doxart.myvpn.RetroFit.RetrofitClient;
import com.doxart.myvpn.Util.AdUtils;
import com.doxart.myvpn.Util.Config;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.Util.VPNCountdownTimer;
import com.doxart.myvpn.databinding.ActivityMainBinding;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayAdInfo;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN_ACTIVITY";
    private ActivityMainBinding b;
    private SharePrefs sharePrefs;

    private LevelPlayInterstitialAd mConnectAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), o -> {});

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        if (o.getData() != null) {
            if (!SharePrefs.getInstance(MainActivity.this).getBoolean("premium") &
                    o.getData().getBooleanExtra("showADFromPaywall", false)) {
                //TODO SHOW İNTERSTİTAİL
            }
        }

        if (o.getResultCode() == AdUtils.SHOW_AD) showInterstitialAd();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    });

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        if (o != null) {
            if (o.getResultCode() == RESULT_OK) startVpn();
            else Toast.makeText(this, "For connect please allow.", Toast.LENGTH_SHORT).show();
        }
    });

    private void inflate() {
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + Utils.dpToPx(15, this));
                return insets;
            });
        }

        sharePrefs = new SharePrefs(this);
        boolean premium = SharePrefs.getInstance(this).getBoolean("premium");

        if (!premium) {
            if (sharePrefs.getBoolean("showPaywall"))
                activityLauncher.launch(new Intent(this, PaywallActivity.class).putExtra("timer", (long) sharePrefs.getLong("paywallDelay")));
                //IronSource.launchTestSuite(getApplicationContext());
        } else b.appbar.premiumBT.setVisibility(View.GONE);

        if (getIntent().getBooleanExtra("openPlay", false)) Utils.openAppInPlayStore(this);

        AdUtils.loadBannerAd(this);
        AdUtils.loadSmallBannerAd(this);
        getConnectInterstitial();
        getInterstitial();
        getIPLocation();
        init();
    }

    private void getIPLocation() {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call;

        call = service.getMyIP();

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                MyIP myIP = response.body();

                if (myIP != null) {
                    b.myIpTxt.setText(myIP.getQuery());

                    b.mapView.getController().animateTo(new GeoPoint(myIP.getLat(), myIP.getLon()));
                    b.mapView.getController().setZoom(15f);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void init() {
        OnlineTileSourceBase darkTileSource = new OnlineTileSourceBase(
                "CartoDB_DarkMatter",
                0,
                19,
                256,
                ".png",
                new String[]{"https://a.basemaps.cartocdn.com/dark_all/"}
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                        MapTileIndex.getX(pMapTileIndex) + "/" +
                        MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding;
            }
        };

        b.mapView.setTileSource(darkTileSource);

        b.mapView.setOnTouchListener((v, event) -> true);

        pulseView(b.pulseView);

        b.safityShimmer.startShimmerAnimation();

        b.countryLay.cCountryTxt.setSelected(true);

        b.appbar.premiumBT.setOnClickListener(v -> activityLauncher.launch(new Intent(this, PaywallActivity.class).putExtra("timer", 0)));
        b.appbar.shareBT.setOnClickListener(v -> Utils.shareApp(this));

        b.vpnBtn.setOnClickListener(v -> showConnectAd());

        b.speedTestLay.setOnClickListener(v -> startActivity(new Intent(this, SpeedTestActivity.class)));
        b.locationLay.setOnClickListener(v -> startActivity(new Intent(this, LocationActivity.class)));

        b.countryLay.getRoot().setOnClickListener(v -> {
            if (VPNHandler.isCONNECTED()) Toast.makeText(this, this.getString(R.string.vpn_is_running), Toast.LENGTH_SHORT).show();
            else getServerListAsFragment();
        });

        updateStatus(OpenVPNService.getStatus());
        VpnStatus.initLogCache(this.getCacheDir());

        updateCurrentServerLay();
    }

    private void getServerListAsFragment() {
        ServersFragment serversFragment = new ServersFragment();
        serversFragment.setOnVPNServerSelectedListener(model -> {
            VPNHandler.setVPN(model);
            serversFragment.dismiss();
            updateCurrentServerLay();

            vpnBtnOnClickListener();
        });

        serversFragment.show(getSupportFragmentManager(), "ServersFragment");
    }

    private void vpnBtnOnClickListener() {
        if (VPNHandler.isCONNECTED()) {
            stopVpn();
        } else {
            prepareVpn();
        }
    }

    private void prepareVpn() {
        if (!VPNHandler.isCONNECTED()) {
            if (Utils.checkConnection(this)) {
                updateStatus("connecting");

                Intent intent = VpnService.prepare(this);

                if (intent != null) resultLauncher.launch(intent);
                else startVpn();
            }
        }
    }

    public void stopVpn() {
        try {
            OpenVPNThread.stop();

            updateStatus("connect");

            VPNHandler.setCONNECTED(false);
        } catch (Exception e) {
            Log.d(TAG, "stopVpn: " + e.getMessage());
        }

    }

    private void startVpn() {
        if (VPNHandler.getVPN() != null) {
            File file = new File(this.getFilesDir().toString() + "/vpn_config.ovpn");

            try {
                InputStream conf;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    conf = Files.newInputStream(file.toPath());
                } else conf = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(conf);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder config = new StringBuilder();
                String line;

                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    config.append(line).append("\n");
                }

                br.readLine();

                OpenVpnApi.startVpn(this, config.toString(), VPNHandler.getVPN().getCountry(), VPNHandler.getVPN().getOvpnUsername(), VPNHandler.getVPN().getOvpnPassword());
                Log.d(TAG, "startVpn: " + VPNHandler.getVPN().getIp() + " : " + VPNHandler.getVPN().getCountry() + " " + VPNHandler.getVPN().getOvpnUsername() + " " + VPNHandler.getVPN().getOvpnPassword());

                b.vpnStatus.setText(this.getString(R.string.starting));
                VPNHandler.setCONNECTED(true);
            } catch (IOException | RemoteException e) {
                Log.d(TAG, "startVpn: " + e);
                updateStatus("connect");
            }
        }
    }

    private void updateStatus(String connectionState) {
        if (connectionState == null) return;
        Log.d(TAG, "updateStatus: " + connectionState);
        switch (connectionState) {
            case VPNHandler.VPN_DISCONNECTED:
                VPNHandler.setCONNECTED(false);

                OpenVPNService.setDefaultStatus();

                ((GradientDrawable) b.countryLay.getRoot().getBackground()).setStroke(2, ContextCompat.getColor(this, R.color.primary));

                b.pulseView.setVisibility(View.INVISIBLE);
                b.pulseView.setBackgroundColor(ContextCompat.getColor(this, R.color.white_alpha));

                b.vpnBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                b.vpnBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));

                b.vpnStatus.setText(this.getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));

                b.durationTxt.setText("");

                b.safityTxt.setText(this.getString(R.string.your_net_not_safe));

                b.safityIcon.setBackgroundColor(ContextCompat.getColor(this, R.color.red));

                b.countryLay.openCListBT.setImageResource(R.drawable.ic_keyboard_arrow_down_24);

                this.stopService(new Intent(this, VPNCountdownTimer.class));
                goConnectionReport(false);
                break;

            case VPNHandler.VPN_CONNECTING:
                ((GradientDrawable) b.countryLay.getRoot().getBackground()).setStroke(2, ContextCompat.getColor(this, R.color.orange));

                b.pulseView.setVisibility(View.VISIBLE);
                b.pulseView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_alpha));

                b.vpnBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange)));

                b.vpnStatus.setText(this.getString(R.string.connecting));
                b.vpnStatus.setTextColor(ContextCompat.getColor(this, R.color.orange));

                b.countryLay.openCListBT.setImageResource(R.drawable.ic_lock_24);

                break;

            case VPNHandler.VPN_CONNECTED:
                VPNHandler.setCONNECTED(true);

                ((GradientDrawable) b.countryLay.getRoot().getBackground()).setStroke(3, ContextCompat.getColor(this, R.color.green));

                b.pulseView.setVisibility(View.VISIBLE);
                b.pulseView.setBackgroundColor(ContextCompat.getColor(this, R.color.green_alpha));

                b.vpnBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                b.vpnBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green)));

                b.vpnStatus.setText(this.getString(R.string.connected));
                b.vpnStatus.setTextColor(ContextCompat.getColor(this, R.color.green));

                b.safityTxt.setText(this.getString(R.string.you_are_safe));

                b.safityIcon.setBackgroundColor(ContextCompat.getColor(this, R.color.green));

                b.countryLay.openCListBT.setImageResource(R.drawable.ic_lock_24);

                this.startService(new Intent(this, VPNCountdownTimer.class));
                goConnectionReport(true);
                break;

        }
    }

    private void goConnectionReport(boolean isConnection) {
        getIPLocation();
        Intent i = new Intent(this, ConnectionReportActivity.class);
        i.putExtra("isConnection", isConnection);
        i.putExtra("sessionM", m);
        i.putExtra("sessionS", s);
        startActivity(i);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String state = intent.getStringExtra("state");
                if (state != null) {
                    VPNHandler.setVpnStatus(state);
                    updateStatus(state);
                }
            } catch (Exception e) {
                Log.d(TAG, "onReceive: " + e.getMessage());
            }

            if (Objects.requireNonNull(intent.getAction()).equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    int m = 0;
    int s = 0;
    public void updateConnectionStatus(int mm, int ss) {
        int totalSeconds = mm * 60 + ss;
        m = totalSeconds / 60;
        s = totalSeconds % 60;
        String formattedTime = String.format(Locale.getDefault(), "%02d.%02d", m, s);
        if (VPNHandler.isCONNECTED()) {
            b.durationTxt.setText(formattedTime);
        }
    }

    public void updateCurrentServerLay() {
        if (isFinishing()) return;
        if (VPNHandler.getVPN() != null) {
            if (VPNHandler.getVPN().getCountryId() != null) Glide.with(this).load("https://flagcdn.com/h80/" + VPNHandler.getVPN().getCountryId().toLowerCase(Locale.US) + ".png").centerCrop().into(b.countryLay.cFlagImg);
            b.countryLay.cCountryTxt.setText(VPNHandler.getVPN().getCountry());

            Utils.setSignalView(this, b.countryLay.s1, b.countryLay.s2, b.countryLay.s3, VPNHandler.getVPN().getPing());
        }
    }

    private void pulseView(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", .25f, 2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", .25f, 2f);

        scaleX.setDuration(2500);
        scaleY.setDuration(2500);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);

        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    private void getInterstitial() {
        LevelPlayInterstitialAd interstitialAd = new LevelPlayInterstitialAd(Config.INTERSTITIAL_AD_ID);
        interstitialAd.loadAd();
    }
    private void getConnectInterstitial() {
        mConnectAd = new LevelPlayInterstitialAd(Config.INTERSTITIAL_AD_ID);
        mConnectAd.setListener(new LevelPlayInterstitialAdListener() {
            @Override
            public void onAdLoaded(@NonNull LevelPlayAdInfo levelPlayAdInfo) {

            }

            @Override
            public void onAdLoadFailed(@NonNull LevelPlayAdError levelPlayAdError) {
                Log.d(TAG, "onAdLoadFailed: " + levelPlayAdError.getErrorMessage());
            }

            @Override
            public void onAdDisplayed(@NonNull LevelPlayAdInfo levelPlayAdInfo) {

            }

            @Override
            public void onAdClosed(@NonNull LevelPlayAdInfo levelPlayAdInfo) {
                LevelPlayInterstitialAdListener.super.onAdClosed(levelPlayAdInfo);
                mConnectAd.loadAd();
                vpnBtnOnClickListener();
            }

            @Override
            public void onAdDisplayFailed(@NonNull LevelPlayAdError levelPlayAdError, @NonNull LevelPlayAdInfo levelPlayAdInfo) {
                LevelPlayInterstitialAdListener.super.onAdDisplayFailed(levelPlayAdError, levelPlayAdInfo);
                Log.d(TAG, "onAdLoadFailed: " + levelPlayAdError.getErrorMessage());
                mConnectAd.loadAd();
                vpnBtnOnClickListener();
            }
        });
        mConnectAd.loadAd();
    }

    public void showConnectAd() {
        if (mConnectAd.isAdReady() & !SharePrefs.getInstance(this).isPremium()) {
            mConnectAd.showAd(this, "Home_Screen");
        } else vpnBtnOnClickListener();
    }

    public void showInterstitialAd() {
        if (mConnectAd.isAdReady() & !SharePrefs.getInstance(this).isPremium()) {
            mConnectAd.showAd(this, "Home_Screen");
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        b.mapView.onResume();
        if (!sharePrefs.getBoolean("premium")) {
            //TODO SHOW BANNER
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("usage_data_updated"));

        if (sharePrefs.getBoolean("premium")) b.appbar.premiumBT.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        b.mapView.onPause();
    }
}