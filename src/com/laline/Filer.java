package com.laline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.laline.SQL.viewTable;

public class Filer {

    private int cOldTrans;
    private int cNewTrans;
    private int pOldTrans;
    private int pNewTrans;
    private int jOldTrans;
    private int jNewTrans;
    private String path = ".\\JavaNotifier.txt";
    File fac = new File(path);


    public Filer(Connection con) {
        List<String[]> maxTransNumber = maxTransNum(con);
        if (!fac.exists()) {
            try {
                fac.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeTrans(maxTransNumber);
        }
        setcOldTrans(Integer.parseInt(readTrans().get(2)[1]));
        setjOldTrans(Integer.parseInt(readTrans().get(1)[1]));
        setpOldTrans(Integer.parseInt(readTrans().get(0)[1]));
        setcNewTrans(con);
        setjNewTrans(con);
        setpNewTrans(con);
        writeTrans(maxTransNumber);
    }

    //Writes to File
    public void writeTrans(List<String[]> transNumber) {
        try {
            FileWriter write = new FileWriter(fac);
            if (!fac.exists()) {
                fac.createNewFile();
            }
            for(int i = 0; i < transNumber.size(); i++) {
                for(int j = 0; j < transNumber.get(i).length; j++) {
                    if(j == 0) {
                        write.write((transNumber.get(i)[j]) + ",");
                    }
                    else {
                        write.write(transNumber.get(i)[j]);
                    }
                }
                write.write(System.getProperty( "line.separator" ));
            }
            write.flush();
            write.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Reads from file
    public List<String[]> readTrans() {
        List<String[]> result = new ArrayList<>();
        String[] entries = new String[3];
        try {
            Scanner scanner = new Scanner(this.fac);
            for(int i = 0; scanner.hasNextLine(); i++) {
                entries[i] = scanner.nextLine();
                result.add(entries[i].split(","));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Returns highest transaction number as an int
    public List<String[]> maxTransNum(Connection con) {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<String[]> table = new ArrayList<>();
        try {
            String query = "SELECT Shipping.StoreID, MAX(Shipping.TransactionNumber) as 'MaxTransNumber' FROM Shipping" +
                    " WHERE Shipping.StoreID IN (2, 3, 4)" +
                    " GROUP BY Shipping.StoreID" +
                    " ORDER BY Shipping.StoreID";
            ResultSet rs = viewTable(con, query);
            int nCol = rs.getMetaData().getColumnCount();

            while( rs.next()) {
                String[] row = new String[nCol];
                for( int iCol = 1; iCol <= nCol; iCol++ ){
                    Object obj = rs.getObject( iCol );
                    row[iCol-1] = (obj == null) ?null:obj.toString();
                }
                table.add( row );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {

        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return table;
    }

    public boolean checkComment(Connection con, int transNumber, int store) throws SQLException {
        String query = "SELECT [Transaction].Comment FROM [Transaction]\n" +
                " WHERE [Transaction].TransactionNumber = " + transNumber + " AND [Transaction].StoreID = " + store;
        String result;
        ResultSet rs = viewTable(con, query);
        if(!rs.next()) {
            return false;
        }
        result = rs.getString(1);
        System.out.println(result);
        if(result.contains("phone")) {
            return true;
        }
        return false;
    }


    public boolean PierNewTrans() {
        if(this.pNewTrans > this.pOldTrans) {
            return true;
        }
        return false;
    }

    public boolean JeffersonNewTrans() {
        if(this.jNewTrans > this.jOldTrans) {
            return true;
        }
        return false;
    }

    public boolean ChestnutNewTrans() {
        if(this.cNewTrans > this.cOldTrans) {
            return true;
        }
        return false;
    }

    private void setjOldTrans(int n) {
        this.jOldTrans = n;
    }

    private void setjNewTrans(Connection con) {
        this.jNewTrans = Integer.parseInt((maxTransNum(con).get(1)[1]));
    }

    private void setcOldTrans(int n) {
        this.cOldTrans = n;
    }

    private void setcNewTrans(Connection con) {
        this.cNewTrans = Integer.parseInt((maxTransNum(con).get(2)[1]));
    }

    private void setpOldTrans(int n) {
        this.pOldTrans = n;
    }

    private void setpNewTrans(Connection con) {
        this.pNewTrans = Integer.parseInt((maxTransNum(con).get(0)[1]));
    }

    public int getjOldTrans() {
        return this.jOldTrans;
    }

    public int getjNewTrans() {
        return this.jNewTrans;
    }

    public int getcOldTrans() {
        return this.cOldTrans;
    }

    public int getcNewTrans() {
        return this.cNewTrans;
    }

    public int getpOldTrans() {
        return this.pOldTrans;
    }

    public int getpNewTrans() {
        return this.pNewTrans;
    }
}