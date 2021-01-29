package com.ulasgokce.myapplication.Models;

import com.ulasgokce.myapplication.Models.Data;

public class Sender {
    public Data data;
    String to;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
}
