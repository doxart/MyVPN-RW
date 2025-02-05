package com.doxart.myvpn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.doxart.myvpn.Interfaces.OnVPNServerSelectedListener;
import com.doxart.myvpn.Model.VPNModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.databinding.CountryItemNewBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CHolder> {

    private final Context context;
    private final List<VPNModel> vpnModelList;
    private final List<String> countryModelList;
    private final VPNAdapter vpnAdapter;
    private View lastView;

    public CountryAdapter(Context context, List<VPNModel> vpnModelList, List<String> countryModelList) {
        this.context = context;
        this.vpnModelList = vpnModelList;
        this.countryModelList = countryModelList;
        vpnAdapter = new VPNAdapter(context);
    }

    public void setOnVPNServerSelectedListener(OnVPNServerSelectedListener onVPNServerSelectedListener) {
        vpnAdapter.setOnVPNServerSelectedListener(onVPNServerSelectedListener);
    }

    @NonNull
    @Override
    public CHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CHolder(LayoutInflater.from(context).inflate(R.layout.country_item_new, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CHolder h, int p) {
        String model = countryModelList.get(p);

        Glide.with(context).load("https://flagcdn.com/h80/" + model.toLowerCase(Locale.US) + ".png").centerCrop().into(h.b.cFlagImg);

        h.b.cCountryTxt.setText(new Locale("", model).getDisplayCountry());

        h.itemView.setOnClickListener(v -> {
            if (lastView != null) lastView.setVisibility(View.GONE);
            if (h.b.vpnRecycler.getVisibility() == View.GONE) {
                lastView = h.b.vpnRecycler;
                h.b.vpnRecycler.setVisibility(View.VISIBLE);
                vpnAdapter.setVpnModelList(parseList(model));
                h.b.vpnRecycler.setAdapter(vpnAdapter);
            } else {
                vpnAdapter.resetList();
                h.b.vpnRecycler.setAdapter(null);
                h.b.vpnRecycler.setVisibility(View.GONE);
            }
        });
    }

    private List<VPNModel> parseList(String model) {
        List<VPNModel> list = new ArrayList<>();

        for (int i = 0; i < vpnModelList.size(); i++) {
            if (vpnModelList.get(i).getCountryId().equals(model)) {
                list.add(vpnModelList.get(i));
            }
        }

        return list;
    }

    @Override
    public int getItemCount() {
        return countryModelList.size();
    }

    public static class CHolder extends RecyclerView.ViewHolder {
        private final CountryItemNewBinding b;
        public CHolder(@NonNull View v) {
            super(v);
            b = CountryItemNewBinding.bind(v);
        }
    }
}
