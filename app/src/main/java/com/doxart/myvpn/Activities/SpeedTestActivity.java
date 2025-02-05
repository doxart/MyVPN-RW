package com.doxart.myvpn.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.AdUtils;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivitySpeedTestBinding;
import com.ekn.gruzer.gaugelibrary.Range;

import java.text.DecimalFormat;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedTestActivity extends AppCompatActivity implements ISpeedTestListener {

    ActivitySpeedTestBinding b;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivitySpeedTestBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + Utils.dpToPx(15, this));
                return insets;
            });
        }

        init();
    }

    private void init() {
        setupGauge();

        b.closeBT.setOnClickListener(v -> onBackPressed());

        if (!SharePrefs.getInstance(this).isPremium()) {
            b.adContainer.addView(AdUtils.getBannerAdView());
        }

        b.startTestBT.setOnClickListener(v -> {
            b.startTestBT.setVisibility(View.GONE);
            new Thread(this::getNetSpeed).start();
        });
    }

    private void setupGauge() {
        Range range = new Range();
        range.setColor(ContextCompat.getColor(this, R.color.red));
        range.setFrom(0d);
        range.setTo(50d);

        Range range1 = new Range();
        range1.setColor(ContextCompat.getColor(this, R.color.orange));
        range1.setFrom(50d);
        range1.setTo(100d);

        Range range2 = new Range();
        range2.setColor(ContextCompat.getColor(this, R.color.green));
        range2.setFrom(100d);
        range2.setTo(150d);

        b.speedGauge.addRange(range);
        b.speedGauge.addRange(range1);
        b.speedGauge.addRange(range2);

        b.speedGauge.setMinValue(0d);
        b.speedGauge.setMaxValue(150d);
        b.speedGauge.setValue(0d);

        b.speedGauge.setValueColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    int test = 0;
    private void getNetSpeed() {
        test = 99;
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        b.speedGauge.setValueColor(ContextCompat.getColor(this, R.color.colorWhite));

        startTime = System.currentTimeMillis();
        speedTestSocket.addSpeedTestListener(this);
        speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/50M.iso", 100);
    }

    @Override
    public void onCompletion(SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;

        runOnUiThread(() -> {
            b.speedGauge.setValue(Math.floor(r));

            b.startTestBT.setVisibility(View.VISIBLE);
            b.speedGauge.setValueColor(ContextCompat.getColor(this, android.R.color.transparent));

            b.speedTxt.setText(String.format("%s MB/s", new DecimalFormat("##").format(r)));
            b.latencyTxt.setText(String.format("%s ms", (System.currentTimeMillis() - startTime) / 600));
            b.startTestBT.setText(getString(R.string.start));
        });
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("showAD", true);
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    public void onProgress(float percent, SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;

        runOnUiThread(() -> b.speedGauge.setValue(Math.floor(r)));
    }

    @Override
    public void onError(SpeedTestError speedTestError, String errorMessage) {
    }

    @Override
    protected void onDestroy() {
        AdUtils.loadBannerAd(this);
        super.onDestroy();
    }
}