/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.TransactionOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



public class MM190117_TransactionOperations implements TransactionOperations {

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int buyerId) {
        Connection conn = DB.getInstance().getConnection();
        String checkQuery = "select * from kupac where idkli = ?";
        String query = "select sum(Cena) from Transakcija T where T.IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);PreparedStatement ps2 = conn.prepareStatement(checkQuery);){
                ps2.setInt(1, buyerId);
                ps.setInt(1, buyerId);
                ResultSet rs2 = ps2.executeQuery();
                if (!rs2.next()) {
                    return BigDecimal.valueOf(-1);
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getBigDecimal(1) == null ? BigDecimal.valueOf(0) : rs.getBigDecimal(1);
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return BigDecimal.valueOf(-1);
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int shopId) {
        Connection conn = DB.getInstance().getConnection();
        String checkQuery = "select * from prodavnica where idkli = ?";
        String query = "select sum(Cena) from Transakcija T where T.IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);PreparedStatement ps2 = conn.prepareStatement(checkQuery);){
                ps2.setInt(1, shopId);
                ps.setInt(1, shopId);
                ResultSet rs2 = ps2.executeQuery();
                if (!rs2.next()) {
                    return BigDecimal.valueOf(-1);
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getBigDecimal(1) == null ? new BigDecimal("0").setScale(3) : rs.getBigDecimal(1);
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return BigDecimal.valueOf(-1);
    }

    @Override
    public List<Integer> getTransationsForBuyer(int buyerId) {
        List<Integer> transactions = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdT from Transakcija T where T.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) transactions.add(rs.getInt(1));
            rs.close();
            return transactions;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int getTransactionForBuyersOrder(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select T.idT from transakcija T join porudzbina P on T.idpor=P.idpor\n" +
                "where T.IdKli=P.IdKli and P.IdPor = ?";
         try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt(1); 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int orderId, int shopId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select T.idT from transakcija T join Prodavnica P on P.IdKli=T.Idkli where P.IdKli = ? and T.idPor = ?";
         try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, shopId);
                ps.setInt(2, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt(1); 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int shopId) {
        List<Integer> transactions = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdT from Transakcija T where T.IdKli = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) transactions.add(rs.getInt(1));
            rs.close();
            if(transactions.isEmpty()) return null;
            return transactions;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Calendar getTimeOfExecution(int transactionId) {
        Connection conn = DB.getInstance().getConnection();
        Calendar cal = Calendar.getInstance();
        String query = "select Vreme from Transakcija where IdT = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    cal.setTime(rs.getDate(1));
                  //  System.out.println("Vreme izvrsenja transakcije: " + cal);
                    return cal;
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select Cena from Transakcija where IdPor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int shopId, int orderId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select Cena from Transakcija where IdPor = ? and IdKli = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ps.setInt(2, shopId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public BigDecimal getTransactionAmount(int transactionId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select Cena from Transakcija where IdT = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public BigDecimal getSystemProfit() {
        Connection conn = DB.getInstance().getConnection();
        String buyerQuery = "select sum(T.cena) from transakcija T join kupac K on K.IdKli = T.IdKli\n" +
                        "join Porudzbina P on P.idPor = T.idPor where P.Status='Arrived'";
        String shopQuery = "select sum(T.cena) from transakcija T join prodavnica Pro on Pro.IdKli = T.IdKli\n" +
                        "join Porudzbina P on P.idPor = T.idPor where P.Status='Arrived'";
        try (PreparedStatement psBuyer = conn.prepareStatement(buyerQuery);
                PreparedStatement psShop = conn.prepareStatement(shopQuery)){
                ResultSet rsBuyer = psBuyer.executeQuery();
                ResultSet rsShop = psShop.executeQuery();
                if (rsBuyer.next() && rsShop.next()){
                    if(rsShop.getBigDecimal(1) == null) return new BigDecimal("0").setScale(3);
                    BigDecimal profit = rsBuyer.getBigDecimal(1).add(rsShop.getBigDecimal(1).negate());
                    return profit;
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }
    
}
