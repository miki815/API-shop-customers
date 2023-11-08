/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.Calendar;
import rs.etf.sab.operations.GeneralOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PC
 */
public class MM190117_GeneralOperations implements GeneralOperations{
    
    private Calendar time;

    public MM190117_GeneralOperations() {
        this.time = Calendar.getInstance();
    }
    
    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar newTime) {
       // this.time.set(newTime.get(Calendar.YEAR), newTime.get(Calendar.MONTH), newTime.get(Calendar.DAY_OF_MONTH));
        time = (Calendar)newTime.clone();
    }

    @Override
    public void setInitialTime(Calendar initialTime) {
        this.setTime(initialTime);
    }

    @Override
    public Calendar time(int days) {
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Porudzbina SET Status='arrived' where DatumPrijema < ?";
        Calendar oldTime = this.getTime();
        this.getTime().add(Calendar.DAY_OF_MONTH, days);
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setDate(1, new Date(this.getTime().getTimeInMillis()));
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return oldTime;
    }

    @Override
    public Calendar getCurrentTime() {
        return this.getTime();
    }

    @Override
    public void eraseAll() {
        Connection conn = DB.getInstance().getConnection();
        String deleteQuery = "delete from transakcija;\n" +
                            "delete from deoPorudzbine;\n" +
                            "delete from porudzbina;\n" +
                            "delete from artikal;\n" +
                            "delete from prodavnica;\n" +
                            "delete from kupac;\n" +
                            "delete from klijent;\n" +
                            "delete from linija;\n" +
                            "delete from grad;\n" +
                            "dbcc checkident ('transakcija', RESEED, 0);\n" +
                            "dbcc checkident ('porudzbina', RESEED, 0);\n" +
                            "dbcc checkident ('artikal', RESEED, 0);\n" +
                            "dbcc checkident ('klijent', RESEED, 0);\n" +
                            "dbcc checkident ('linija', RESEED, 0);\n" +
                            "dbcc checkident ('grad', RESEED, 0);\n" +
                            "dbcc checkident ('deoPorudzbine', RESEED, 0);";
        
        try(PreparedStatement ps = conn.prepareStatement(deleteQuery)){
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void main(String[] args) {
        MM190117_GeneralOperations general = new MM190117_GeneralOperations();
        MM190117_CityOperations city = new MM190117_CityOperations();
        MM190117_OrderOperations order = new MM190117_OrderOperations(general);
        MM190117_ArticleOperations article = new MM190117_ArticleOperations();
        MM190117_ShopOperations shop = new MM190117_ShopOperations();
        MM190117_TransactionOperations transaction = new MM190117_TransactionOperations();
        MM190117_BuyerOperations buyerOps = new MM190117_BuyerOperations();
        general.eraseAll();
        
        final Calendar time = Calendar.getInstance();
        time.clear();
        time.set(2018, 0, 1);
        general.setInitialTime(time);
        Calendar currentTime = general.getCurrentTime();
        System.out.println(time);
        System.out.println(currentTime);
        System.out.println(time.equals(currentTime));
        general.time(40);
        currentTime = general.getCurrentTime();
        final Calendar newTime = Calendar.getInstance();
        newTime.clear();
        newTime.set(2018, 1, 10);
        System.out.println(newTime.equals(currentTime));
     }
    
}
