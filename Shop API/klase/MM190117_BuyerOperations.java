/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.BuyerOperations;


public class MM190117_BuyerOperations implements BuyerOperations{
    
    public int createBuyer​(String name, int cityId){
        int buyerId = -1;
        Connection conn = DB.getInstance().getConnection();
        String query = "INSERT INTO Klijent(IdGra) Values(?)";
        String query2 = "INSERT INTO Kupac(Ime, IdKli) Values(?,?)";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, cityId);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) {
                    System.out.println("Kreiran je novi kupac " + rs.getInt(1));
                    buyerId = rs.getInt(1);
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        try(PreparedStatement ps = conn.prepareStatement(query2,PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setString(1, name);
            ps.setInt(2, buyerId);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) {
                    return buyerId;
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
    }
    
    public int setCity​(int buyerId, int cityId){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Klijent join Kupac on Klijent.IdKli=Kupac.IdKli where Kupac.IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query,
             ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);){
                ps.setInt(1, buyerId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    rs.updateInt("IdGra", cityId);
                    rs.updateRow();
                    System.out.println("Izmenjen je grad kupca");
                    return 1;
                } 
                else return -1;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }
    
    public int getCity​(int buyerId){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Klijent join Kupac on Klijent.IdKli=Kupac.IdKli where Kupac.IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, buyerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("Grad kupca je " + rs.getInt("IdGra"));
                    return rs.getInt("IdGra");
                } 
                else return -1;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }
    
    public BigDecimal increaseCredit​(int buyerId, BigDecimal credit){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Klijent join Kupac on Klijent.IdKli=Kupac.IdKli where Kupac.IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query,
             ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);){
             ps.setInt(1, buyerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    rs.updateBigDecimal("Kredit", rs.getBigDecimal("Kredit").add(credit));
                    rs.updateRow();
                    System.out.println("Izmenjen je racun kupca " + buyerId);
                    return rs.getBigDecimal("Kredit");
                }   
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return credit;
    }
    
    public int createOrder​(int buyerId){
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into porudzbina(IdKli) select IdKli from Kupac where Kupac.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);){
            ps.setInt(1, buyerId);
            ps.executeUpdate();
            try( ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) {
                  //  System.out.println("Kreirana je nova porudzbina " + rs.getInt(1));
                    return rs.getInt(1) != 0 ? rs.getInt(1) : -1;
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public List<Integer> getOrders​(int buyerId){
        List<Integer> orders = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdPor from Porudzbina P where P.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                orders.add(rs.getInt(1));
            }
            rs.close();
            return orders;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public BigDecimal getCredit​(int buyerId){
        Connection conn = DB.getInstance().getConnection();
        String query = "select Kredit from Klijent K where K.IdKli = ?";
         try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getBigDecimal(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    
    public static void main(String[] args) {
        MM190117_GeneralOperations general = new MM190117_GeneralOperations();
        MM190117_CityOperations city = new MM190117_CityOperations();
        MM190117_OrderOperations order = new MM190117_OrderOperations(general);
        MM190117_ArticleOperations article = new MM190117_ArticleOperations();
        MM190117_ShopOperations shop = new MM190117_ShopOperations();
        MM190117_TransactionOperations transaction = new MM190117_TransactionOperations();
        //order.dikstraTest(1);
        Calendar cal = Calendar.getInstance();
        System.out.println(cal.getTime());
        general.setTime(cal);
       // transaction.getTimeOfExecution(1);
       // order.completeOrder(1);
        /*
        order.getFinalPrice(1);
        order.getDiscountSum(1);
        order.completeOrder(1);
        System.out.println(transaction.getAmmountThatBuyerPayedForOrder(1));
        System.out.println(transaction.getBuyerTransactionsAmmount(4));
        System.out.println("Profit: " + transaction.getSystemProfit());
        System.out.println("Transfet T1: " + transaction.getTransactionAmount(1));
        System.out.println("Transfet T2: " + transaction.getTransactionAmount(2));
        System.out.println("Test transactionId buyer: " + transaction.getTransactionForBuyersOrder(1));
        System.out.println("Test transactionId buyer: " + transaction.getTransactionForBuyersOrder(2));
        System.out.println("Test transactionId buyer: " + transaction.getTransactionForBuyersOrder(3));
        System.out.println("Test transactionId shop: " + transaction.getTransactionForShopAndOrder(1, 2));
        System.out.println("Test transactionId shop: " + transaction.getTransactionForShopAndOrder(1, 3));
        System.out.println("Test transactionId shop: " + transaction.getTransactionForShopAndOrder(66, 1));
        System.out.println("Buyer 1 Transactions: " + transaction.getTransationsForBuyer(1));
        System.out.println("Shop 2 Transactions: " + transaction.getTransationsForShop(2));
    */
     /*   general.eraseAll();

     
        // City
        System.out.println("---Testing City Operations---");
        int city1 = city.createCity("Kostolac");
        int city2 = city.createCity("Beograd");
        System.out.println(city1);
        System.out.println(city2);
        System.out.println(city.connectCities(city1, city2, 80));
     
        // Shop
        System.out.println("---Testing Shop Operations---");
        int shop1 = shop.createShop("Jabuka", "Kostolac");
        int shop2 = shop.createShop("Laguna", "Beograd");
        System.out.println(shop.createShop("M", "Test -1"));
        System.out.println(shop1);
        System.out.println(shop2);
        System.out.println("Grad prodavnice 2 je " + shop.getCity(2));
        shop.setCity(2, "Kostolac");
        System.out.println("Grad prodavnice 2 je " + shop.getCity(2));
        System.out.println("Popust prodavnice 2 je " + shop.getDiscount(2));
        shop.setDiscount(shop2, 20);
        System.out.println("Popust prodavnice 2 je " + shop.getDiscount(2));
        
        // Article
        System.out.println("---Testing Article Operations---");
        int article1 = article.createArticle(shop2, "Alhemicar", 800);
        int article2 = article.createArticle(shop2, "Majstor i Margarita", 1000);
        System.out.println(article1);
        System.out.println(article2);
        System.out.println("Artikli na stanju pre i nakon inkrementa");
        System.out.println(shop.getArticleCount(article1));
        shop.increaseArticleCount(article1, 15);
        System.out.println(shop.getArticleCount(article1));
        System.out.println("Artikli prodavnica 2 i 1:");
        System.out.println(shop.getArticles(shop2));
        System.out.println(shop.getArticles(shop1));

*/
        
     
     
        // Order
     //   System.out.println("---Testing Order Operations---");
     //   System.out.println(order.addArticle(1, 2, 5));
     //   System.out.println("Kupac");
     //   System.out.println(order.getBuyer(3));
     //   System.out.println("Poruceni artikli");
     //  System.out.println(order.getItems(4));
     //   System.out.println(order.getState(4));
        
        
    }
}
