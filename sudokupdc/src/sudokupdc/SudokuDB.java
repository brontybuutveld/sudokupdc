package sudokupdc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SudokuDB {

    private final DBManager dbManager;
    private final Connection conn;
    private Statement statement;
    
    public SudokuDB() {
        dbManager = new DBManager();
        conn = dbManager.getConnection();
    }

    public void createPuzzleTable() {
        String createTable = 
                "CREATE TABLE PUZZLE ("
                    + "ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "DATA VARCHAR(81),"
                    + "SOL VARCHAR(81)"
                + ")";
        
        dbManager.updateDB(createTable);
    }
    
    public void deletePuzzleTable() {
        if (isTable("PUZZLE")) {
            dbManager.updateDB("DROP TABLE PUZZLE");
        }
    }
    
    public void createMoveTable() {
        String createTable = 
                "CREATE TABLE MOVE ("
                    + "move_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "puzzle_id INT NOT NULL,"
                    + "row INT NOT NULL,"
                    + "col INT NOT NULL,"
                    + "value INT NOT NULL,"
                    + "prev INT,"
                    + "action_type VARCHAR(10),"
                    + "FOREIGN KEY (puzzle_id) REFERENCES PUZZLE(id)"
                + ")";
        
        dbManager.updateDB(createTable);
    }
    
    public void deleteMoveTable() {
        if (isTable("MOVE")) {
            dbManager.updateDB("DROP TABLE MOVE");
        }
    }
    
    public void insertMoveTable(int id, int row, int col, int val, int prev) {
        if (!isTable("MOVE")) {
            createMoveTable();
        }
        
        String record = "INSERT INTO MOVE (PUZZLE_ID, ROW, COL, VALUE, PREV, ACTION_TYPE) "
                + "VALUES ("+ id +", "+ row +", "+ col +", "+ val +", "+ prev +", 'DONE')";
        dbManager.updateDB(record);
    }
    
    public int insertPuzzleTable(int[][] data, int[][] sol) {
        if (!isTable("PUZZLE")) {
            createPuzzleTable();
        }
        
        String Data = "", Sol = "";
        for (int[] row : data) {
            for (Integer col : row) {
                Data += String.valueOf(col);
            }
        }
        for (int[] row : sol) {
            for (Integer col : row) {
                Sol += String.valueOf(col);
            }
        }
        
        String record = "INSERT INTO PUZZLE (DATA, SOL) VALUES ('"+ Data +"', '"+ Sol +"')";
        dbManager.updateDB(record);
        
        String query = "SELECT * FROM PUZZLE WHERE ID=(SELECT max(ID) FROM PUZZLE)";
        ResultSet rs = dbManager.queryDB(query);
        try {
            while (rs.next()) {
                System.out.println(rs.getInt("ID"));
                System.out.println(rs.getString("DATA"));
                System.out.println(rs.getString("SOL"));
                return rs.getInt("ID");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(SudokuDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public int[] undo(int id) {
        int[] ret = new int[3];
        String query = "SELECT MOVE_ID, PREV, ROW, COL FROM MOVE "
                + "WHERE MOVE_ID=(SELECT max(MOVE_ID) FROM MOVE WHERE PUZZLE_ID="+ id +" AND ACTION_TYPE='DONE')";
        ResultSet rs = dbManager.queryDB(query);
        try {
            if (rs.next()) {
                ret[0] = rs.getInt("PREV");
                ret[1] = rs.getInt("ROW");
                ret[2] = rs.getInt("COL");
                System.out.println(ret[0] +" "+ ret[1] +" "+ ret[2]);
                String update = "UPDATE MOVE SET ACTION_TYPE='UNDONE' WHERE MOVE_ID="+ rs.getInt("MOVE_ID");
                dbManager.updateDB(update);
            } else {
                ret[0] = -1;
                return ret;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SudokuDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
        public int[] redo(int id) {
        int[] ret = new int[3];
        String query = "SELECT MOVE_ID, VALUE, ROW, COL FROM MOVE "
                + "WHERE MOVE_ID=(SELECT min(MOVE_ID) FROM MOVE WHERE PUZZLE_ID="+ id +" AND ACTION_TYPE='UNDONE')";
        ResultSet rs = dbManager.queryDB(query);
        try {
            if (rs.next()) {
                ret[0] = rs.getInt("VALUE");
                ret[1] = rs.getInt("ROW");
                ret[2] = rs.getInt("COL");
                System.out.println(ret[0] +" "+ ret[1] +" "+ ret[2]);
                String update = "UPDATE MOVE SET ACTION_TYPE='DONE' WHERE MOVE_ID="+ rs.getInt("MOVE_ID");
                dbManager.updateDB(update);
            } else {
                ret[0] = -1;
                return ret;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SudokuDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /*public ResultSet getWeekSpecial() {
        String query = "SELECT TITLE, PRICE, DISCOUNT FROM BOOK, PROMOTION WHERE BOOK.CATEGORY=PROMOTION.CATEGORY";
        ResultSet rs = dbManager.queryDB(query);
        
        return (rs);
    }*/
    
    public boolean isTable(String tableName) {
        boolean tableExists = false;
        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            ResultSet rs = dbMetaData.getTables(null, null, tableName.toUpperCase(), null);
            if (rs.next()) {
                tableExists = true;
            }

            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(SudokuDB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tableExists;
    }

    /*public void createWeekSpecialTable(ResultSet rs) {
        try {
            if (isTable("WEEKSPECIAL")) {
                dbManager.updateDB("DROP TABLE WEEKSPECIAL");
            }
            String createTable = "CREATE TABLE WEEKSPECIAL (TITLE VARCHAR(50), SPECIALPRICE FLOAT)";
            dbManager.updateDB(createTable);
            
            while (rs.next()) {
                int discount = rs.getInt("DISCOUNT");
                float price = rs.getFloat("PRICE");
                String title = rs.getString("TITLE");

                Float specialPrice = (1 - (discount / 100.0f)) * price;
                String insertRecords = "INSERT INTO WEEKSPECIAL (TITLE, SPECIALPRICE) VALUES ('" + title + "', " + specialPrice + ")";

                dbManager.updateDB(insertRecords);
            }
            
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(SudokuDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
}