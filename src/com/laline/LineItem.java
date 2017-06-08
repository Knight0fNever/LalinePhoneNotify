package com.laline;


public class LineItem {



    private String ilc;
    private int itemId;
    private String description;
    private int storeId;
    private String rep;
    private double price;
    private double cost;
    private double qty;
    private double whQty;
    private double salesTax;
    private String comment;
    private int transactionEntryId;
    private int transactionNumber;



    LineItem(int transactionNumber, int storeId) {
        this.transactionNumber = transactionNumber;
        this.storeId = storeId;
    }

    public String getIlc() {
        return ilc;
    }

    void setIlc(String ilc) {
        this.ilc = ilc;
    }

    void setCost(double cost) {
        this.cost = cost;
    }

    int getItemId() {
        return itemId;
    }

    void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getRep() {
        return rep;
    }

    void setRep(String rep) {
        this.rep = rep;
    }

    public double getPrice() {
        return price;
    }

    void setPrice(double price) {
        this.price = price;
    }

    public double getQty() {
        return qty;
    }

    void setQty(double qty) {
        this.qty = qty;
    }

    public String getComment() {
        return comment;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    public int getTransactionEntryId() {
        return transactionEntryId;
    }

    void setTransactionEntryId(int transactionEntryId) {
        this.transactionEntryId = transactionEntryId;
    }

    void setWhQty(double quantity) {
        this.whQty = quantity;
    }

    public double getWhQty() {
        return whQty;
    }

    public double getCost() {
        return cost;
    }

    public void setSalesTax(double salesTax) {
        this.salesTax = salesTax;
    }

    public double getSalesTax() {
        return salesTax;
    }
}
