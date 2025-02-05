package com.doxart.myvpn.Util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class VPNCountdownTimer extends Service {
    private Handler handler;
    private Runnable runnable;
    private boolean isServiceRunning = false;
    private int usageMinutes, usageSeconds, inUsageSeconds = 0;
    private int oneMinute;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        oneMinute = 60;

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if (isServiceRunning) {
                    usageSeconds++;
                    inUsageSeconds++;

                    Intent intent = new Intent("usage_data_updated");
                    intent.putExtra("usageMinutes", usageMinutes);
                    intent.putExtra("usageSeconds", usageSeconds);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        handler.removeCallbacks(runnable);
    }
}
