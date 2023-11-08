/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.ShopOperations;



public class MM190117_ShopOperations implements ShopOperations {

    @Override
    public int createShop(String name, String cityName) {
        int shopId = -1;
        int cityId = -1;
        Connection conn = DB.getInstance().getConnection();
        String query = "INSERT INTO Klijent(IdGra) Values(?)";
        String query2 = "INSERT INTO Prodavnica(Naziv, IdKli) Values(?,?)";
        String queryCity = "SELECT IdGra FROM Grad G WHERE G.naziv = ?";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement psCity = conn.prepareStatement(queryCity)){
            psCity.setString(1, cityName);
            ResultSet rsCity = psCity.executeQuery();
            if (rsCity.next()) cityId = rsCity.getInt(1);
            else return -1;           
            ps.setInt(1, cityId);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) {
                    System.out.println("Kreirana je nova prodavnica " + rs.getInt(1));
                    shopId = rs.getInt(1);
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        try(PreparedStatement ps = conn.prepareStatement(query2,PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setString(1, name);
            ps.setInt(2, shopId);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) return shopId;   
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int setCity(int shopId, String cityName) {
        int cityId = -1;
        Connection conn = DB.getInstance().getConnection();
        String queryCity = "select IdGra from Grad G where G.naziv = ?";
        String query = "select * from Klijent join Prodavnica on Klijent.IdKli=Prodavnica.IdKli where Prodavnica.IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query,
             ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                 PreparedStatement psCity = conn.prepareStatement(queryCity);){
                
                psCity.setString(1, cityName);
                ResultSet rsCity = psCity.executeQuery();
                if (rsCity.next()) cityId = rsCity.getInt(1);
                else return -1;                      
                ps.setInt(1, shopId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    rs.updateInt("IdGra", cityId);
                    rs.updateRow();
                    System.out.println("Izmenjen je grad prodavnice");
                    return 1;
                } 
                else return -1;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }

    @Override
    public int getCity(int shopId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdGra FROM Klijent K JOIN Prodavnica P ON K.IdKli = P.IdKli WHERE K.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, shopId);
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int setDiscount(int shopId, int discount) {
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Prodavnica SET Popust = ? WHERE IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, discount);
            ps.setInt(2, shopId);
            int success = ps.executeUpdate();
            if(success == 1) return 1;
            else return -1;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int increaseArticleCount(int articleId, int increment) {
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Artikal SET Kolicina = Kolicina + ? WHERE IdArt = ?";
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, increment);
            ps.setInt(2, articleId);
            int success = ps.executeUpdate();
            if(success == 1) return 1;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int getArticleCount(int articleId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Kolicina FROM Artikal A WHERE A.IdArt = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, articleId);
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getArticles(int shopId) {
        List<Integer> articles = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdArt from Artikal A where A.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                articles.add(rs.getInt(1));
            }
            rs.close();
            return articles;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int getDiscount(int shopId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Popust FROM Prodavnica P WHERE P.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, shopId);
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
}
