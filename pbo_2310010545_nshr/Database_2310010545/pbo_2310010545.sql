-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 12 Nov 2025 pada 11.06
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pbo_2310010545`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `event_budaya`
--

CREATE TABLE `event_budaya` (
  `id` int(11) NOT NULL,
  `nama_event` varchar(150) NOT NULL,
  `objek_id` int(11) DEFAULT NULL,
  `jenis` enum('Tari','Musik','Teater','Upacara','Kuliner','Lainnya') DEFAULT 'Lainnya',
  `tanggal` date DEFAULT NULL,
  `durasi_jam` int(11) DEFAULT 0,
  `harga` decimal(12,2) DEFAULT 0.00,
  `kuota` int(11) DEFAULT 0,
  `fasilitas` text DEFAULT NULL,
  `lokasi` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `event_budaya`
--

INSERT INTO `event_budaya` (`id`, `nama_event`, `objek_id`, `jenis`, `tanggal`, `durasi_jam`, `harga`, `kuota`, `fasilitas`, `lokasi`) VALUES
(1, 'Festival Tari Piring', 1, 'Tari', '2025-11-30', 3, 25000.00, 200, 'Panggung, Sound', 'GOR Banjarmasin'),
(2, 'Parade Wayang', 2, 'Teater', '2025-12-05', 2, 40000.00, 150, 'Kursi VIP, Souvenir', 'Taman Budaya'),
(3, 'parede musik', 4, 'Musik', '2005-10-11', 1900, 2222.00, 1121, 'baik', 'bjm');

-- --------------------------------------------------------

--
-- Struktur dari tabel `kategori_budaya`
--

CREATE TABLE `kategori_budaya` (
  `id` int(11) NOT NULL,
  `nama_kategori` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `kategori_budaya`
--

INSERT INTO `kategori_budaya` (`id`, `nama_kategori`) VALUES
(5, 'Kuliner'),
(2, 'Musik'),
(1, 'Tari'),
(3, 'Teater'),
(7, 'Test'),
(4, 'Upacara 1');

-- --------------------------------------------------------

--
-- Struktur dari tabel `objek_budaya`
--

CREATE TABLE `objek_budaya` (
  `id` int(11) NOT NULL,
  `nama_objek` varchar(150) NOT NULL,
  `kategori_id` int(11) NOT NULL,
  `asal_daerah` varchar(100) DEFAULT NULL,
  `tahun_ditemukan` varchar(10) DEFAULT NULL,
  `deskripsi` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `objek_budaya`
--

INSERT INTO `objek_budaya` (`id`, `nama_objek`, `kategori_id`, `asal_daerah`, `tahun_ditemukan`, `deskripsi`) VALUES
(1, 'Tari Piring', 1, 'Sumatera Barat', '—', 'Tarian tradisional Minangkabau'),
(2, 'Wayang Kulit', 3, 'Jawa', '—', 'Pertunjukan teater bayangan'),
(3, 'Reog Ponorogo', 1, 'Jawa Timur', '—', 'Kesenian tradisional Ponorogo'),
(4, 'bekantan', 1, 'bjm', '2000', 'bb');

-- --------------------------------------------------------

--
-- Struktur dari tabel `pelanggan`
--

CREATE TABLE `pelanggan` (
  `id` int(11) NOT NULL,
  `nama` varchar(100) NOT NULL,
  `telp` varchar(25) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `alamat` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `pelanggan`
--

INSERT INTO `pelanggan` (`id`, `nama`, `telp`, `email`, `alamat`) VALUES
(1, 'nashir', '877', 'nashir@gmail.com', 'kayutangi');

-- --------------------------------------------------------

--
-- Struktur dari tabel `pemesanan`
--

CREATE TABLE `pemesanan` (
  `id` int(11) NOT NULL,
  `kode` varchar(20) DEFAULT NULL,
  `event_id` int(11) NOT NULL,
  `pelanggan_id` int(11) NOT NULL,
  `tgl_pesan` datetime DEFAULT current_timestamp(),
  `jumlah_tiket` int(11) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `status` enum('baru','lunas','batal') DEFAULT 'baru'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `pemesanan`
--

INSERT INTO `pemesanan` (`id`, `kode`, `event_id`, `pelanggan_id`, `tgl_pesan`, `jumlah_tiket`, `total`, `status`) VALUES
(1, '21', 1, 1, '2025-11-12 17:46:50', 1, 25000.00, 'baru'),
(4, '1', 2, 1, '2025-11-12 17:47:44', 2, 80000.00, 'baru'),
(5, '12', 1, 1, '2025-11-12 17:55:29', 1, 25000.00, 'baru'),
(7, '53', 1, 1, '2025-11-12 17:55:54', 232, 5800000.00, 'baru'),
(9, '33', 2, 1, '2025-11-12 18:06:28', 1, 40000.00, 'baru');

-- --------------------------------------------------------

--
-- Struktur dari tabel `user`
--

CREATE TABLE `user` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `level` enum('Admin','User') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `user`
--

INSERT INTO `user` (`id_user`, `username`, `password`, `level`) VALUES
(1, 'admin', 'admin', 'Admin'),
(2, 'user', 'user', 'User');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `event_budaya`
--
ALTER TABLE `event_budaya`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ev_obj` (`objek_id`);

--
-- Indeks untuk tabel `kategori_budaya`
--
ALTER TABLE `kategori_budaya`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nama_kategori` (`nama_kategori`);

--
-- Indeks untuk tabel `objek_budaya`
--
ALTER TABLE `objek_budaya`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_obj_kat` (`kategori_id`);

--
-- Indeks untuk tabel `pelanggan`
--
ALTER TABLE `pelanggan`
  ADD PRIMARY KEY (`id`);

--
-- Indeks untuk tabel `pemesanan`
--
ALTER TABLE `pemesanan`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `kode` (`kode`),
  ADD KEY `fk_psn_ev` (`event_id`),
  ADD KEY `fk_psn_pel` (`pelanggan_id`);

--
-- Indeks untuk tabel `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `event_budaya`
--
ALTER TABLE `event_budaya`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT untuk tabel `kategori_budaya`
--
ALTER TABLE `kategori_budaya`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT untuk tabel `objek_budaya`
--
ALTER TABLE `objek_budaya`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT untuk tabel `pelanggan`
--
ALTER TABLE `pelanggan`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT untuk tabel `pemesanan`
--
ALTER TABLE `pemesanan`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT untuk tabel `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `event_budaya`
--
ALTER TABLE `event_budaya`
  ADD CONSTRAINT `fk_ev_obj` FOREIGN KEY (`objek_id`) REFERENCES `objek_budaya` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Ketidakleluasaan untuk tabel `objek_budaya`
--
ALTER TABLE `objek_budaya`
  ADD CONSTRAINT `fk_obj_kat` FOREIGN KEY (`kategori_id`) REFERENCES `kategori_budaya` (`id`) ON UPDATE CASCADE;

--
-- Ketidakleluasaan untuk tabel `pemesanan`
--
ALTER TABLE `pemesanan`
  ADD CONSTRAINT `fk_psn_ev` FOREIGN KEY (`event_id`) REFERENCES `event_budaya` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_psn_pel` FOREIGN KEY (`pelanggan_id`) REFERENCES `pelanggan` (`id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
