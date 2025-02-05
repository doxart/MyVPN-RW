package com.doxart.myvpn.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.doxart.myvpn.R;
import com.doxart.myvpn.RetroFit.GetIPDataService;
import com.doxart.myvpn.RetroFit.MyIP;
import com.doxart.myvpn.RetroFit.RetrofitClient;
import com.doxart.myvpn.Util.AdUtils;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityLocationBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {

    ActivityLocationBinding b;

    private MyIP myIP;

    IMapController mapController;
    private final String TAG = "LOCATION_PROCESS";

    SharePrefs sharePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharePrefs = new SharePrefs(this);
        inflate();
    }

    private void inflate() {
        b = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + Utils.dpToPx(15, this));
                return insets;
            });
        }

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
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

        b.closeBT.setOnClickListener(v -> onBackPressed());
        b.mapView.setTileSource(darkTileSource);

        mapController = b.mapView.getController();

        b.mapView.setOnTouchListener((v, event) -> {
            b.getRoot().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        if (!sharePrefs.isPremium()) {
            b.adContainer.addView(AdUtils.getBannerSmallAdView());
        }

        b.refreshBT.setOnClickListener(v -> {
            first = false;
            getIPLocation(null);
        });

        getIPLocation(null);
    }

    boolean first = false;

    public void getIPLocation(String ip) {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call;

        if (ip == null) call = service.getMyIP();
        else call = service.getMyIP(ip);

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                myIP = response.body();
                if (!first) {
                    setIPLocation();
                    first = true;
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    public void setIPLocation() {
        if (myIP != null) {
            if (mapController != null & b != null) {
                mapController.animateTo(new GeoPoint(myIP.getLat(), myIP.getLon()));
                mapController.setZoom(10f);

                b.myIpTxt.setText(myIP.getQuery());

                DecimalFormat decimalFormat = new DecimalFormat("##.#######");

                b.latTxt.setText(String.format(getString(R.string.lat_d), decimalFormat.format(myIP.getLat())));
                b.lngTxt.setText(String.format(getString(R.string.lng_d), decimalFormat.format(myIP.getLon())));

                b.regionTxt.setText(myIP.getRegion());
                b.cityTxt.setText(myIP.getCity());
                b.countryTxt.setText(myIP.getCountry());
                b.ispTxt.setText(myIP.getIsp());
            }
        } else getIPLocation(null);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("showAD", true);
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        b.mapView.onResume();
        setIPLocation();
    }

    @Override
    public void onPause() {
        b.mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        AdUtils.loadSmallBannerAd(this);
        super.onDestroy();
    }
}