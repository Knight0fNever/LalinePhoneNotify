package com.laline;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        String emailUsername = "info@lalineusa.com";
        String emailPassword = "Lalineinfo";
        String emailTo = "jairb@lalineusa.com,joshc@lalineusa.com,dianar@lalineusa.com";
        String testEmailTo = "joshc@lalineusa.com";
        String emailFrom = "info@lalineusa.com";
        String emailBcc = "";
        SQL sql = new SQL("jdbc:sqlserver://LALINEHQ\\SQLEXPRESS;databaseName=Laline HQ", "sa", "Zcsf4119!");
        Connection con = sql.getCon();
        Filer  fl = new Filer(con);



        /*
        //Testing
        TransactionDetails tdTest = new TransactionDetails(con, 45125, 4);
        ShippingInfo siTest = new ShippingInfo(con, 4, 45125);
        CheckInventory ciTest = new CheckInventory(con, tdTest);
        CreateTransfer ctTest = new CreateTransfer(con, 45125, 4, tdTest);
        Email eTest = new Email(con, testEmailTo, emailFrom, emailBcc, tdTest.getStore(), emailUsername, emailPassword, siTest, tdTest, ctTest.getWorksheetId());
        eTest.eSend(ctTest.getItemLinesToDrop(), ctTest.getItemLinesTook());
        */



        //Main loop for Pier
        if(fl.PierNewTrans()) {
            TransactionDetails td = new TransactionDetails(con, fl.getpNewTrans(), 2);
            ShippingInfo si = new ShippingInfo(con, 2, fl.getpNewTrans());
            CheckInventory ci = new CheckInventory(con, td);
            CreateTransfer ct = new CreateTransfer(con, fl.getpNewTrans(), 2, td);
            Email e = new Email(con, emailTo + ",corinaf@lalineusa.com,pier39@lalineusa.com", emailFrom, emailBcc, td.getStore(), emailUsername, emailPassword, si, td, ct.getWorksheetId());
            e.eSend(ct.getItemLinesToDrop(), ct.getItemLinesTook());
        }

        //Main loop for Jefferson
        if(fl.JeffersonNewTrans()) {
            TransactionDetails td = new TransactionDetails(con, fl.getjNewTrans(), 3);
            ShippingInfo si = new ShippingInfo(con, 3, fl.getjNewTrans());
            CheckInventory ci = new CheckInventory(con, td);
            CreateTransfer ct = new CreateTransfer(con, fl.getjNewTrans(), 3, td);
            Email e = new Email(con, emailTo + ",jefferson@lalineusa.com", emailFrom, emailBcc, td.getStore(), emailUsername, emailPassword, si, td, ct.getWorksheetId());
            e.eSend(ct.getItemLinesToDrop(), ct.getItemLinesTook());
        }

        //Main loop for Chestnut
        if(fl.ChestnutNewTrans()) {
            TransactionDetails td = new TransactionDetails(con, fl.getcNewTrans(), 4);
            ShippingInfo si = new ShippingInfo(con, 4, fl.getcNewTrans());
            CheckInventory ci = new CheckInventory(con, td);
            CreateTransfer ct = new CreateTransfer(con, fl.getcNewTrans(), 4, td);
            Email e = new Email(con, emailTo + ",chestnut@lalineusa.com,eugenial@lalineusa.com", emailFrom, emailBcc, td.getStore(), emailUsername, emailPassword, si, td, ct.getWorksheetId());
            e.eSend(ct.getItemLinesToDrop(), ct.getItemLinesTook());
        }
    }
}