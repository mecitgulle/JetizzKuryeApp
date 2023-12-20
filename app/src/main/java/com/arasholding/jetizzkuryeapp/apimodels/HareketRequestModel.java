package com.arasholding.jetizzkuryeapp.apimodels;

public class HareketRequestModel {
    private String Barkod;
    private String Kaynak;
    private String isKoliNo;
    private int MusteriId;
    private double Desi;
    private String KaynakIp;
    private String Koordinat;
    private String Sube;
    private boolean IsFarkliAcente;
    private boolean IsBarkodPrint;

    public String getBarkod() {
        return Barkod;
    }

    public void setBarkod(String barkod) {
        Barkod = barkod;
    }

    public String getKaynak() {
        return Kaynak;
    }

    public void setKaynak(String kaynak) {
        Kaynak = kaynak;
    }

    public String getIsKoliNo() {
        return isKoliNo;
    }

    public void setIsKoliNo(String isKoliNo) {
        this.isKoliNo = isKoliNo;
    }

    public int getMusteriId() {
        return MusteriId;
    }

    public void setMusteriId(int musteriId) {
        MusteriId = musteriId;
    }

    public double getDesi() {
        return Desi;
    }

    public void setDesi(double desi) {
        Desi = desi;
    }

    public String getKaynakIp() {
        return KaynakIp;
    }

    public void setKaynakIp(String kaynakIp) {
        KaynakIp = kaynakIp;
    }

    public String getKoordinat() {
        return Koordinat;
    }

    public void setKoordinat(String koordinat) {
        Koordinat = koordinat;
    }

    public String getSube() {
        return Sube;
    }

    public void setSube(String sube) {
        Sube = sube;
    }

    public boolean isFarkliAcente() {
        return IsFarkliAcente;
    }

    public void setFarkliAcente(boolean farkliAcente) {
        IsFarkliAcente = farkliAcente;
    }

    public boolean isBarkodPrint() {
        return IsBarkodPrint;
    }

    public void setBarkodPrint(boolean barkodPrint) {
        IsBarkodPrint = barkodPrint;
    }
}
