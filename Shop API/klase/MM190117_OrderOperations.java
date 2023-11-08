/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.OrderOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import rs.etf.sab.operations.GeneralOperations;



public class MM190117_OrderOperations implements OrderOperations {
    
    private GeneralOperations general;

    public GeneralOperations getGeneral() {
        return general;
    }

    public void setGeneral(GeneralOperations general) {
        this.general = general;
    }

    public MM190117_OrderOperations(GeneralOperations general) {
        this.general = general;
    }
    
    

    @Override
    public int addArticle(int orderId, int articleId, int count) {
        Connection conn = DB.getInstance().getConnection();
        String queryUpdate = "select idDeo, kolicina from deoPorudzbine K where K.IdArt = ? and K.IdPor = ?";
        String queryAvailable = "select kolicina from Artikal A where A.IdArt = ?";
        String queryInsert = "insert into deoPorudzbine(idArt,idPor,kolicina) values(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(queryAvailable,
             ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);){
                ps.setInt(1, articleId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) >= count) {
                    rs.updateInt(1, rs.getInt(1) - count);
                    rs.updateRow();
                    System.out.println("Dovoljno artikla " + articleId + " na stanju");
                } 
                else {
                    System.out.println("Nema dovoljno artikla " + articleId + " na stanju");
                    return -1;
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        try (PreparedStatement ps = conn.prepareStatement(queryUpdate,
             ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                PreparedStatement ps2 = conn.prepareStatement(queryInsert, PreparedStatement.RETURN_GENERATED_KEYS);){
                ps.setInt(1, articleId);
                ps.setInt(2, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    rs.updateInt("kolicina", rs.getInt("kolicina") + count);
                    rs.updateRow();
                    System.out.println("Povecan broj artikala u porudzbini");
                    return 1;
                } 
                else {
                    ps2.setInt(1, articleId);
                    ps2.setInt(2, orderId);
                    ps2.setInt(3, count);
                    ps2.executeUpdate();
                    try(ResultSet rs2 = ps2.getGeneratedKeys();){
                        if (rs2.next()) {
                            System.out.println("Dodat novi artikal u porudzbinu " + rs2.getInt(1));
                            return  rs2.getInt(1);
                        }
                    }        
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }

    @Override
    public int removeArticle(int orderId, int articleId) {
        Connection conn = DB.getInstance().getConnection();
        String deleteQuery = "delete from deoPorudzbine K where K.idArt = ? and K.idPor = ?";
        try(PreparedStatement ps = conn.prepareStatement(deleteQuery)){
            int success = ps.executeUpdate();
            if(success == 1) return 1;
            else return -1;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getItems(int orderId) {
        List<Integer> articles = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select idArt from deoPorudzbine K where K.IdPor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    articles.add(rs.getInt(1));
                } 
                rs.close();
                return articles;
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public int completeOrder(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        BigDecimal finalPrice = this.getFinalPrice(orderId);
        BigDecimal buyerCredit;
        int buyerId;
        String checkQuery = "select Kredit, k.IdKli from klijent k join porudzbina p on k.idkli = p.idkli where p.idpor = ?";
        String updateQuery = "select Status, DatumSlanja, DatumPrijema from porudzbina p where p.idpor = ?";
        String transactionQuery = "insert into transakcija(cena,vreme,idpor,idkli) values(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(checkQuery,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            PreparedStatement ps2 = conn.prepareStatement(updateQuery,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            PreparedStatement ps3 = conn.prepareStatement(transactionQuery);){
                ps.setInt(1, orderId);
                ps2.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                ResultSet rs2 = ps2.executeQuery();
                if (rs.next() && rs2.next()) {
                    buyerCredit = rs.getBigDecimal("kredit");
                    buyerId = rs.getInt("idKli");
                    if(buyerCredit.compareTo(finalPrice) < 0){
                        System.out.println("Kupac nema dovoljno novca za ostvarivanje porudzbine!");
                        return -1;
                    }
                    else if(rs2.getString("Status").equals("created") == false){
                        System.out.println("Porudzbina je vec kompletirana!");
                        return -1;
                    }
                    else{
                        Calendar cal = this.general.getCurrentTime();
                        Date sendDate = new Date(cal.getTimeInMillis());
                        int days = this.getOrderDays(orderId);
                        ps3.setBigDecimal(1, finalPrice);
                        finalPrice = finalPrice.multiply(BigDecimal.valueOf(-1));
                        rs.updateBigDecimal("Kredit", rs.getBigDecimal("kredit").add(finalPrice));
                        rs.updateRow();
                        rs2.updateString("Status", "sent");
                        rs2.updateDate("DatumSlanja", sendDate);
                      //  cal.add(Calendar.DATE, days);
                        rs2.updateDate("DatumPrijema", new Date(cal.getTimeInMillis()+days*24*3600*1000));
                        rs2.updateRow();
                        ps3.setDate(2, sendDate);
                        ps3.setInt(3, orderId);
                        ps3.setInt(4, buyerId);
                        ps3.executeUpdate();
                        rs.close();
                        rs2.close();
                        return 1;
                    }
                }                
                rs.close();
                rs2.close();
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return -1;
    }

    @Override
    public BigDecimal getFinalPrice(int orderId) { //TODO -1
        Connection conn = DB.getInstance().getConnection();
        String query = "{ call SP_FINAL_PRICE(?, ?) }";
        try ( CallableStatement cs = conn.prepareCall(query)) {
            cs.setInt(1, orderId);
            cs.setDate(2, new Date(this.general.getCurrentTime().getTimeInMillis()));
            ResultSet rs = cs.executeQuery();
            if(rs.next()){
                System.out.println("Ukupna cena je " + rs.getBigDecimal(1));
                return rs.getBigDecimal(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.valueOf(-1);
    }

    @Override
    public BigDecimal getDiscountSum(int orderId) { // TODO not completed
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT SUM(Deo.Kolicina * Art.Cena)\n" +
        "FROM DeoPorudzbine Deo JOIN Artikal Art ON Deo.IdArt = Art.IdArt WHERE Deo.IdPor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()){
                    BigDecimal regularPrice = rs.getBigDecimal(1);
                    BigDecimal finalPrice = this.getFinalPrice(orderId);
                    finalPrice = finalPrice.multiply(BigDecimal.valueOf(-1));
                    BigDecimal discount = regularPrice.add(finalPrice);
                    System.out.println("Ukupna usteda je " + discount);
                    rs.close();
                    return discount;
                };
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return BigDecimal.valueOf(-1);
    }

    @Override
    public String getState(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select Status from Porudzbina P where P.IdPor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return "";
    }

    @Override
    public Calendar getSentTime(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        Calendar sentTime = Calendar.getInstance();
        String query = "select DatumSlanja from Porudzbina P where P.IdPor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if(rs.getDate(1) != null){
                        sentTime.setTime(rs.getDate(1));
                        return sentTime;
                    }
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public Calendar getRecievedTime(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        Calendar arriveTime = Calendar.getInstance();
        String query = "select DatumPrijema from Porudzbina P where P.IdPor = ? and P.Status = 'arrived'";
        try (PreparedStatement ps = conn.prepareStatement(query);){
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if(rs.getDate(1) != null){
                        arriveTime.setTime(rs.getDate(1));
                        return arriveTime;
                    }
                }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }

    @Override
    public int getBuyer(int orderId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select idKli from Porudzbina P where P.IdPor = ?";
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
    public int getLocation(int orderId) {
        int distance = 0;
        int daysPassed = 0;
        Calendar currentTime = general.getCurrentTime();

        Calendar sentTime = Calendar.getInstance();
        Connection conn = DB.getInstance().getConnection();
        String query = "{ call SP_SHORTEST_PATH (?) }";
        String queryStatus = "select P.Status, P.DatumSlanja from Porudzbina P WHERE P.IdPor = ?";
        String queryArticle = "{ call SP_CRITICAL_CITY (?, ?)}";
        String queryLocation = "{ call SP_LOCATION (?, ?, ?)}";

        try ( CallableStatement cs = conn.prepareCall(query);
              PreparedStatement ps = conn.prepareStatement(queryStatus);
              CallableStatement cs2 = conn.prepareCall(queryArticle);
              CallableStatement cs3 = conn.prepareCall(queryLocation);) {
            cs.setInt(1, orderId);
            cs2.setInt(1, orderId);
            ps.setInt(1, orderId);
            ResultSet rsStatus = ps.executeQuery();
            if(rsStatus.next()){
                if(rsStatus.getString(1).equals("created")) return -1;
                sentTime.setTime(rsStatus.getDate(2));
                daysPassed = daysBetween(sentTime, currentTime);
            } else return -1;
            ResultSet rs = cs.executeQuery();
            if(rs.next()){
                System.out.println("Udaljenost: " + rs.getInt(2));
                distance = rs.getInt(2);
                cs2.setInt(2, rs.getInt(1));
                ResultSet rs2 = cs2.executeQuery();
                if(rs2.next()){
                    System.out.println("Najudaljeniji grad od prodavnice je " + rs2.getInt(1));
                    System.out.println("Udaljenost: " + rs2.getInt(2));
                    distance =  rs2.getInt(2);
                }
                if(daysPassed < distance) return rs.getInt(1);
                else{
                    cs3.setInt(1, rs.getInt(1));     
                    cs3.setInt(2, orderId);      
                    cs3.setInt(3, daysPassed - distance);
                    ResultSet rs3 = cs3.executeQuery();
                    if(rs3.next()){
                        System.out.println("Trenutna lokacija porudzbine: " + rs3.getInt(1));
                        return rs3.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -11;
    }

    private int getOrderDays(int orderId) {
        int days = 0;
        Connection conn = DB.getInstance().getConnection();
        String query = "{ call SP_SHORTEST_PATH (?) }";
        String queryArticle = "{ call SP_CRITICAL_CITY (?, ?)}";
        try ( CallableStatement cs = conn.prepareCall(query);
             CallableStatement cs2 = conn.prepareCall(queryArticle);) {
            cs.setInt(1, orderId);
            cs2.setInt(1, orderId);
            ResultSet rs = cs.executeQuery();
            if(rs.next()){
                System.out.println("Najblizi grad prodavnice je " + rs.getInt(1));
                System.out.println("Udaljenost: " + rs.getInt(2));
                days +=  rs.getInt(2);
                cs2.setInt(2, rs.getInt(1));
                ResultSet rs2 = cs2.executeQuery();
                if(rs2.next()){
                    System.out.println("Najudaljeniji grad od prodavnice je " + rs2.getInt(1));
                    System.out.println("Udaljenost: " + rs2.getInt(2));
                    days +=  rs2.getInt(2);
                }
                System.out.println("Ukupno: " + days + " dana");
                return days;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public int daysBetween(Calendar startDate, Calendar endDate){
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return (int)TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }
    
}
