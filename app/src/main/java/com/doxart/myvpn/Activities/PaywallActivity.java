package com.doxart.myvpn.Activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.doxart.myvpn.Adapter.PaywallItemAdapter;
import com.doxart.myvpn.Model.PaywallItemModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.IndicatorItemDecoration;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityPaywallBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.Offering;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.PurchaseParams;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreProduct;
import com.revenuecat.purchases.models.StoreTransaction;

import java.util.ArrayList;
import java.util.List;

public class PaywallActivity extends AppCompatActivity {

    private final String TAG = "APPTAG";
    private ActivityPaywallBinding b;

    private SharePrefs sharePrefs;

    private Package annualPackage, monthlyPackage, weeklyPackage;
    private Package discountedAnnualPackage, discountedMonthlyPackage, discountedWeeklyPackage;

    private boolean showedSpecial = false;
    private boolean canShowSpecial = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivityPaywallBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + Utils.dpToPx(10, this));
                return insets;
            });
        }

        sharePrefs = new SharePrefs(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        init();
    }

    private void init() {
        b.closeBT.setOnClickListener(v -> onBackPressed());

        List<PaywallItemModel> models = new ArrayList<>();
        models.add(new PaywallItemModel(R.drawable.paywall_img_1, "All servers free", "You can use all servers with premium!"));
        models.add(new PaywallItemModel(R.drawable.paywall_img_1, "No ads", "Adless usage of the app, no ads!"));
        models.add(new PaywallItemModel(R.drawable.paywall_img_1, "Limitless speed", "Get premium and use unlimited speed!"));

        PaywallItemAdapter paywallItemAdapter = new PaywallItemAdapter(this, models);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        b.recyclerView.setLayoutManager(manager);
        b.recyclerView.setHasFixedSize(true);
        b.recyclerView.addItemDecoration(new IndicatorItemDecoration());
        b.recyclerView.setAdapter(paywallItemAdapter);

        new LinearSnapHelper().attachToRecyclerView(b.recyclerView);

        b.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visiblePosition = manager.findLastCompletelyVisibleItemPosition();
                if (visiblePosition >= 0) {
                    PaywallItemModel model;
                    model = models.get(visiblePosition);

                    b.paywallTitle.setText(model.getTitle());
                    b.paywallTxt.setText(model.getTxt());
                }
            }
        });

        checkPremium();
        getOfferings();
    }

    private void checkPremium() {
        Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                EntitlementInfo entitlementInfo = customerInfo.getEntitlements().get("premium");

                boolean isPremium = false;

                if (entitlementInfo != null) isPremium = entitlementInfo.isActive();

                if (isPremium) {
                    sharePrefs.putBoolean("premium", true);
                    finish();
                    return;
                }
                canShowSpecial = customerInfo.getAllPurchasedProductIds().isEmpty();
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                Log.d(TAG, "onError: " + error.getMessage());
                canShowSpecial = false;
            }
        });
    }

    private void getOfferings() {
        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {
            @Override
            public void onReceived(@NonNull Offerings offerings) {
                Offering defaultOffer = offerings.get("premium");
                Offering discountedOffer = offerings.get("sale");
                if (defaultOffer != null) {
                    annualPackage = defaultOffer.getAnnual();

                    if (annualPackage != null) {
                        StoreProduct annual = annualPackage.getProduct();

                        if (sharePrefs.getInt("paywallItem") == 0) b.text2.setText(String.format(getString(R.string.yearly_sub_format), annual.getPrice().getFormatted()));
                    }

                    monthlyPackage = defaultOffer.getMonthly();

                    if (monthlyPackage != null) {
                        StoreProduct monthly = monthlyPackage.getProduct();

                        if (sharePrefs.getInt("paywallItem") == 1) b.text2.setText(String.format(getString(R.string.monthly_sub_format), monthly.getPrice().getFormatted()));
                    }

                    weeklyPackage = defaultOffer.getWeekly();

                    if (weeklyPackage != null) {
                        StoreProduct weekly = weeklyPackage.getProduct();

                        if (sharePrefs.getInt("paywallItem") == 2) b.text2.setText(String.format(getString(R.string.weekly_sub_format), weekly.getPrice().getFormatted()));
                    } else Log.d(TAG, "onReceived: weekly null");
                }

                if (discountedOffer != null) {
                    discountedAnnualPackage = discountedOffer.getAnnual();
                    discountedMonthlyPackage = discountedOffer.getMonthly();
                    discountedWeeklyPackage = discountedOffer.getWeekly();
                }

                b.buyBt.setOnClickListener(v -> purchaseSub(null, false));
                b.restoreBt.setOnClickListener(v -> restoreSub());
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                Toast.makeText(PaywallActivity.this, getString(R.string.something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError: " + error.getMessage());
            }
        });
    }

    private void purchaseSub(com.revenuecat.purchases.Package aPackage, boolean discounted) {
        if (aPackage == null) {
            if (!discounted) {
                switch (sharePrefs.getInt("paywallItem")) {
                    case 0:
                        aPackage = annualPackage;
                        break;
                    case 1:
                        aPackage = monthlyPackage;
                        break;
                    case 2:
                        aPackage = weeklyPackage;
                        break;
                }
            } else {
                switch (sharePrefs.getInt("paywallDiscountItem")) {
                    case 0:
                        aPackage = discountedAnnualPackage;
                        break;
                    case 1:
                        aPackage = discountedMonthlyPackage;
                        break;
                    case 2:
                        aPackage = discountedWeeklyPackage;
                        break;
                }
            }

            if (aPackage == null) return;
        }

        Package finalAPackage = aPackage;

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, sharePrefs.getUserID());
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, finalAPackage.getProduct().getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, finalAPackage.getProduct().getName());
        bundle.putLong(FirebaseAnalytics.Param.PRICE, finalAPackage.getProduct().getPrice().getAmountMicros());

        FirebaseAnalytics.getInstance(this).logEvent("start_purchase", bundle);

        Purchases.getSharedInstance().purchase(
                new PurchaseParams.Builder(this, finalAPackage).build(),
                new PurchaseCallback() {
                    @Override
                    public void onCompleted(@NonNull StoreTransaction storeTransaction, @NonNull CustomerInfo customerInfo) {
                        EntitlementInfo entitlementInfo = customerInfo.getEntitlements().get("premium");
                        if (entitlementInfo != null) {
                            if (entitlementInfo.isActive()) {
                                sharePrefs.putBoolean("premium", true);
                                FirebaseAnalytics.getInstance(PaywallActivity.this).logEvent("purchase_sub", bundle);
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("free");
                                FirebaseMessaging.getInstance().subscribeToTopic("premium");
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull PurchasesError purchasesError, boolean b) {

                    }
                });
    }

    private void restoreSub() {
        Purchases.getSharedInstance().restorePurchases(new ReceiveCustomerInfoCallback() {
            @Override
            public void onError(@NonNull PurchasesError error) {
                Toast.makeText(PaywallActivity.this, getString(R.string.something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                EntitlementInfo entitlementInfo = customerInfo.getEntitlements().get("premium");
                if (entitlementInfo != null) {
                    if (entitlementInfo.isActive()) {
                        sharePrefs.putBoolean("premium", true);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("free");
                        FirebaseMessaging.getInstance().subscribeToTopic("premium");
                        finish();
                    }
                } else Toast.makeText(PaywallActivity.this, getString(R.string.no_active_subscription_found), Toast.LENGTH_SHORT).show();
            }
        });
    }
}