package com.laline;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.laline.SQL.viewTable;

public class ShippingInfo {

    private String name;
    private String company;
    private int transNumber;
    private double shippingCharge;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String email;
    private String transTime;
    private String phone;
    private int storeID;
    private Connection con;



    //Constructor
    public ShippingInfo(Connection con, int storeID, int transNumber) throws SQLException {
        this.storeID = storeID;
        this.transNumber = transNumber;
        this.con = con;
        setShippingInfo();
    }


    //Queries the database for customer and shipping information and parses into appropriate field
    public void setShippingInfo() throws SQLException {
        String query = "SELECT Shipping.TransactionNumber, Shipping.Charge, Shipping.Name, Shipping.Company, Shipping.[Address]," +
                " Shipping.Address2, Shipping.City, Shipping.[State], Shipping.Zip, Shipping.Country, Shipping.EmailAddress," +
                " Shipping.DateCreated, Shipping.PhoneNumber, Shipping.StoreID" +
                " FROM Shipping" +
                " WHERE Shipping.StoreID = " + this.storeID + " AND Shipping.TransactionNumber = " + this.transNumber;
        ResultSet rs = viewTable(this.con, query);
        while(rs.next()) {
            setTransNumber(rs.getInt("TransactionNumber"));
            setShippingCharge(rs.getDouble("Charge"));
            setName(rs.getString("Name"));
            setCompany(rs.getString("Company"));
            setAddress(rs.getString("Address"));
            setAddress2(rs.getString("Address2"));
            setCity(rs.getString("City"));
            setState(rs.getString("State"));
            setZip(rs.getString("Zip"));
            setCountry(rs.getString("Country"));
            setEmail(rs.getString("EmailAddress"));
            setTransTime(rs.getString("DateCreated"));
            setPhone(rs.getString("PhoneNumber"));
            setStoreID(rs.getInt("StoreID"));
        }
    }



    public void setName(String s) {
        this.name = s;
    }

    public void setCompany(String s) {
        this.company = s;
    }

    public void setTransNumber(int n) {
        this.transNumber = n;
    }

    public void setShippingCharge(double n) {
        this.shippingCharge = n;
    }

    public void setAddress(String s) {
        this.address = s;
    }

    public void setAddress2(String s) {
        this.address2 = s;
    }

    public void setCity(String s) {
        this.city = s;
    }

    public void setState(String s) {
        this.state = s;
    }

    public void setZip(String s) {
        this.zip = s;
    }

    public void setCountry(String s) {
        this.country = s;
    }

    public void setEmail(String s) {
        this.email = s;
    }

    public void setTransTime(String s) {
        this.transTime = s;
    }

    public void setPhone(String s) {
        this.phone = s;
    }

    public void setStoreID(int n) {
        this.storeID = n;
    }

    public String getName() {
        return this.name;
    }

    public String getCompany() {
        return this.company;
    }

    public String getAddress() {
        return this.address;
    }

    public String getAddress2() {
        return this.address2;
    }

    public String getCity() {
        return this.city;
    }

    public String getState() {
        return this.state;
    }

    public String getZip() {
        return  this.zip;
    }

    public String getCountry() {
        return this.country;
    }

    public String getEmail() {
        return this.email;
    }

    public String getTransTime() {
        return this.transTime;
    }

    public String getPhone() {
        return this.phone;
    }

    public int getTransNumber() {
        return this.transNumber;
    }

    public int getStoreID() {
        return this.storeID;
    }

    public double getShippingCharge() {
        return this.shippingCharge;
    }
}
