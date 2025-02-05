package com.doxart.myvpn.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doxart.myvpn.Interfaces.OnVPNServerSelectedListener;
import com.doxart.myvpn.Model.VPNModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.VpnItemBinding;

import org.spongycastle.util.encoders.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VPNAdapter extends RecyclerView.Adapter<VPNAdapter.VPNHolder> {

    private final Context context;
    private List<VPNModel> vpnModelList;
    private OnVPNServerSelectedListener onVPNServerSelectedListener;

    public VPNAdapter(Context context) {
        this.context = context;
    }

    public void setVpnModelList(List<VPNModel> vpnModelList) {
        this.vpnModelList = vpnModelList;
        notifyDataSetChanged();
    }

    public void resetList() {
        this.vpnModelList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnVPNServerSelectedListener(OnVPNServerSelectedListener onVPNServerSelectedListener) {
        this.onVPNServerSelectedListener = onVPNServerSelectedListener;
    }

    @NonNull
    @Override
    public VPNHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VPNHolder(LayoutInflater.from(context).inflate(R.layout.vpn_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VPNHolder h, int p) {
        VPNModel model = vpnModelList.get(p);
        h.b.cCountryTxt.setText(model.getIp());

        Utils.setSignalView(context, h.b.s1, h.b.s2, h.b.s3, model.getPing());

        h.itemView.setOnClickListener(v -> saveOpenVPNConfig(model));
    }

    private void saveOpenVPNConfig(VPNModel model) {
        try {
            byte[] decodedBytes = Base64.decode(model.getOvpnConfig());
            File file = new File(context.getFilesDir(), "vpn_config.ovpn");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedBytes);
            fos.close();

            if (onVPNServerSelectedListener != null) onVPNServerSelectedListener.onSelectServer(model);
        } catch (Exception e) {
            Log.d("SaveOVPNError", "saveOpenVPNConfig: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return vpnModelList.size();
    }

    public static class VPNHolder extends RecyclerView.ViewHolder {
        private final VpnItemBinding b;
        public VPNHolder(@NonNull View v) {
            super(v);
            b = VpnItemBinding.bind(v);
        }
    }
}
