package com.laline;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.laline.SQL.viewTable;

public class TransactionDetails {

    private List<String[]> lineItems = new ArrayList<>();
    private int transNumber;
    private int store;
    private Connection con;
    private int[] transStore;
    private String[] rep;
    private String[] sku;
    private String[] description;
    private double[] price;
    private double[] qty;
    private double[] total;
    private String[] comment;
    private int[] lineTransNumber;
    private String[] transactionTime;
    private double[] transactionSalesTax;
    private List<String[]> tenderInfo = new ArrayList<>();
    private String[] tenderType;
    private double[] tenderAmount;
    private String transComment;

    //Constructor
    public TransactionDetails(Connection c, int tn, int storeID) throws SQLException {
        this.transNumber = tn;
        this.store = storeID;
        this.con = c;
        getTransDetails();
    }

    //Returns the number of line items in the transaction
    public int size() {
        return this.lineItems.size();
    }


    //Queries database and returns line items into ArrayList lineItems
    public void getTransDetails() throws SQLException {
        String query = "SELECT TransactionEntry.TransactionNumber, Item.ItemLookupCode, Item.[Description], TransactionEntry.Price, TransactionEntry.Quantity, " +
                "(TransactionEntry.Price * TransactionEntry.Quantity) as 'Total', SalesRep.Name as 'SalesRep', TransactionEntry.StoreID, " +
                "TransactionEntry.Comment, TransactionEntry.TransactionTime, CAST((TransactionEntry.SalesTax * TransactionEntry.Quantity)as money) as 'TotalTax' " +
                " FROM TransactionEntry " +
                "LEFT JOIN Item ON TransactionEntry.ItemID = Item.ID " +
                "LEFT JOIN SalesRep ON TransactionEntry.SalesRepID = SalesRep.ID AND TransactionEntry.StoreID = SalesRep.StoreID" +
                " WHERE TransactionEntry.TransactionNumber = " + this.transNumber + " AND TransactionEntry.StoreID = " + this.store;
        ResultSet rs = viewTable(con, query);
        int nCol = rs.getMetaData().getColumnCount();
        while( rs.next()) {
            String[] row = new String[nCol];
            for( int iCol = 1; iCol <= nCol; iCol++ ){
                Object obj = rs.getObject( iCol );
                row[iCol-1] = (obj == null) ?null:obj.toString();
            }
            this.lineItems.add( row );
        }
        getTenderDetails();

        //Parses Transaction details into array for each column
        setSku();
        setQty();
        setDescription();
        setTotal();
        setRep();
        setPrice();
        setLineTransNumber();
        setComment();
        setTransactionTime();
        setTransactionSalesTax();
        setTransComment();
    }

    private void setTransComment() throws SQLException {
        String query = "SELECT [Transaction].Comment FROM [Transaction] WHERE [Transaction].TransactionNumber = " + this.transNumber
                + " AND [Transaction].StoreID = " + this.store;
        ResultSet rs = viewTable(this.con, query);
        while(rs.next()) {
            this.transComment = rs.getString("Comment");
        }

    }

    private void getTenderDetails() throws SQLException {
        String query = "SELECT Tender.[Description], TenderEntry.Amount " +
                " FROM TenderEntry " +
                " LEFT JOIN Tender ON TenderEntry.TenderID = Tender.ID" +
                " WHERE TenderEntry.StoreID = " + this.store + " AND TenderEntry.TransactionNumber = " + this.transNumber;
        ResultSet rs = viewTable(this.con, query);
        int nCol = rs.getMetaData().getColumnCount();
        while( rs.next()) {
            String[] row = new String[nCol];
            for( int iCol = 1; iCol <= nCol; iCol++ ){
                Object obj = rs.getObject( iCol );
                row[iCol-1] = (obj == null) ?null:obj.toString();
            }

            this.tenderInfo.add(row);

        }
        setTenderType();
        setTenderAmount();
    }

    //Parses TransactionNumbers from lineItems into TransactionNumber array lineTransNumber
    public void setLineTransNumber() {
        lineTransNumber = new int[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            lineTransNumber[i] = Integer.parseInt(this.lineItems.get(i)[0]);
        }
    }

    //Parses Prices from lineItems into Prices array price
    public void setPrice() {
        price = new double[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            price[i] = Double.parseDouble(this.lineItems.get(i)[3]);
        }
    }

    //Parses Quantities from lineItems into Quantity array qty
    public void setQty() {
        qty = new double[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            qty[i] = Double.parseDouble(this.lineItems.get(i)[4]);
        }
    }

    //Parses Totals from lineItems into Totals array total
    public void setTotal() {
        total = new double[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            total[i] = Double.parseDouble(this.lineItems.get(i)[5]);
        }
    }

    //Parses ItemLookupCodes from lineItems into ItemLookupCode array sku
    public void setSku() {
        sku = new String[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            sku[i] = this.lineItems.get(i)[1];
        }
    }

    //Parses Description from lineItems into Description array description
    public void setDescription() {
        description = new String[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            description[i] = this.lineItems.get(i)[2];
        }
    }

    //Parses SalesRep from lineItems into SalesRep array rep
    public void setRep() {
        rep = new String[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            rep[i] = this.lineItems.get(i)[6];
        }
        int i = 0;
    }

    //Parses StoreId from lineItems into StoreId array transStore
    public void setTransStore() {
        transStore = new int[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            transStore[i] = Integer.parseInt(this.lineItems.get(i)[7]);
        }
    }

    private void setComment() {
        this.comment = new String[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            this.comment[i] = this.lineItems.get(i)[8];
        }
    }

    private void setTransactionTime() {
        this.transactionTime = new String[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            this.transactionTime[i] = this.lineItems.get(i)[9];
        }
    }

    private void setTransactionSalesTax() {
        this.transactionSalesTax = new double[this.lineItems.size()];
        for(int i = 0; i < this.lineItems.size(); i++) {
            this.transactionSalesTax[i] = Double.parseDouble(this.lineItems.get(i)[10]);
        }
    }

    private void setTenderType() {
        this.tenderType = new String[this.tenderInfo.size()];
        for(int i = 0; i < this.tenderInfo.size(); i++) {
            this.tenderType[i] = this.tenderInfo.get(i)[0];
        }
    }

    private void setTenderAmount() {
        this.tenderAmount = new double[this.tenderInfo.size()];
        for(int i = 0; i < this.tenderInfo.size(); i++) {
            this.tenderAmount[i] = Double.parseDouble(this.tenderInfo.get(i)[1]);
        }
    }


    //Getters
    public double getPrice(int i) {
        return price[i];
    }

    public double getQty(int i) {
        return qty[i];
    }

    public String getLookupCode(int i) {
        return sku[i];
    }

    public String getDescription(int i) {
        return description[i];
    }

    public String getComment(int i) {
        return comment[i];
    }

    public double getTotal(int i) {
        return total[i];
    }

    public int getlineTransNumber(int i) {
        return lineTransNumber[i];
    }

    public int getStore() {
        return store;
    }

    public int getTransNumber() {
        return transNumber;
    }

    public List<String[]> getLineItems() {
        return lineItems;
    }

    public String[] getLine(int row) {
        return lineItems.get(row);
    }

    public String getTransactionTime() {
        if(this.transactionTime.length > 0) {
            return this.transactionTime[0];
        }
        else {
            return "";
        }
    }

    public double getTransactionSalesTax(int i) {
        return transactionSalesTax[i];
    }

    public int tenderTypeSize() {
        return this.tenderType.length;
    }

    public String getTenderType(int i) {
        return this.tenderType[i];
    }

    public double getTenderAmount(int i) {
        return this.tenderAmount[i];
    }

    public String getTransComment() {
        return this.transComment;
    }

}
