/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.ArticleOperations;


public class MM190117_ArticleOperations implements ArticleOperations{
    
    public static void gradovi() {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Grad";
        try (
            PreparedStatement stmt = conn.prepareStatement(query,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);  
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int createArticle​(int shopId, String articleName, int articlePrice){
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Artikal(Naziv, IdKli, Cena) values(?, ?, ?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, articleName);
            ps.setInt(2, shopId);
            ps.setInt(3, articlePrice);
            ps.executeUpdate();
            try( ResultSet rs = ps.getGeneratedKeys();){
                if (rs.next()) {
                    System.out.println("Kreiran je novi artikal " + rs.getInt(1));
                    return rs.getInt(1);
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MM190117_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
    }

    
    
}
