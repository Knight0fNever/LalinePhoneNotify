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
        String p = "jfjTre tihn5mmvuiiTptpo elakn3iisg Flenoi#pos";
        SQL sql = new SQL("jdbc:sqlserver://LALINEHQ\\SQLEXPRESS;databaseName=Laline HQ", "app", p.substring(11, 20));
        Connection con = sql.getCon();
        Filer  fl = new Filer(con);




        /*
        //Testing
        //Transaction transTest = new Transaction(65428, 3, sql);
        //CreateTransfer ct = new CreateTransfer(con, transTest);
        //Email email = new Email(con, testEmailTo, emailFrom, emailBcc, transTest.getStoreId(), emailUsername, emailPassword, transTest, ct.getWorksheetId());
        //email.eSend();
        */






        //Main loop for Pier
        if(fl.PierNewTrans()) {
            Transaction transPier = new Transaction(fl.getpNewTrans(), 2, sql);
            CreateTransfer ct = new CreateTransfer(con, transPier);
            Email e = new Email(con, emailTo + ",corinaf@lalineusa.com,pier39@lalineusa.com", emailFrom, emailBcc, transPier.getStoreId(), emailUsername, emailPassword, transPier, ct.getWorksheetId());
            e.eSend();
        }

        //Main loop for Jefferson
        if(fl.JeffersonNewTrans()) {
            Transaction transJeff = new Transaction(fl.getjNewTrans(), 3, sql);
            CreateTransfer ct = new CreateTransfer(con, transJeff);
            Email e = new Email(con, emailTo + ",jefferson@lalineusa.com", emailFrom, emailBcc, transJeff.getStoreId(), emailUsername, emailPassword, transJeff, ct.getWorksheetId());
            e.eSend();
        }

        //Main loop for Chestnut
        if(fl.ChestnutNewTrans()) {
            Transaction transChestnut = new Transaction(fl.getcNewTrans(), 4, sql);
            CreateTransfer ct = new CreateTransfer(con, transChestnut);
            Email e = new Email(con, emailTo + ",chestnut@lalineusa.com,eugenial@lalineusa.com", emailFrom, emailBcc, transChestnut.getStoreId(), emailUsername, emailPassword, transChestnut, ct.getWorksheetId());
            e.eSend();
        }
    }
}