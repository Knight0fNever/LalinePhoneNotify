package com.laline;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.laline.SQL.viewTable;

public class Transaction {

    private int transactionNumber;
    private int storeId;
    private String comment;
    private double total;
    private String cashier;
    private double salesTax;
    private double shipping;
    private String customerName;
    private String customerCompany;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    private List<LineItem> lineItems = new ArrayList<>();
    private List<LineItem> lineItemsUpdated;
    private String transactionTime;
    private Connection con;
    private List<Tender> tenders = new ArrayList<>();

    Transaction(int transactionNumber, int storeId, SQL sql) throws SQLException {
        this.transactionNumber = transactionNumber;
        this.storeId = storeId;
        this.con = sql.getCon();
        setTransactionDetails();
        setUpdatedLineItems();
        setCustomerDetails();
        setTenders();
    }

    private void setUpdatedLineItems() {
        lineItemsUpdated = new ArrayList<>(lineItems.size());
        for(LineItem lineItem : lineItems) {
            lineItemsUpdated.add(new LineItem(lineItem));
        }
    }

    private void setTenders() throws SQLException {
        String query = "SELECT TenderEntry.Amount, TenderEntry.[Description] FROM [Transaction]\n" +
                "LEFT JOIN TenderEntry ON [Transaction].TransactionNumber = TenderEntry.TransactionNumber AND [Transaction].StoreID = TenderEntry.StoreID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            Tender tender = new Tender();
            tender.setTenderType(rs.getString("Description"));
            tender.setTenderAmount(rs.getDouble("Amount"));
            tenders.add(tender);
        }
    }

    private void setCustomerDetails() throws SQLException {
        setCustomerName();
        setCustomerCompany();
        setCustomerAddress();
        setCustomerEmail();
        setCustomerPhone();
    }

    private void setTransactionDetails() throws SQLException {
        setComment();
        setTotal();
        setCashier();
        setSalesTax();
        setShipping();
        setTransactionLines();
        setTransactionTime();
    }

    private void setShipping() throws SQLException {
        String query = "SELECT Shipping.Charge FROM [Transaction]\n" +
                "LEFT JOIN Shipping ON [Transaction].TransactionNumber = Shipping.TransactionNumber AND [Transaction].StoreID = Shipping.StoreID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.shipping = rs.getDouble("Charge");
        }
    }

    private void setSalesTax() throws SQLException {
        String query = "SELECT [Transaction].SalesTax FROM [Transaction]\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.salesTax = rs.getDouble("SalesTax");
        }
    }

    private void setCashier() throws SQLException {
        String query = "SELECT Cashier.Name FROM [Transaction]\n" +
                "LEFT JOIN Cashier ON [Transaction].CashierID = Cashier.ID AND [Transaction].StoreID = Cashier.StoreID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.cashier = rs.getString("Name");
        }
    }

    private void setTotal() throws SQLException {
        String query = "SELECT [Transaction].Total FROM [Transaction]\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.total = rs.getDouble("Total");
        }
    }

    private void setTransactionLines() throws SQLException {
        String query = "SELECT TransactionEntry.ItemID, Item.ItemLookupCode, Item.[Description], SalesRep.Name, TransactionEntry.Price,\n" +
                "TransactionEntry.Quantity, TransactionEntry.Comment, TransactionEntry.ID, TransactionEntry.SalesTax, TransactionEntry.Cost \n" +
                "FROM [TransactionEntry]\n" +
                "LEFT JOIN Item ON TransactionEntry.ItemID = Item.ID\n" +
                "LEFT JOIN SalesRep ON TransactionEntry.SalesRepID = SalesRep.ID AND TransactionEntry.StoreID = SalesRep.StoreID\n" +
                "WHERE [TransactionEntry].TransactionNumber = " + this.transactionNumber + " AND [TransactionEntry].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            LineItem lineItem = new LineItem(this.transactionNumber, this.storeId);
            lineItem.setItemId(rs.getInt("ItemID"));
            lineItem.setIlc(rs.getString("ItemLookupCode"));
            lineItem.setDescription(rs.getString("Description"));
            lineItem.setRep(rs.getString("Name"));
            lineItem.setPrice(rs.getDouble("Price"));
            lineItem.setQty(rs.getDouble("Quantity"));
            lineItem.setComment(rs.getString("Comment"));
            lineItem.setTransactionEntryId(rs.getInt("ID"));
            lineItem.setSalesTax(rs.getDouble("SalesTax"));
            lineItem.setCost(rs.getDouble("Cost"));
            lineItem.setWhQty(setWarehouseQty(lineItem.getItemId()));
            lineItems.add(lineItem);
        }
    }

    private double setWarehouseQty(int itemId) throws SQLException {
        double result = 0.0;
        String query = "SELECT ItemDynamic.Quantity FROM ItemDynamic\n" +
                "WHERE ItemDynamic.StoreID = 1 AND ItemDynamic.ItemID = " + itemId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            result = rs.getDouble("Quantity");
        }
        return result;
    }

    private void setComment() throws SQLException {
        String query = "SELECT [Transaction].Comment FROM [Transaction]\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.comment = rs.getString("Comment");
        }
    }

    private void setCustomerName() throws SQLException {
        String query = "SELECT Customer.FirstName + ' ' +  Customer.LastName as 'Name' FROM [Transaction]\n" +
                "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.customerName = rs.getString("Name");
        }
    }

    private void setCustomerCompany() throws SQLException {
        String query = "SELECT Customer.Company FROM [Transaction]\n" +
                "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.customerCompany = rs.getString("Company");
        }
        if(this.customerCompany == null) {
            this.customerCompany = "";
        }
    }

    private void setCustomerAddress() throws SQLException {
        String query = "SELECT Customer.[Address], Customer.Address2, Customer.City, Customer.[State], Customer.Zip, Customer.Country FROM [Transaction]\n" +
                "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        String address = "";
        String address2 = "";
        String city = "";
        String state = "";
        String zip = "";
        String country = "";
        while(rs.next()) {
            address = rs.getString("Address");
            address2 = rs.getString("Address2");
            city = rs.getString("City");
            state = rs.getString("State");
            zip = rs.getString("Zip");
            country = rs.getString("Country");
        }
        StringBuilder sb = new StringBuilder();
        if(!address.isEmpty()) {
            sb.append(address);
            sb.append("\n");
        }
        if(!address2.isEmpty()) {
            sb.append(address2);
            sb.append("\n");
        }
        sb.append(city + ", " + state + " " + zip + "\n" + country);
        this.customerAddress = sb.toString();
    }

    private void setCustomerPhone() throws SQLException {
        String query = "SELECT Customer.PhoneNumber FROM [Transaction]\n" +
                "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.customerPhone = rs.getString("PhoneNumber");
        }
    }

    private void setCustomerEmail() throws SQLException {
        String query = "SELECT Customer.EmailAddress FROM [Transaction]\n" +
                "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while (rs.next()) {
            this.customerEmail = rs.getString("EmailAddress");
        }
    }

    private void setTransactionTime() throws SQLException {
        String query = "SELECT [Transaction].[Time] FROM [Transaction]\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.transactionTime = rs.getString("Time");
        }

    }

    public int getTransactionNumber() {
        return transactionNumber;
    }

    public int getStoreId() {
        return storeId;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public String getComment() {
        return comment;
    }

    public double getTotal() {
        return total;
    }

    public String getCashier() {
        return cashier;
    }

    public double getSalesTax() {
        return salesTax;
    }

    public double getShippingCharge() {
        return shipping;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerCompany() {
        return customerCompany;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public List<Tender> getTenders() {
        return tenders;
    }

    public List<LineItem> getLineItemsUpdated() {
        return lineItemsUpdated;
    }

    public void setLineItemsUpdated(List<LineItem> lineItemsUpdated) {
        this.lineItemsUpdated = lineItemsUpdated;
    }
}
