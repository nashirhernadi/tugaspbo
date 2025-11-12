/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package form;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.sql.*;
import java.math.BigDecimal;
import pbo_2310010545.*; // Crud, Koneksi

public class FrmPemesanan extends javax.swing.JFrame {
    
   private boolean initializing = true;

    public FrmPemesanan() {
     initComponents();                 // <— GUI Builder
        setLocationRelativeTo(null);
        setTitle("Pemesanan Tiket");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // jangan ada default button (Enter tidak memicu tombol apa pun)
        try { getRootPane().setDefaultButton(null); } catch (Exception ignore){}

        // txtTotal readonly saja
        try { txtTotal.setEditable(false); } catch (Exception ignore) {}

        loadCombo();                      // isi combo (silent)
        loadTable();

        // ====== Events (hanya tombol) ======
        btnHitung.addActionListener(e -> onHitung());
        btnSimpan.addActionListener(e -> onSimpan());
        btnUbah.addActionListener(e -> onUbah());
        btnHapus.addActionListener(e -> onHapus());
       

        tblPemesanan.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && tblPemesanan.getSelectedRow() >= 0) {
                    txtId.setText(v(0));
                    txtKode.setText(v(1));
                    setCombo(cmbEvent, v(2));
                    setCombo(cmbPelanggan, v(3));
                    txtJumlah.setText(v(4));
                    txtTotal.setText(v(5));
                }
            }
        });

        initializing = false;             // selesai inisialisasi
    }

    // ================= Helper aman =================

    private String v(int c){
        Object o = tblPemesanan.getValueAt(tblPemesanan.getSelectedRow(), c);
        return (o==null) ? "" : o.toString();
    }

    private void setCombo(JComboBox<String> c, String idStr){
        if (idStr == null) return;
        for (int i=0; i<c.getItemCount(); i++) {
            String it = c.getItemAt(i);
            if (it != null && it.startsWith(idStr + " - ")) { c.setSelectedIndex(i); break; }
        }
    }

    /** Ambil id dari combo. return null kalau placeholder/invalid. TIDAK munculkan popup. */
    private Integer tryGetId(JComboBox<String> c) {
        Object it = c.getSelectedItem();
        if (it == null) return null;
        String s = it.toString().trim();
        if (s.isEmpty() || s.startsWith("-")) return null;
        int dash = s.indexOf(" - ");
        if (dash < 1) return null;
        try { return Integer.parseInt(s.substring(0, dash).trim()); }
        catch (NumberFormatException ex) { return null; }
    }

    /** Validasi saat user menekan tombol (baru munculkan popup). */
    private Integer requireId(JComboBox<String> c, String namaField){
        Integer id = tryGetId(c);
        if (id == null) JOptionPane.showMessageDialog(this, namaField + " belum dipilih / data kosong.");
        return id;
    }

    private int parseIntOrDefault(JTextField tf, int def, String namaField, boolean showPopup) {
        String t = tf.getText()==null ? "" : tf.getText().trim();
        if (t.isEmpty()) return def;
        try { return Integer.parseInt(t); }
        catch (NumberFormatException e) {
            if (showPopup) JOptionPane.showMessageDialog(this, namaField + " harus angka.");
            return def;
        }
    }

    private BigDecimal parseDecOrZero(JTextField tf, String namaField, boolean showPopup) {
        String t = tf.getText()==null ? "" : tf.getText().trim();
        if (t.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(t); }
        catch (Exception e) {
            if (showPopup) JOptionPane.showMessageDialog(this, namaField + " harus angka (desimal).");
            return BigDecimal.ZERO;
        }
    }

    // ================ Data binding ================

    private void loadCombo(){
        cmbEvent.removeAllItems();
        cmbPelanggan.removeAllItems();

        // placeholder
        cmbEvent.addItem("- Pilih Event -");
        try (PreparedStatement ps = Koneksi.getKoneksi().prepareStatement(
                "SELECT id,nama_event FROM event_budaya ORDER BY nama_event");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbEvent.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, e.getMessage()); }

        cmbPelanggan.addItem("- Pilih Pelanggan -");
        try (PreparedStatement ps = Koneksi.getKoneksi().prepareStatement(
                "SELECT id,nama FROM pelanggan ORDER BY nama");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbPelanggan.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, e.getMessage()); }

        // otomatis pilih item valid pertama (tanpa men-trigger popup)
        cmbEvent.setSelectedIndex(cmbEvent.getItemCount() > 1 ? 1 : 0);
        cmbPelanggan.setSelectedIndex(cmbPelanggan.getItemCount() > 1 ? 1 : 0);

        // enable/disable tombol
        boolean bisaTransaksi = (cmbEvent.getItemCount() > 1) && (cmbPelanggan.getItemCount() > 1);
        btnHitung.setEnabled(bisaTransaksi);
        btnSimpan.setEnabled(bisaTransaksi);
        btnUbah.setEnabled(bisaTransaksi);
        btnHapus.setEnabled(bisaTransaksi);

        // Default angka
        if (txtJumlah.getText()==null || txtJumlah.getText().trim().isEmpty()) txtJumlah.setText("1");
        if (txtTotal.getText()==null  || txtTotal.getText().trim().isEmpty())  txtTotal.setText("0");
    }

    private void loadTable(){
        try {
            Crud.loadToTable(
                tblPemesanan,
                new String[]{"ID","Kode","EventID","PelangganID","Jumlah","Total","Status","Tanggal"},
                "SELECT id, kode, event_id, pelanggan_id, jumlah_tiket, total, status, DATE_FORMAT(tgl_pesan,'%Y-%m-%d %H:%i') " +
                "FROM pemesanan ORDER BY id DESC"
            );
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    // ================== Actions =====================

    private void onHitung(){
        if (initializing) return; // jangan jalan saat load
        try{
            Integer evId = requireId(cmbEvent, "Event");
            if (evId == null) return;

            Object oHarga = Crud.single("SELECT harga FROM event_budaya WHERE id=?", evId);
            BigDecimal harga = (oHarga == null) ? BigDecimal.ZERO : new BigDecimal(oHarga.toString());

            int jml = parseIntOrDefault(txtJumlah, 1, "Jumlah", true);
            txtTotal.setText(harga.multiply(new BigDecimal(jml)).toString());
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void onSimpan(){
        if (initializing) return;
        try {
            Integer evId  = requireId(cmbEvent, "Event");
            Integer pelId = requireId(cmbPelanggan, "Pelanggan");
            if (evId == null || pelId == null) return;

            if (txtKode.getText()==null || txtKode.getText().trim().isEmpty())
                txtKode.setText("PSN" + (System.currentTimeMillis() % 100000));

            onHitung(); // agar total terisi benar

            int jml = parseIntOrDefault(txtJumlah, 1, "Jumlah", true);
            BigDecimal total = parseDecOrZero(txtTotal, "Total", true);

            Crud.execUpdate(
                "INSERT INTO pemesanan(kode,event_id,pelanggan_id,jumlah_tiket,total,status) VALUES(?,?,?,?,?,?)",
                txtKode.getText(), evId, pelId, jml, total, "baru"
            );
            loadTable(); resetForm();
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void onUbah(){
        if (initializing) return;
        try {
            Integer evId  = requireId(cmbEvent, "Event");
            Integer pelId = requireId(cmbPelanggan, "Pelanggan");
            if (evId == null || pelId == null) return;

            int jml = parseIntOrDefault(txtJumlah, 1, "Jumlah", true);
            BigDecimal total = parseDecOrZero(txtTotal, "Total", true);

            Crud.execUpdate(
                "UPDATE pemesanan SET kode=?, event_id=?, pelanggan_id=?, jumlah_tiket=?, total=? WHERE id=?",
                txtKode.getText(), evId, pelId, jml, total, Integer.parseInt(txtId.getText())
            );
            loadTable(); resetForm();
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void onHapus(){
        if (initializing) return;
        try {
            Crud.execUpdate("DELETE FROM pemesanan WHERE id=?", Integer.parseInt(txtId.getText()));
            loadTable(); resetForm();
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void resetForm(){
        txtId.setText("");
        txtKode.setText("");
        txtJumlah.setText("1");
        txtTotal.setText("0");
        if (cmbEvent.getItemCount()>1) cmbEvent.setSelectedIndex(1); else cmbEvent.setSelectedIndex(0);
        if (cmbPelanggan.getItemCount()>1) cmbPelanggan.setSelectedIndex(1); else cmbPelanggan.setSelectedIndex(0);
        tblPemesanan.clearSelection();
    
    }
    // ================== HELPER METHOD ==================


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSimpan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnHapus = new javax.swing.JButton();
        txtId = new javax.swing.JTextField();
        btnUbah = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtKode = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtJumlah = new javax.swing.JTextField();
        txtJumlahPeserta = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPemesanan = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbPelanggan = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        cmbEvent = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        btnHitung = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        jLabel1.setText("Id Pemesanan");

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnUbah.setText("Ubah");
        btnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahActionPerformed(evt);
            }
        });

        jLabel2.setText("Kode Pemesanan");

        jLabel3.setText("Jumlah");

        txtJumlahPeserta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtJumlahPesertaKeyReleased(evt);
            }
        });

        tblPemesanan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblPemesanan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPemesananMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPemesanan);

        jLabel4.setText("Jumlah Peserta");

        txtTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalActionPerformed(evt);
            }
        });

        jLabel6.setText("Total");

        cmbPelanggan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Id", "Nama", " " }));

        jLabel8.setText("Pelanggan");

        cmbEvent.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Id", "Nama", " " }));
        cmbEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbEventActionPerformed(evt);
            }
        });

        jLabel9.setText("Event");

        btnHitung.setText("Hitung");
        btnHitung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHitungActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtKode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtJumlah, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtJumlahPeserta, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnHitung)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSimpan)
                                .addGap(18, 18, 18)
                                .addComponent(btnUbah)
                                .addGap(18, 18, 18)
                                .addComponent(btnHapus))
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 552, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(cmbPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(cmbEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtKode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtJumlahPeserta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnUbah)
                    .addComponent(btnHapus)
                    .addComponent(btnHitung))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
       
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahActionPerformed
      
    }//GEN-LAST:event_btnUbahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
       
    }//GEN-LAST:event_btnHapusActionPerformed

    private void tblPemesananMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPemesananMouseClicked
      
    }//GEN-LAST:event_tblPemesananMouseClicked

    private void txtTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalActionPerformed

    private void cmbEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbEventActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbEventActionPerformed

    private void txtJumlahPesertaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahPesertaKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtJumlahPesertaKeyReleased

    private void btnHitungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHitungActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnHitungActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
      } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
    java.util.logging.Logger.getAnonymousLogger()
        .log(java.util.logging.Level.SEVERE, null, ex);
}     
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmPemesanan().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnHitung;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnUbah;
    private javax.swing.JComboBox<String> cmbEvent;
    private javax.swing.JComboBox<String> cmbPelanggan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPemesanan;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtJumlahPeserta;
    private javax.swing.JTextField txtKode;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
