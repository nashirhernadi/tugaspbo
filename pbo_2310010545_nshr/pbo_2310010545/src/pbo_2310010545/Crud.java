/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pbo_2310010545;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Crud {
     // Untuk INSERT, UPDATE, DELETE
    public static int execUpdate(String sql, Object... params) throws SQLException {
        Connection c = Koneksi.getKoneksi();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        }
    }

    // Untuk SELECT dan langsung ditempel ke JTable
    public static DefaultTableModel loadToTable(JTable table, String[] cols, String sql, Object... params) throws SQLException {
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        Connection c = Koneksi.getKoneksi();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[cols.length];
                    for (int col = 0; col < cols.length; col++) {
                        row[col] = rs.getObject(col + 1); // ambil kolom 1..n
                    }
                    model.addRow(row);
                }
            }
        }
        table.setModel(model);
        return model;
    }

    // Ambil satu nilai (baris pertama, kolom pertama)
    public static Object single(String sql, Object... params) throws SQLException {
        Connection c = Koneksi.getKoneksi();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        }
    }
}
