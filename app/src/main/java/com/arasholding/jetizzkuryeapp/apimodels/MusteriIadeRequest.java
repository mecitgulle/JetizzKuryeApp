package com.arasholding.jetizzkuryeapp.apimodels;

public class MusteriIadeRequest {
    private int musteriId;
    private String barkod;
    private String teslimalan;


    public int getMusteriId() {
        return musteriId;
    }

    public void setMusteriId(int musteriId) {
        this.musteriId = musteriId;
    }

    public String getBarkod() {
        return barkod;
    }

    public void setBarkod(String barkod) {
        this.barkod = barkod;
    }

    public String getTeslimalan() {
        return teslimalan;
    }

    public void setTeslimalan(String teslimalan) {
        this.teslimalan = teslimalan;
    }
}
