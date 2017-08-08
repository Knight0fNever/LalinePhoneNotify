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
    private Customer customer;
    private List<LineItem> lineItems = new ArrayList<>();
    private List<LineItem> lineItemsUpdated;
    private String transactionTime;
    private Connection con;
    private List<Tender> tenders = new ArrayList<>();

    Transaction(int transactionNumber, int storeId, SQL sql) throws SQLException {
        this.customer = new Customer();
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
        int shipToId = 0;
        String query = "SELECT [Transaction].ShipToID FROM [Transaction]\n" +
                "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + "[Transaction].StoreID = " + this.storeId;
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            shipToId = rs.getInt("ShipToID");
        }
        if(shipToId == 0) {
            //Billing
            setCustomerName("SELECT Customer.FirstName + ' ' + Customer.LastName as 'Name' FROM [Transaction]\n" +
                    "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerCompany("SELECT Customer.Company FROM [Transaction]\n" +
                    "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerAddress("SELECT Customer.[Address], Customer.Address2, Customer.City, Customer.[State], Customer.Zip, Customer.Country FROM [Transaction]\n" +
            "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerPhone("SELECT Customer.PhoneNumber FROM [Transaction]\n" +
                    "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerEmail("SELECT Customer.EmailAddress FROM [Transaction]\n" +
                    "LEFT JOIN Customer ON [Transaction].CustomerID = Customer.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
        }
        else {
            //Shipping
            setCustomerName("SELECT ShipTo.Name FROM [Transaction]\n" +
                    "LEFT JOIN ShipTo ON [Transaction].ShipToID = ShipTo.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerCompany("SELECT ShipTo.Company FROM [Transaction]\n" +
                    "LEFT JOIN ShipTo ON [Transaction].ShipToID = ShipTo.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerAddress("SELECT ShipTo.[Address], ShipTo.Address2, ShipTo.City, ShipTo.[State], ShipTo.Zip, ShipTo.Country FROM [Transaction]\n" +
                    "LEFT JOIN ShipTo ON [Transaction].ShipToID = ShipTo.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerPhone("SELECT ShipTo.PhoneNumber FROM [Transaction]\n" +
                    "LEFT JOIN ShipTo ON [Transaction].ShipToID = ShipTo.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
            setCustomerEmail("SELECT ShipTo.EmailAddress FROM [Transaction]\n" +
                    "LEFT JOIN ShipTo ON [Transaction].ShipToID = ShipTo.ID\n" +
                    "WHERE [Transaction].TransactionNumber = " + this.transactionNumber + " AND [Transaction].StoreID = " + this.storeId);
        }
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

    private void setCustomerName(String query) throws SQLException {
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.customer.getAddress().setName(rs.getString("Name"));
        }
    }

    private void setCustomerCompany(String query) throws SQLException {
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.customer.getAddress().setCompany(rs.getString("Company"));
        }
        if(this.customer.getAddress().getCompany() == null) {
            this.customer.getAddress().setCompany("");
        }
    }

    private void setCustomerAddress(String query) throws SQLException {
        ResultSet rs = viewTable(con, query);
        String addressLocal = "";
        String address2Local = "";
        while(rs.next()) {
            addressLocal = rs.getString("Address");
            address2Local = rs.getString("Address2");
            this.customer.getAddress().setCity(rs.getString("City"));
            this.customer.getAddress().setState(rs.getString("State"));
            this.customer.getAddress().setZip(rs.getString("Zip"));
            this.customer.getAddress().setCountry(rs.getString("Country"));
        }
        StringBuilder sb = new StringBuilder();
        if(!addressLocal.isEmpty()) {
            sb.append(addressLocal);
            sb.append("\n");
        }
        if(!address2Local.isEmpty()) {
            sb.append(address2Local);
            sb.append("\n");
        }
        sb.append(this.customer.getAddress().getCity() + ", " + this.customer.getAddress().getState() + " " + this.customer.getAddress().getZip() + "\n" + this.customer.getAddress().getCountry());
        this.customer.getAddress().setAddress(sb.toString());
    }

    private void setCustomerPhone(String query) throws SQLException {
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.customer.getAddress().setPhoneNumber(rs.getString("PhoneNumber"));
        }
    }

    private void setCustomerEmail(String query) throws SQLException {
        ResultSet rs = viewTable(con, query);
        while (rs.next()) {
            this.customer.getAddress().setEmail(rs.getString("EmailAddress"));
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
        return this.customer.getAddress().getName();
    }

    public String getCustomerCompany() {
        return this.customer.getAddress().getCompany();
    }

    public String getCustomerAddress() {
        return this.customer.getAddress().getAddress();
    }

    public String getCustomerPhone() {
        return this.customer.getAddress().getPhoneNumber();
    }

    public String getCustomerEmail() {
        return this.customer.getAddress().getEmail();
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
