package com.arasholding.jetizzkuryeapp.apimodels;

import org.joda.time.DateTime;

public class AtfDetay
{
    private String AtfNo;

    private String Barkod;

    private String Unvan;

    private int OdemeTipi;

    private String Gonderen;

    private String Alici;

    private String AliciTelefon ;

    private String AliciAdres;

    private int ToplamParca;

    private double ToplamDesi;

    private String ToplamFiyat;

    private String TeslimAlan;

    private DateTime TeslimTarihi;

    private boolean Tahsilatlimi;

    private double TahsilatTutari;

    public String getAtfNo() {
        return AtfNo;
    }

    public void setAtfNo(String atfNo) {
        AtfNo = atfNo;
    }

    public String getBarkod() {
        return Barkod;
    }

    public void setBarkod(String barkod) {
        Barkod = barkod;
    }

    public String getUnvan() {
        return Unvan;
    }

    public void setUnvan(String unvan) {
        Unvan = unvan;
    }

    public int getOdemeTipi() {
        return OdemeTipi;
    }

    public void setOdemeTipi(int odemeTipi) {
        OdemeTipi = odemeTipi;
    }

    public String getGonderen() {
        return Gonderen;
    }

    public void setGonderen(String gonderen) {
        Gonderen = gonderen;
    }

    public String getAlici() {
        return Alici;
    }

    public void setAlici(String alici) {
        Alici = alici;
    }

    public String getAliciTelefon() {
        return AliciTelefon;
    }

    public void setAliciTelefon(String aliciTelefon) {
        AliciTelefon = aliciTelefon;
    }

    public String getAliciAdres() {
        return AliciAdres;
    }

    public void setAliciAdres(String aliciAdres) {
        AliciAdres = aliciAdres;
    }

    public int getToplamParca() {
        return ToplamParca;
    }

    public void setToplamParca(int toplamParca) {
        ToplamParca = toplamParca;
    }

    public double getToplamDesi() {
        return ToplamDesi;
    }

    public void setToplamDesi(double toplamDesi) {
        ToplamDesi = toplamDesi;
    }

    public String getToplamFiyat() {
        return ToplamFiyat;
    }

    public void setToplamFiyat(String toplamFiyat) {
        ToplamFiyat = toplamFiyat;
    }

    public String getTeslimAlan() {
        return TeslimAlan;
    }

    public void setTeslimAlan(String teslimAlan) {
        TeslimAlan = teslimAlan;
    }

    public DateTime getTeslimTarihi() {
        return TeslimTarihi;
    }

    public void setTeslimTarihi(DateTime teslimTarihi) {
        TeslimTarihi = teslimTarihi;
    }

    public boolean isTahsilatlimi() {
        return Tahsilatlimi;
    }

    public void setTahsilatlimi(boolean tahsilatlimi) {
        Tahsilatlimi = tahsilatlimi;
    }

    public double getTahsilatTutari() {
        return TahsilatTutari;
    }

    public void setTahsilatTutari(double tahsilatTutari) {
        TahsilatTutari = tahsilatTutari;
    }
}
