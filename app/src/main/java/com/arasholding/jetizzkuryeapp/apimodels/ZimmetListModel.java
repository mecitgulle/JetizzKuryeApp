package com.arasholding.jetizzkuryeapp.apimodels;

import org.joda.time.DateTime;

public class ZimmetListModel {
    private int AtfId;
    private String AtfNo;
    private String Alici;
    private String MusteriTakipNo;
    private String AliciTelefon;
    private String SonIslem;
    private String GondericiAdres;
    private String GondericiNotu;
    private String GonderenTelefon;
    private String OlasiTeslimTarihi;
    private String TahsilatTipi;
    private double TahsilatTutari;
    private String AliciAdres;
    private String Barkod;
    private String Kurye;
    private String Hizmet;
    private String Magaza;
    private String Desi;
    private DateTime CreateDate;
    private int ToplamParca;
    private int MusteriId;

    public int getAtfId() {
        return AtfId;
    }

    public void setAtfId(int atfId) {
        AtfId = atfId;
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

    public String getGondericiNotu() {
        return GondericiNotu;
    }

    public void setGondericiNotu(String gondericiNotu) {
        GondericiNotu = gondericiNotu;
    }

    public String getSonIslem() {
        return SonIslem;
    }

    public void setSonIslem(String sonIslem) {
        SonIslem = sonIslem;
    }

    public String getBarkod() {
        return Barkod;
    }

    public void setBarkod(String barkod) {
        Barkod = barkod;
    }

    public String getKurye() {
        return Kurye;
    }

    public void setKurye(String kurye) {
        Kurye = kurye;
    }

    public String getHizmet() {
        return Hizmet;
    }

    public void setHizmet(String hizmet) {
        Hizmet = hizmet;
    }

    public String getMagaza() {
        return Magaza;
    }

    public void setMagaza(String magaza) {
        Magaza = magaza;
    }

    public String getDesi() {
        return Desi;
    }

    public void setDesi(String desi) {
        Desi = desi;
    }

    public String getAtfNo() {
        return AtfNo;
    }

    public void setAtfNo(String atfNo) {
        AtfNo = atfNo;
    }

    public String getMusteriTakipNo() {
        return MusteriTakipNo;
    }

    public void setMusteriTakipNo(String musteriTakipNo) {
        MusteriTakipNo = musteriTakipNo;
    }

    public String getAliciAdres() {
        return AliciAdres;
    }

    public void setAliciAdres(String aliciAdres) {
        AliciAdres = aliciAdres;
    }

    public String getGondericiAdres() {
        return GondericiAdres;
    }

    public void setGondericiAdres(String gondericiAdres) {
        GondericiAdres = gondericiAdres;
    }

    public String getGonderenTelefon() {
        return GonderenTelefon;
    }

    public void setGonderenTelefon(String gonderenTelefon) {
        GonderenTelefon = gonderenTelefon;
    }

    public String getOlasiTeslimTarihi() {
        return OlasiTeslimTarihi;
    }

    public void setOlasiTeslimTarihi(String olasiTeslimTarihi) {
        OlasiTeslimTarihi = olasiTeslimTarihi;
    }

    public String getTahsilatTipi() {
        return TahsilatTipi;
    }

    public void setTahsilatTipi(String tahsilatTipi) {
        TahsilatTipi = tahsilatTipi;
    }

    public double getTahsilatTutari() {
        return TahsilatTutari;
    }

    public void setTahsilatTutari(double tahsilatTutari) {
        TahsilatTutari = tahsilatTutari;
    }

    public int getMusteriId() { return MusteriId; }

    public void setMusteriId(int musteriId) { MusteriId = musteriId; }

    public DateTime getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(DateTime createDate) {
        CreateDate = createDate;
    }

    public int getToplamParca() {
        return ToplamParca;
    }

    public void setToplamParca(int toplamParca) {
        ToplamParca = toplamParca;
    }
}
