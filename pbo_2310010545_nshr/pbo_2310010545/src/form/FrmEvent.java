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
import java.util.regex.Pattern;
import pbo_2310010545.*; // Crud, Koneksi
/**
 *
 * @author Anomali
 */
public class FrmEvent extends javax.swing.JFrame {
    
  
    public FrmEvent() {
      initComponents(); // <- GUI Builder
        setLocationRelativeTo(null);
        setTitle("Event Budaya");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initJenisStatic();      // isi combo jenis (static)
        loadComboObjek();       // isi combo objek dari DB
        loadTable();            // tampilkan data tabel

        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
     

        tblEvent.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting() && tblEvent.getSelectedRow()>=0){
                    txtId.setText(v(0));
                    txtNama.setText(v(1));
                    setComboById(cmbObjek, v(2));           // ObjekID
                    cmbJenis.setSelectedItem(v(3));         // Jika v(3) int, nanti disesuaikan di bawah
                    // Kalau kolom jenis kamu INT, tabel menampilkan angka.
                    // Agar combo cocok, map manual:
                    try {
                        int asInt = Integer.parseInt(v(3));
                        // 1:Tari, 2:Musik, 3:Teater, 4:Upacara, 5:Kuliner, 6:Lainnya (mapping bebas)
                        String nama = mapJenisIdToText(asInt);
                        if (nama != null) cmbJenis.setSelectedItem(nama);
                    } catch (Exception ignore) {}
                    txtTanggal.setText(v(4));
                    txtDurasi.setText(v(5));
                    txtHarga.setText(v(6));
                    txtKuota.setText(v(7));
                    txtFasil.setText(v(8));
                    txtLokasi.setText(v(9));
                }
            }
        });

        // default angka agar tidak kosong
        if (empty(txtDurasi)) txtDurasi.setText("0");
        if (empty(txtHarga))  txtHarga.setText("0");
        if (empty(txtKuota))  txtKuota.setText("0");
    }

    // ========= Helper UI & parsing =========

    private boolean empty(JTextField tf){ return tf.getText()==null || tf.getText().trim().isEmpty(); }

    private String v(int c){
        Object o = tblEvent.getValueAt(tblEvent.getSelectedRow(), c);
        return (o==null) ? "" : o.toString();
    }

    private int parseIntOr0(JTextField tf, String namaField){
        String t = tf.getText()==null ? "" : tf.getText().trim();
        if (t.isEmpty()) return 0;
        try { return Integer.parseInt(t); }
        catch (Exception e){ JOptionPane.showMessageDialog(this, namaField + " harus angka."); return 0; }
    }

    private BigDecimal parseDecOr0(JTextField tf, String namaField){
        String t = tf.getText()==null ? "" : tf.getText().trim();
        if (t.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(t); }
        catch (Exception e){ JOptionPane.showMessageDialog(this, namaField + " harus angka."); return BigDecimal.ZERO; }
    }

    private void setComboById(JComboBox<String> combo, String idStr){
        if (idStr == null || idStr.isEmpty()) return;
        for (int i=0; i<combo.getItemCount(); i++){
            String it = combo.getItemAt(i);
            if (it != null && it.startsWith(idStr + " - ")) { combo.setSelectedIndex(i); break; }
        }
    }

    private Integer idFromComboAllowNull(JComboBox<String> combo){
        Object it = combo.getSelectedItem();
        if (it == null) return null;
        String s = it.toString();
        if (s.startsWith("0 - ")) return null; // Tanpa objek
        int dash = s.indexOf(" - ");
        if (dash < 1) return null;
        try { return Integer.parseInt(s.substring(0, dash).trim()); }
        catch(Exception e){ return null; }
    }

    // ========= Jenis (text/INT kompatibel) =========

    // Isi combo secara statik (kamu bebas ganti urutan)
    private void initJenisStatic(){
        cmbJenis.removeAllItems();
        cmbJenis.addItem("Tari");
        cmbJenis.addItem("Musik");
        cmbJenis.addItem("Teater");
        cmbJenis.addItem("Upacara");
        cmbJenis.addItem("Kuliner");
        cmbJenis.addItem("Lainnya");
        cmbJenis.setSelectedIndex(0);
    }

    // Mapping id -> text kalau kolom 'jenis' kamu INT
    private String mapJenisIdToText(int id){
        switch (id){
            case 1: return "Tari";
            case 2: return "Musik";
            case 3: return "Teater";
            case 4: return "Upacara";
            case 5: return "Kuliner";
            case 6: return "Lainnya";
            default: return null;
        }
    }

    // Mapping text -> id (untuk kolom INT)
    private Integer mapJenisTextToId(String text){
        if (text==null) return null;
        switch (text){
            case "Tari": return 1;
            case "Musik": return 2;
            case "Teater": return 3;
            case "Upacara": return 4;
            case "Kuliner": return 5;
            case "Lainnya": return 6;
            default: return null;
        }
    }

    // Ambil nilai yang aman untuk dikirim ke DB (INT atau TEXT)
    private Object jenisValueForDB() {
        // Cek tipe kolom 'jenis' di database (sekali query cepat)
        try {
            DatabaseMetaData md = Koneksi.getKoneksi().getMetaData();
            try (ResultSet rs = md.getColumns(null, null, "event_budaya", "jenis")) {
                if (rs.next()) {
                    String typeName = rs.getString("TYPE_NAME"); // VARCHAR / INT / ENUM / dll
                    String sel = (cmbJenis.getSelectedItem()==null) ? "" : cmbJenis.getSelectedItem().toString();
                    if (typeName != null && typeName.toUpperCase().contains("INT")) {
                        // kolom INT -> kirim id (angka)
                        Integer id = mapJenisTextToId(sel);
                        return (id == null ? 0 : id);
                    } else {
                        // kolom VARCHAR/ENUM -> kirim teks
                        return sel;
                    }
                }
            }
        } catch (Exception ignore) {}
        // Fallback aman: kirim teks
        return (cmbJenis.getSelectedItem()==null) ? null : cmbJenis.getSelectedItem().toString();
    }

    // ========= Data binding DB =========

    private void loadComboObjek(){
        cmbObjek.removeAllItems();
        cmbObjek.addItem("0 - (Tanpa Objek)");
        try (PreparedStatement ps = Koneksi.getKoneksi().prepareStatement(
                "SELECT id, nama_objek FROM objek_budaya ORDER BY nama_objek");
             ResultSet rs = ps.executeQuery()){
            while (rs.next()) cmbObjek.addItem(rs.getInt(1)+" - "+rs.getString(2));
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
        cmbObjek.setSelectedIndex(0);
    }

    private void loadTable(){
        try {
            Crud.loadToTable(
                tblEvent,
                new String[]{"ID","Nama","ObjekID","Jenis","Tanggal","Durasi","Harga","Kuota","Fasilitas","Lokasi"},
                "SELECT id, nama_event, IFNULL(objek_id,0) AS objek_id, jenis, " +
                "IFNULL(DATE_FORMAT(tanggal,'%Y-%m-%d'),'') AS tgl, durasi_jam, harga, kuota, " +
                "IFNULL(fasilitas,''), IFNULL(lokasi,'') " +
                "FROM event_budaya ORDER BY id DESC"
            );
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    // ========= Validasi ringan =========

    private boolean validTanggal(){
        String t = txtTanggal.getText()==null ? "" : txtTanggal.getText().trim();
        if (t.isEmpty()) return true; // kosong kita izinkan (biar gak blok)
        boolean ok = Pattern.matches("\\d{4}-\\d{2}-\\d{2}", t);
        if (!ok) JOptionPane.showMessageDialog(this, "Format tanggal harus YYYY-MM-DD (contoh 2025-11-30).");
        return ok;
    }

    // ========= CRUD =========

    private void simpan(){
        try {
            if (!validTanggal()) return;

            Crud.execUpdate(
                "INSERT INTO event_budaya(nama_event,objek_id,jenis,tanggal,durasi_jam,harga,kuota,fasilitas,lokasi) " +
                "VALUES(?,?,?,?,?,?,?,?,?)",
                txtNama.getText(),
                idFromComboAllowNull(cmbObjek),
                jenisValueForDB(),                         // <— kunci: aman INT / TEXT
                txtTanggal.getText(),
                parseIntOr0(txtDurasi,"Durasi"),
                parseDecOr0(txtHarga,"Harga"),
                parseIntOr0(txtKuota,"Kuota"),
                txtFasil.getText(),
                txtLokasi.getText()
            );
            loadTable(); resetForm();
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void ubah(){
        try {
            if (!validTanggal()) return;

            Crud.execUpdate(
                "UPDATE event_budaya SET nama_event=?, objek_id=?, jenis=?, tanggal=?, durasi_jam=?, harga=?, kuota=?, fasilitas=?, lokasi=? WHERE id=?",
                txtNama.getText(),
                idFromComboAllowNull(cmbObjek),
                jenisValueForDB(),                         // <— kunci: aman INT / TEXT
                txtTanggal.getText(),
                parseIntOr0(txtDurasi,"Durasi"),
                parseDecOr0(txtHarga,"Harga"),
                parseIntOr0(txtKuota,"Kuota"),
                txtFasil.getText(),
                txtLokasi.getText(),
                Integer.parseInt(txtId.getText())
            );
            loadTable(); resetForm();
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void hapus(){
        try {
            Crud.execUpdate("DELETE FROM event_budaya WHERE id=?", Integer.parseInt(txtId.getText()));
            loadTable(); resetForm();
        } catch (Exception e){ JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void resetForm(){
        txtId.setText("");
        txtNama.setText("");
        txtTanggal.setText("");
        txtDurasi.setText("0");
        txtHarga.setText("0");
        txtKuota.setText("0");
        txtFasil.setText("");
        txtLokasi.setText("");
        if (cmbObjek.getItemCount()>0) cmbObjek.setSelectedIndex(0);
        try { cmbJenis.setSelectedIndex(0); } catch (Exception ignore) {}
        tblEvent.clearSelection();
    }


 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSimpan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnHapus = new javax.swing.JButton();
        txtId = new javax.swing.JTextField();
        btnUbah = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDurasi = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblEvent = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cmbObjek = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtTanggal = new javax.swing.JTextField();
        jScroolPane2 = new javax.swing.JScrollPane();
        txtFasil = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        cmbJenis = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        txtKuota = new javax.swing.JTextField();
        txtLokasi = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtHarga = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        jLabel1.setText("ID Event");

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        txtId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdActionPerformed(evt);
            }
        });

        btnUbah.setText("Ubah");
        btnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahActionPerformed(evt);
            }
        });

        jLabel2.setText("Nama Event");

        jLabel3.setText("Durasi");

        tblEvent.setModel(new javax.swing.table.DefaultTableModel(
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
        tblEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblEventMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblEvent);

        jLabel4.setText("Fasilitas");

        jLabel5.setText("Objek");

        cmbObjek.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id", "nama objek", " ", " " }));
        cmbObjek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbObjekActionPerformed(evt);
            }
        });

        jLabel6.setText("Tanggal");

        txtFasil.setColumns(20);
        txtFasil.setRows(5);
        jScroolPane2.setViewportView(txtFasil);

        jLabel7.setText("Jenis");

        cmbJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id", "nama", " " }));
        cmbJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbJenisActionPerformed(evt);
            }
        });

        jLabel8.setText("Kouta");

        jLabel9.setText("Lokasi");

        jLabel10.setText("Harga");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtDurasi, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtId, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNama, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTanggal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSimpan)
                                .addGap(18, 18, 18)
                                .addComponent(btnUbah)
                                .addGap(18, 18, 18)
                                .addComponent(btnHapus))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(52, 52, 52)
                                    .addComponent(cmbJenis, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(52, 52, 52)
                                    .addComponent(cmbObjek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScroolPane2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtKuota, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtLokasi, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 551, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cmbObjek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbJenis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDurasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtKuota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtLokasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScroolPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnUbah)
                    .addComponent(btnHapus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbObjekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbObjekActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbObjekActionPerformed

    private void txtIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
       
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahActionPerformed
       
    }//GEN-LAST:event_btnUbahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
      
    }//GEN-LAST:event_btnHapusActionPerformed

    private void tblEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblEventMouseClicked
       
    }//GEN-LAST:event_tblEventMouseClicked

    private void cmbJenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbJenisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbJenisActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new FrmEvent().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnUbah;
    private javax.swing.JComboBox<String> cmbJenis;
    private javax.swing.JComboBox<String> cmbObjek;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScroolPane2;
    private javax.swing.JTable tblEvent;
    private javax.swing.JTextField txtDurasi;
    private javax.swing.JTextArea txtFasil;
    private javax.swing.JTextField txtHarga;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtKuota;
    private javax.swing.JTextField txtLokasi;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtTanggal;
    // End of variables declaration//GEN-END:variables
}
