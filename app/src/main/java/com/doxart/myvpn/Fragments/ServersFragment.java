package com.doxart.myvpn.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doxart.myvpn.Adapter.CountryAdapter;
import com.doxart.myvpn.App.VPNHandler;
import com.doxart.myvpn.Interfaces.OnVPNServerSelectedListener;
import com.doxart.myvpn.Model.VPNModel;
import com.doxart.myvpn.databinding.FragmentServersBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ServersFragment extends BottomSheetDialogFragment {

    private final String TAG = "SERVERS_JOB";
    private FragmentServersBinding b;
    private Context context;

    private OnVPNServerSelectedListener onVPNServerSelectedListener;

    public void setOnVPNServerSelectedListener(OnVPNServerSelectedListener onVPNServerSelectedListener) {
        this.onVPNServerSelectedListener = onVPNServerSelectedListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentServersBinding.inflate(inflater, container, false);
        context = getContext();

        init();

        return b.getRoot();
    }

    private void init() {
        getCustomVPNs();

        if (VPNHandler.getVpnList() != null) {
            CountryAdapter countryAdapter = new CountryAdapter(context, VPNHandler.getVpnList(), getCountries(VPNHandler.getVpnList()));
            countryAdapter.setOnVPNServerSelectedListener(onVPNServerSelectedListener);

            b.recyclerView.setHasFixedSize(true);
            b.recyclerView.setAdapter(countryAdapter);
        }
    }

    private void getCustomVPNs() {
        FirebaseFirestore.getInstance().collection("countries").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<VPNModel> vpnModelList = task.getResult().toObjects(VPNModel.class);

                CountryAdapter countryAdapter = new CountryAdapter(context, vpnModelList, getCountries(vpnModelList));
                countryAdapter.setOnVPNServerSelectedListener(onVPNServerSelectedListener);

                b.customRecyclerView.setHasFixedSize(true);
                b.customRecyclerView.setAdapter(countryAdapter);
            } else {
                if (task.getException() != null) Log.d(TAG, "getCustomVPNs: " + task.getException().getMessage());
            }
        });
    }

    private List<String> getCountries(List<VPNModel> vpnList) {
        List<String> countryModelList = new ArrayList<>();

        for (int i = 0; i < vpnList.size(); i++) {
            VPNModel model = vpnList.get(i);
            if (!countryModelList.contains(model.getCountryId())) {
                countryModelList.add(model.getCountryId());
            }
        }

        countryModelList.sort(String::compareToIgnoreCase);

        return countryModelList;
    }
}