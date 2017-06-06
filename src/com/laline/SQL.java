package com.laline;

import java.sql.*;

public class SQL {

    private String conString;
    private Connection con;

    public SQL(String conStringInit, String uName, String uPass) throws SQLException {
        conString = conStringInit;
        this.initializeCon(uName, uPass);
    }

    public String getConString() {
        return conString;
    }

    public void setConString(String con) {
        conString = con;
    }

    public void initializeCon(String uName, String uPass) throws SQLException {
        con = DriverManager.getConnection(this.getConString(), uName, uPass);
    }

    public Connection getCon() {
        return con;
    }

    //Takes query and connects to SQL DB to run query. Places result of query in ResultSet rs
    public static ResultSet viewTable(Connection con, String query) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {

        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return rs;
    }

    public static void runQuery(Connection con, String query) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
        if(stmt != null) {
            stmt.close();
        }
    }
}
