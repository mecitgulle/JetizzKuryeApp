package com.arasholding.jetizzkuryeapp.apimodels;

public class ReturnRequest {
    private int Id;
    private int IadeNedeni;
    private String Kanit;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getIadeNedeni() {
        return IadeNedeni;
    }

    public void setIadeNedeni(int iadeNedeni) {
        IadeNedeni = iadeNedeni;
    }

    public String getKanit() {
        return Kanit;
    }

    public void setKanit(String kanit) {
        Kanit = kanit;
    }
}
