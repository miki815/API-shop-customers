/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import rs.etf.sab.operations.*;
import org.junit.Test;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

import java.util.Calendar;

public class StudentMain {

    public static void main(String[] args) {
        System.out.println("test");

        ArticleOperations articleOperations = new MM190117_ArticleOperations(); 
        BuyerOperations buyerOperations = new MM190117_BuyerOperations();
        CityOperations cityOperations = new MM190117_CityOperations();
        GeneralOperations generalOperations = new MM190117_GeneralOperations();
        OrderOperations orderOperations = new MM190117_OrderOperations(generalOperations);
        ShopOperations shopOperations = new MM190117_ShopOperations();
        TransactionOperations transactionOperations = new MM190117_TransactionOperations();

        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();
    }
}
