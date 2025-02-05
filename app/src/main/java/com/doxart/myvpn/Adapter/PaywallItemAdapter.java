package com.doxart.myvpn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doxart.myvpn.Model.PaywallItemModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.databinding.PaywallItemBinding;

import java.util.List;

public class PaywallItemAdapter extends RecyclerView.Adapter<PaywallItemAdapter.PIHolder> {

    private final Context context;
    private final List<PaywallItemModel> list;

    public PaywallItemAdapter(Context context, List<PaywallItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public PIHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PIHolder(LayoutInflater.from(context).inflate(R.layout.paywall_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PIHolder h, int p) {
        PaywallItemModel model = list.get(p);

        setParams(h);

        h.b.paywallImg.setImageResource(model.getDrawable());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void setParams(PIHolder h) {
        h.b.paywallImg.post(() -> {
            if (h.b.paywallImg.getHeight() <= 0) {
                setParams(h);
                return;
            }
            ViewGroup.LayoutParams params = h.b.paywallImg.getLayoutParams();
            if (params != null) {
                int w = h.b.paywallImg.getWidth() / 3;
                params.height = h.b.paywallImg.getWidth() / 2;
                h.b.paywallImg.setLayoutParams(params);
            }

        });
    }

    public static class PIHolder extends RecyclerView.ViewHolder {
        private final PaywallItemBinding b;
        public PIHolder(@NonNull View v) {
            super(v);
            b = PaywallItemBinding.bind(v);
        }
    }
}
