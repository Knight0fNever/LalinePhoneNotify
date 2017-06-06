package com.laline;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.laline.SQL.viewTable;

public class CheckInventory {

    private int storeNumber;
    private int transNumber;
    private String[] lineItems;
    private Connection con;
    private TransactionDetails td;
    private int[] whQty;


    //Constructor
    public CheckInventory(Connection con, TransactionDetails td) throws SQLException {
        this.td = td;
        this.storeNumber = this.td.getStore();
        this.transNumber = this.td.getTransNumber();
        this.con = con;
        this.lineItems = new String[this.td.size()];
        queryInvQty();
    }


    //Queries database and sets WH quantity of line items into array
    public void queryInvQty() throws SQLException {
        this.whQty = new int[lineItems.length];
        String query = "";
        for(int i = 0; i < this.td.size(); i++) {
            this.lineItems[i] = this.td.getLookupCode(i);
        }
        for(int i = 0; i < this.lineItems.length; i++) {
            if (this.lineItems.length != 0) {
                query = "SELECT ItemDynamic.Quantity FROM ItemDynamic " +
                        "LEFT JOIN Item ON ItemDynamic.ItemID = Item.ID " +
                        " WHERE ItemDynamic.StoreID = " + this.storeNumber + " AND Item.ItemLookupCode = '" + this.lineItems[i] + "'";
            }
            ResultSet rs = viewTable(this.con, query);
            while(rs.next()) {
                this.whQty[i] = rs.getInt(1);
            }
        }

    }


    //Returns true if warehouse has sufficient stock
    public boolean inStockWH(int n) throws SQLException {
        this.queryInvQty();
        if(stockDiff(n) >= 0) {
            return true;
        }
        return false;
    }

    //Calculates the inventory difference, negative number represents not enough stock in WH
    public int stockDiff(int n) {
       return (int) (this.whQty[n] - this.td.getQty(n));
    }


    //Getters
    public int getStoreNumber() {
        return this.storeNumber;
    }

    public int getTransNumber() {
        return this.transNumber;
    }

    public String[] getLineItems() {
        return this.lineItems;
    }

    public int[] getWHQty() {
        return this.whQty;
    }

}
