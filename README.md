# Aplikasi Manajemen Toko Kelontong

## Deskripsi
Aplikasi desktop JavaFX untuk mengelola data barang toko kelontong dengan fitur lengkap untuk penjualan, stok, dan manajemen barang.

## Teknologi yang Digunakan
- **JavaFX 21** - Framework UI
- **MySQL** - Database
- **XAMPP** - Local server untuk MySQL
- **OpenPDF** - Generate nota PDF
- **Apache POI** - Export laporan Excel
- **Maven** - Build tool

## Fitur Utama

### 1. 📊 **Dashboard**
- Tampilan metrik penting dalam satu layar
- Total penjualan hari ini dan minggu ini
- Daftar barang dengan stok menipis
- Total nilai persediaan
- Grafik tren penjualan 7 hari terakhir
- Daftar produk terlaris bulan ini
- Navigasi cepat ke manajemen barang dan laporan

### 2. 📋 **Manajemen Data Barang**
- Tampilan tabel data barang dengan kolom: ID, Nama, Kategori, Harga, Stok, Tanggal Masuk
- Pencarian barang berdasarkan nama
- Sorting berdasarkan Nama, Harga, atau Stok (ASC/DESC)
- Refresh data otomatis

### 3. 🛒 **Penjualan Barang**
- Pilih barang dari tabel → klik "Jual Barang"
- Input jumlah yang dijual dengan validasi stok
- Otomatis update stok setelah penjualan
- Generate nota penjualan dalam PDF
- Simpan riwayat transaksi ke database

### 4. 📦 **Tambah Stok**
- Input format: `ID,Jumlah` (satu baris per barang)
- Support batch update untuk banyak barang sekaligus
- Validasi ID barang dan format input
- Laporan detail hasil (berhasil/gagal)
- Transaksi database yang aman

### 5. ➕ **Tambah Barang Baru**
- Form input: Nama, Kategori, Harga, Stok Awal
- ComboBox kategori editable (pilih yang ada atau ketik baru)
- Validasi nama barang unik
- Tanggal masuk otomatis
- Kategori baru langsung tersedia setelah disimpan

### 6. 🖱️ **Context Menu (Klik Kanan)**
- **Edit Barang**: Ubah nama, kategori, harga, atau stok
- **Tambah Stok Manual**: Tambah stok untuk barang yang dipilih
- **Hapus Barang**: Hapus barang dengan konfirmasi berlapis (2 tahap)
- Akses cepat melalui klik kanan pada baris tabel

### 7. 📊 **Laporan Penjualan**
- Laporan Harian, Mingguan, dan Bulanan
- Export ke Excel dengan format rapi
- Detail transaksi lengkap dengan total
- Statistik periode dan jumlah transaksi
- Nama file sesuai periode laporan

## Struktur Database

### Tabel `barang`:
```sql
CREATE TABLE barang (
    id_barang INT AUTO_INCREMENT PRIMARY KEY,
    nama_barang VARCHAR(255) UNIQUE NOT NULL,
    kategori VARCHAR(100) NOT NULL,
    harga INT NOT NULL,
    stok INT NOT NULL DEFAULT 0,
    tanggal_masuk DATE NOT NULL
);
```

### Tabel `penjualan`:
```sql
CREATE TABLE penjualan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_barang INT NOT NULL,
    jumlah INT NOT NULL,
    harga_satuan DOUBLE NOT NULL,
    tanggal DATETIME NOT NULL,
    FOREIGN KEY (id_barang) REFERENCES barang(id_barang)
);
```

## Cara Menjalankan

### Persiapan:
1. Install XAMPP dan jalankan MySQL
2. Buat database bernama `tokodepi`
3. Import/buat tabel `barang` dan `penjualan`
4. Pastikan Maven dan Java 21 terinstall

### Compile & Run:
```bash
mvn clean compile
mvn javafx:run
```

### Atau dengan IDE:
- Import project ke IDE (IntelliJ/Eclipse)
- Run class `MainApp.java`

## Konfigurasi Database
File: `src/main/java/com/depichan/db/DBConnection.java`
```java
private static final String URL = "jdbc:mysql://localhost:3306/tokodepi";
private static final String USER = "root";
private static final String PASSWORD = ""; // kosong untuk XAMPP default
```

## Struktur Project
```
src/
├── main/
│   ├── java/
│   │   └── com/depichan/
│   │       ├── MainApp.java
│   │       ├── controller/
│   │       │   ├── BarangController.java
│   │       │   └── NotaController.java
│   │       ├── db/
│   │       │   └── DBConnection.java
│   │       └── model/
│   │           └── Barang.java
│   └── resources/
│       ├── img/
│       │   └── icon.png
│       └── view/
│           ├── barang-view.fxml
│           └── Nota.fxml
└── test/
    └── java/
```

## Dependencies (pom.xml)
- JavaFX FXML
- MySQL Connector Java
- OpenPDF (untuk generate PDF)

## Fitur Keamanan
- PreparedStatement untuk mencegah SQL injection
- Validasi input komprehensif
- Konfirmasi berlapis untuk operasi berisiko (hapus barang)
- Transaction rollback untuk batch operations
- Double confirmation untuk penghapusan data

## Output Files
- **Nota PDF**: Disimpan di folder `nota/` dengan format `nota-[timestamp].pdf`
- **Database Backup**: Disarankan backup berkala

## Troubleshooting
1. **Database Connection Error**: Pastikan XAMPP MySQL running
2. **JavaFX Error**: Pastikan menggunakan Java 21 dengan JavaFX
3. **PDF Generation Error**: Periksa permission folder `nota/`

## Author
- **Developer**: Depichan
- **Version**: 1.0-SNAPSHOT
- **Build Tool**: Maven
