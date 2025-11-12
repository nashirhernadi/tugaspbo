/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pbo_2310010545;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author Anomali
 */
public class Koneksi {

   // SESUAIKAN nama DB, user, pass
    private static final String URL  = "jdbc:mysql://localhost:3306/pbo_2310010545";
    private static final String USER = "root";
    private static final String PASS = ""; // isi kalau pakai password

    // Selalu buat koneksi baru setiap dipanggil
    public static Connection getKoneksi() {
        try {
            // Connector/J 5.x pakai ini:
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            return conn;

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Driver MySQL tidak ditemukan.\nPastikan mysql-connector-java-5.x sudah ada di Libraries.\n" 
                    + e.getMessage()
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Gagal koneksi ke database.\nPeriksa XAMPP (MySQL ON), nama database, user, dan password.\n"
                    + e.getMessage()
            );
        }
        return null;
    }
}
    
