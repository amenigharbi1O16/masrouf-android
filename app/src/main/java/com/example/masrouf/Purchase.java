package com.example.masrouf;

public class Purchase {
    public long id, productId;
    public double total, qty;
    public String unit, store, date;
    public double unitPrice() { return qty == 0 ? total : total / qty; }
}
