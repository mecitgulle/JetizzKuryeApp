package com.arasholding.jetizzkuryeapp.apimodels;

import com.google.gson.annotations.SerializedName;

public class DeliveryRequest {
    @SerializedName("Id")
    private int Id;
    @SerializedName("TeslimTarihi")
    private String TeslimTarihi;
    @SerializedName("TeslimAlanKimlikNo")
    private String TeslimAlanKimlikNo;
    @SerializedName("TeslimAlanAd")
    private String TeslimAlanAd;
    @SerializedName("TeslimAlanSoyad")
    private String TeslimAlanSoyad;
    @SerializedName("TeslimKoordinat")
    private String TeslimKoordinat;
    @SerializedName("Kaynak")
    private String Kaynak;
    @SerializedName("Kanit")
    private String Kanit;
    @SerializedName("TeslimTipi")
    private int TeslimTipi;
    @SerializedName("Resim")
    private String Resim;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }


    public String getTeslimTarihi() {
        return TeslimTarihi;
    }

    public void setTeslimTarihi(String teslimTarihi) {
        TeslimTarihi = teslimTarihi;
    }

    public String getTeslimAlanKimlikNo() {
        return TeslimAlanKimlikNo;
    }

    public void setTeslimAlanKimlikNo(String teslimAlanKimlikNo) {
        TeslimAlanKimlikNo = teslimAlanKimlikNo;
    }

    public String getTeslimAlanAd() {
        return TeslimAlanAd;
    }

    public void setTeslimAlanAd(String teslimAlanAd) {
        TeslimAlanAd = teslimAlanAd;
    }

    public String getTeslimAlanSoyad() {
        return TeslimAlanSoyad;
    }

    public void setTeslimAlanSoyad(String teslimAlanSoyad) {
        TeslimAlanSoyad = teslimAlanSoyad;
    }

    public int getTeslimTipi() {
        return TeslimTipi;
    }

    public void setTeslimTipi(int teslimTipi) {
        TeslimTipi = teslimTipi;
    }

    public String getTeslimKoordinat() {
        return TeslimKoordinat;
    }

    public void setTeslimKoordinat(String teslimKoordinat) {
        TeslimKoordinat = teslimKoordinat;
    }

    public String getKaynak() {
        return Kaynak;
    }

    public void setKaynak(String kaynak) {
        Kaynak = kaynak;
    }

    public String getKanit() {
        return Kanit;
    }

    public void setKanit(String kanit) {
        Kanit = kanit;
    }

    public String getResim() { return Resim; }

    public void setResim(String resim) {
        Resim = resim;
    }
}
