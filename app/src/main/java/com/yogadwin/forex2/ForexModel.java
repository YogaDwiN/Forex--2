package com.yogadwin.forex2;

public class ForexModel {
    public String code, name;

    public double rate;

    public ForexModel(String code, String name, double rate){
        this.code = code;
        this.name = name;
        this.rate = rate;
    }
}
