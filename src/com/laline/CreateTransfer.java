package com.laline;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.laline.SQL.runQuery;
import static com.laline.SQL.viewTable;

public class CreateTransfer {

    private String poTitle;
    private int storeId;
    private int worksheetId;
    private String poNumber;
    private int transNumber;
    private Connection con;
    private String store;
    private String storeInfo;
    private TransactionDetails td;
    private List<Integer> itemLinesToDrop = new ArrayList<>();
    private List<Integer> itemLinesTook = new ArrayList<>();




    //Constructor
    public CreateTransfer(Connection con, int transNumber, int storeId, TransactionDetails td) throws SQLException {
        this.transNumber = transNumber;
        this.storeId = storeId;
        this.con = con;
        this.td = td;
        setStore();
        setPoTitle();
        addItemsToDrop(td.getLineItems());
        if(!(td.getLineItems().size() == this.itemLinesToDrop.size())) {
            createWorksheet(con);
            setWorksheetId(con);
            setStoreInfo();
            insertWorksheetStore(con);
            insertWorksheetHeader(con);
            insertWorksheetEntries(con);
        }
    }

    //Replaces StoreID with String of store name
    public void setStore() {
        if(this.storeId == 2) {
            this.store = "Pier";
        }
        else if(this.storeId == 3) {
            this.store = "Jefferson";
        }
        else if(this.storeId == 4) {
            this.store = "Chestnut";
        }
        else if(this.storeId == 1) {
            this.store = "Warehouse";
        }
    }

    //Queries database to get the highest Inventory Transfer worksheetID
    public void setWorksheetId(Connection con) throws SQLException {
        String query = "SELECT MAX(Worksheet.ID) as 'MaxWorkSheet' FROM Worksheet " +
                "WHERE Worksheet.Style = 330";
        ResultSet rs = viewTable(con, query);
        while(rs.next()) {
            this.worksheetId = rs.getInt(1);
        }
        setPoNumber();
    }

    //Sets the PO Number equal to the WorksheetID
    public void setPoNumber() {
        this.poNumber = Integer.toString(this.worksheetId);
    }

    //Creates title for the Transfer/Worksheet
    public void setPoTitle() {
        this.poTitle = "Phone Order | " + this.transNumber + " | " + this.store;
        //System.out.println(poTitle);
    }


    //Creates a new worksheet in the database
    public void createWorksheet(Connection con) throws SQLException {
        String query = "INSERT INTO Worksheet " +
                "(Style, EffectiveDate, [Status], Notes, Title, FromDate) " +
                "VALUES (330, GETDATE(), 2, 'Phone Order - " + this.store + "',  '" + this.poTitle + "', '2000-05-22 00:00:00.000')";
        runQuery(con, query);
    }


    //Creates the required entries in the WorsheetStore table
    public void insertWorksheetStore(Connection con) throws SQLException {
        String query = "INSERT INTO WorksheetStore " +
                "(WorksheetID, StoreID, [Status], DateProcessed, Originator)" +
                "VALUES(" + this.worksheetId + "," + this.storeId + ", 0, null, 0), " +
                "(" + this.worksheetId + ", 1, 0, null, -1)";
        runQuery(con, query);
    }


    //Converts the storeID to the appropriate String containing the store address
    public void setStoreInfo() {
        if(this.storeId == 2) {
            this.storeInfo = "'L - Pier 39 \n" +
                    "Pier 39 \n" +
                    "Lower Level \n" +
                    "San Francisco, CA USA 94133 \n" +
                    "\n" +
                    " Phone: 415-986-4088 \n" +
                    "Fax: '";
        }
        else if(this.storeId == 3) {
            this.storeInfo = "'L - Jefferson \n" +
                    "275 Jefferson Street \n" +
                    "San Francisco, CA USA 94133 \n" +
                    "\n" +
                    " Phone:  \n" +
                    "Fax: '";
        }
        else if(this.storeId == 4) {
            this.storeInfo = "'L - Chestnut \n" +
                    "2601 Chestnut \n" +
                    "San Francisco, CA  \n" +
                    "\n" +
                    " Phone: 415-292-7790 \n" +
                    "Fax: 415-292-7791'";
        }
        else if(this.storeId == 1) {
            this.storeInfo = "'L - Warehouse \n" +
                    "Pier 50 \n" +
                    "San Francisco, CA 94112 USA \n" +
                    "Tel: 415-371-1644 \n" +
                    "Fax: 415-371-1644' \n";
        }
    }

    //Inserts the required entries into WorksheetHeader_PurchaseOrder
    public void insertWorksheetHeader(Connection con) throws SQLException {
        String query = "INSERT INTO WorksheetHeader_PurchaseOrder\n" +
                "(WorksheetID, PONumber, DateCreated, [To], ShipTo, Requisitioner, ShipVia, FOBPoint, Terms, TaxRate, Shipping, Freight, RequiredDate, ConfirmingTo,\n" +
                "Remarks, SupplierID, CurrencyID, ExchangeRate, OtherFees, MasterPOID)\n" +
                "Values(" + this.worksheetId + ", " + this.poNumber + ", GETDATE(), 'L - Warehouse\n" +
                "Pier 50\n" +
                "San Francisco, CA 94112 USA\n" +
                "Tel: 415-371-1644\n" +
                "Fax: 415-371-1644\n" +
                "', " + this.storeInfo +
                ", '', '', '', '', 0, 0.00, '', GETDATE(), '', 'Phone Order - " + this.store + "', 0, 0, 1, 0.00, 0)";
        runQuery(con, query);
    }

    //Inserts a row for each line item into the WorksheetEntries
    public void insertWorksheetEntries(Connection con) throws SQLException {
        List<String[]> lineItems = td.getLineItems();
        List<Integer> lineItemItemId = new ArrayList<>();
        List<Double> lineItemCost = new ArrayList<>();
        String itemQuery;
        //Finds the ItemID based off of the ItemLookupCode
        for(int i = 0; i < lineItems.size(); i++) {
            itemQuery = "SELECT TOP 1 Item.ID FROM Item\n" +
                    " WHERE Item.ItemLookupCode = '" + td.getLookupCode(i) + "'";
            ResultSet rs = viewTable(con, itemQuery);
            while(rs.next()) {
                lineItemItemId.add(rs.getInt(1));
            }
        }
        String itemCostQuery;
        //Finds the Cost based off of the ItemLookupCode
        for(int i = 0; i < lineItems.size(); i++) {
            itemCostQuery = "SELECT TOP 1 Item.Cost FROM Item\n" +
                    " WHERE Item.ItemLookupCode = '" + td.getLookupCode(i) + "'";
            ResultSet rs = viewTable(con, itemCostQuery);
            while(rs.next()) {
                lineItemCost.add(rs.getDouble(1));
            }
        }
        //Uses the CheckInventory class to find the line item rows that have insufficient stock in the WH
        addItemsToDrop(lineItems);

        //Builds the query for inserting each line item row into the database
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO Worksheet_PurchaseOrder\n" +
                "(WorksheetID, ItemDescription, QuantityOrdered, ItemID, DetailID, OrderNumber, Price, StoreID, TaxRate)\n" +
                "VALUES");

        for (int i = 0; i < lineItems.size(); i++) {
            if(!itemLinesToDrop.contains(i) || !(itemLinesTook.contains(i))) {
                query.append("(" + this.worksheetId + ", '" + td.getDescription(i) + "', " + (int) td.getQty(i) + ", " + lineItemItemId.get(i) + ", 0, '', " + lineItemCost.get(i) + ", 0, 0),\n");
            }
        }
        //Removes comma from the query if last entry
        if(query.charAt(query.length() - 2) == ',') {
            query.deleteCharAt(query.length() - 2);
        }
        if(!lineItemItemId.isEmpty() || !(query.charAt(query.length()-1) == 'S')) {
            runQuery(con, query.toString());
            //System.out.println(query);
        }
    }

    //Uses the CheckInventory class to find the line item rows that have insufficient stock in the WH
    private void addItemsToDrop(List<String[]> lineItems) throws SQLException {
        for (int i = 0; i < lineItems.size(); i++) {
            CheckInventory ci = new CheckInventory(con, this.td);
            int qty = ci.getWHQty()[i] - (int) this.td.getQty(i);
            if (qty < 0) {
                itemLinesToDrop.add(i);
            }
            if(td.getComment(i).toLowerCase().matches("cc(.*)")) {
                itemLinesTook.add(i);
            }
        }
    }


    //Getters
    public List<Integer> getItemLinesToDrop() {
        return this.itemLinesToDrop;
    }

    public List<Integer> getItemLinesTook() {
        return this.itemLinesTook;
    }

    public int getWorksheetId() {
        return this.worksheetId;
    }

}
