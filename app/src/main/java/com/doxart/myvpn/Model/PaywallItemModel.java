package com.doxart.myvpn.Model;

public class PaywallItemModel {
    private int drawable;
    private String title;
    private String txt;

    public PaywallItemModel() {}

    public PaywallItemModel(int drawable, String title, String txt) {
        this.drawable = drawable;
        this.title = title;
        this.txt = txt;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }
}
