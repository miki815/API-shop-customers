/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.List;
import rs.etf.sab.operations.CityOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PC
 */
public class MM190117_CityOperations implements CityOperations {

    @Override
    public int createCity(String name) { // TODO set name unique
        Connection conn = DB.getInstance().getConnection();
        String insertQuery = "insert into grad(Naziv) values(?)";        
        try(PreparedStatement ps = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);){
            ps.setString(1, name);  
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) return rs.getInt(1);
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getCities() {
        List<Integer> cities = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdGra from Grad";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                cities.add(rs.getInt(1));
            }
            return cities;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int connectCities(int i1, int i2, int i) {
        Connection conn = DB.getInstance().getConnection();
        String checkQuery = "select * from linija G where ((G.IdGra1=? AND G.IdGra2=?) OR (G.IdGra1=? AND G.IdGra2=?))";
        String lineQuery = "insert into linija (udaljenost, idGra1, idGra2) values(?,?,?)";
        try(PreparedStatement ps = conn.prepareStatement(checkQuery);){
            ps.setInt(1, i1);
            ps.setInt(2, i2);
            ps.setInt(3, i2);
            ps.setInt(4, i1);    
            try(ResultSet rs = ps.executeQuery();){
                if (rs.next()) {
                    System.out.println("Linija " + i1 + "-" + i2 + "vec postoji.");
                    return -1;
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        try(PreparedStatement ps = conn.prepareStatement(lineQuery, PreparedStatement.RETURN_GENERATED_KEYS);){
            ps.setInt(1, i);
            ps.setInt(2, i1);
            ps.setInt(3, i2);    
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) return rs.getInt(1);
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
    }

    @Override
    public List<Integer> getConnectedCities(int cityId) {
        List<Integer> connections = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select case when L.IdGra1 = ? then L.IdGra2 when L.IdGra2 = ? then L.IdGra1 end as lines from linija L";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, cityId);
            ps.setInt(2, cityId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt(1) != 0) connections.add(rs.getInt(1));
            }
            return connections;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getShops(int cityId) {
        List<Integer> shops = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select P.IdKli from Prodavnica P join Klijent K on P.IdKli = K.Idkli where K.IdGra = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, cityId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                shops.add(rs.getInt(1));
            }
            return shops;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
        
 /*   public static void main(String[] args) {
        System.out.println("test buyer");
        MM190117_BuyerOperations buyer = new MM190117_BuyerOperations();
        CityOperations cityOperations = new MM190117_CityOperations();
     //   buyer.createBuyer("miki", 1);
     //   buyer.createBuyer("mica", 2);
        buyer.getCity(3);   
        buyer.increaseCredit(2, BigDecimal.valueOf(50));
     //   buyer.createOrder(3);
     //   buyer.createOrder(1);
        List<Integer> orders = buyer.getOrders(3);
        System.out.println("Porudzbine: ");
        for(int i = 0;i < orders.size();i++) System.out.println(orders.get(i));
        System.out.println("Kredit: ");
        System.out.println(buyer.getCredit(2));
        System.out.println(cityOperations.connectCities(50, 3, 4));
     //   System.out.println("Kreiranje gradova");
     //   System.out.println(cityOperations.createCity("Nis"));
     //   System.out.println(cityOperations.createCity("Nis"));
     //   System.out.println(cityOperations.createCity("Novi Sad"));
        System.out.println("Ispis gradova");
        System.out.println(cityOperations.getCities());
        System.out.println("Ispis prodavnica");
        System.out.println(cityOperations.getShops(1));  
        System.out.println("Konekcije");
        System.out.println(cityOperations.getConnectedCities(1));
    }*/
}
