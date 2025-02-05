package com.doxart.myvpn.Util;

import android.content.Context;

import com.unity3d.mediation.LevelPlayAdSize;
import com.unity3d.mediation.banner.LevelPlayBannerAdView;

public class AdUtils {
    public static final int SHOW_AD = 99991453;
    private static LevelPlayBannerAdView bannerAdView;
    private static LevelPlayBannerAdView bannerSmallAdView;

    public static LevelPlayBannerAdView getBannerAdView() {
        return bannerAdView;
    }

    public static LevelPlayBannerAdView getBannerSmallAdView() {
        return bannerSmallAdView;
    }

    public static void loadBannerAd(Context context) {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        bannerAdView = new LevelPlayBannerAdView(context, Config.BANNER_AD_ID);
        bannerAdView.setAdSize(LevelPlayAdSize.MEDIUM_RECTANGLE);
        bannerAdView.loadAd();
    }

    public static void loadSmallBannerAd(Context context) {
        if (bannerSmallAdView != null) {
            bannerSmallAdView.destroy();
        }
        bannerSmallAdView = new LevelPlayBannerAdView(context, Config.BANNER_AD_ID);
        bannerSmallAdView.setAdSize(LevelPlayAdSize.BANNER);
        bannerSmallAdView.loadAd();
    }
}
