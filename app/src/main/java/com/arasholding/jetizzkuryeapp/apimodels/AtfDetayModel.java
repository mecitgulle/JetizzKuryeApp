package com.arasholding.jetizzkuryeapp.apimodels;

import org.joda.time.DateTime;

import java.util.List;

public class AtfDetayModel {
    private AtfDetay AtfDetay;
    private List<AtfHareketleri> AtfHareketleri;

    public com.arasholding.jetizzkuryeapp.apimodels.AtfDetay getAtfDetay() {
        return AtfDetay;
    }

    public void setAtfDetay(com.arasholding.jetizzkuryeapp.apimodels.AtfDetay atfDetay) {
        AtfDetay = atfDetay;
    }

    public List<com.arasholding.jetizzkuryeapp.apimodels.AtfHareketleri> getAtfHareketleri() {
        return AtfHareketleri;
    }

    public void setAtfHareketleri(List<com.arasholding.jetizzkuryeapp.apimodels.AtfHareketleri> atfHareketleri) {
        AtfHareketleri = atfHareketleri;
    }
}

