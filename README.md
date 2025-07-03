# 🏪 DepiStore Manager

## 📋 Deskripsi
DepiStore Manager adalah aplikasi desktop berbasis JavaFX yang dirancang khusus untuk mengelola toko kelontong dengan fitur-fitur lengkap. Aplikasi ini menyediakan solusi terintegrasi untuk manajemen persediaan, penjualan, dan laporan bisnis dengan antarmuka yang intuitif dan modern.

## 🎯 Tujuan Aplikasi
- Mempermudah pengelolaan data barang dan stok toko
- Menyediakan dashboard analitik untuk monitoring bisnis
- Mengotomatisasi proses penjualan dan pencatatan transaksi
- Menghasilkan laporan komprehensif untuk analisis bisnis
- Memberikan interface yang user-friendly untuk pemilik toko

## 🛠️ Teknologi yang Digunakan
- **JavaFX 21.0.1** - Framework UI Desktop
- **MySQL 8.0.33** - Database Management System
- **XAMPP** - Local development server
- **OpenPDF 1.3.30** - Library untuk generate nota PDF
- **Apache POI 5.2.3** - Export laporan ke format Excel
- **Maven** - Build automation tool
- **Java 21** - Programming language

## ✨ Fitur Utama

### 1. 📊 **Dashboard Analitik**
- **Metrik Real-time**: Penjualan harian, mingguan, dan total transaksi
- **Monitoring Stok**: Peringatan barang dengan stok menipis (< 10 unit)
- **Valuasi Persediaan**: Total nilai barang dalam persediaan
- **Grafik Tren**: Visualisasi penjualan 7 hari terakhir dalam bentuk bar chart
- **Top Products**: Daftar produk terlaris bulan ini
- **Quick Actions**: Navigasi cepat ke manajemen barang dan laporan

### 2. � **Manajemen Data Barang**
- **Tampilan Tabel**: Data barang dengan kolom ID, Nama, Kategori, Harga, Stok, Tanggal Masuk
- **Pencarian Cerdas**: Filter barang berdasarkan nama secara real-time
- **Sorting Fleksibel**: Pengurutan berdasarkan Nama, Harga, atau Stok (ASC/DESC)
- **Auto Refresh**: Pembaruan data otomatis setiap kali ada perubahan
- **Indikator Stok**: Highlight visual untuk barang dengan stok rendah

### 3. 🛒 **Sistem Penjualan**
- **Proses Penjualan**: Pilih barang → klik "Jual Barang" → input jumlah
- **Validasi Stok**: Otomatis cek ketersediaan stok sebelum penjualan
- **Update Real-time**: Pengurangan stok otomatis setelah transaksi
- **Nota Digital**: Generate nota penjualan dalam format PDF
- **Riwayat Transaksi**: Semua transaksi tersimpan dalam database

### 4. � **Manajemen Stok**
- **Batch Update**: Input format `ID,Jumlah` untuk update banyak barang sekaligus
- **Validasi Input**: Cek ID barang dan format data secara otomatis
- **Laporan Detail**: Feedback lengkap untuk setiap operasi (berhasil/gagal)
- **Transaction Safety**: Rollback otomatis jika ada error
- **Manual Add**: Tambah stok individual melalui context menu

### 5. ➕ **Tambah Barang Baru**
- **Form Lengkap**: Input Nama, Kategori, Harga, Stok Awal
- **Kategori Dinamis**: ComboBox editable untuk kategori baru
- **Validasi Unique**: Pencegahan duplikasi nama barang
- **Timestamp Otomatis**: Tanggal masuk tercatat otomatis
- **Kategori Persisten**: Kategori baru langsung tersedia untuk barang lain

### 6. 🖱️ **Context Menu (Klik Kanan)**
- **Edit Barang**: Ubah nama, kategori, harga, atau stok
- **Tambah Stok Manual**: Tambah stok untuk barang yang dipilih
- **Hapus Barang**: Hapus barang dengan konfirmasi berlapis (2 tahap)
- **Akses Cepat**: Semua fungsi dapat diakses langsung dari tabel

### 7. 📊 **Sistem Laporan**
- **Laporan Harian**: Transaksi per hari dengan detail lengkap
- **Laporan Mingguan**: Ringkasan penjualan dalam seminggu
- **Laporan Bulanan**: Analisis bulanan dengan statistik
- **Export Excel**: Format laporan professional siap print
- **Auto Naming**: Nama file sesuai periode laporan
- **Statistik Komprehensif**: Total transaksi, item terjual, dan revenue

### 8. 🔐 **Keamanan & Validasi**
- **SQL Injection Prevention**: Menggunakan PreparedStatement
- **Input Validation**: Validasi komprehensif untuk semua input
- **Double Confirmation**: Konfirmasi berlapis untuk operasi berisiko
- **Transaction Rollback**: Keamanan data dengan rollback otomatis
- **Error Handling**: Penanganan error yang robust

## 🗃️ Struktur Database

DepiStore Manager menggunakan database MySQL dengan struktur yang optimal untuk performa dan integritas data.

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

**Keterangan Kolom:**
- `id_barang`: Primary key dengan auto increment
- `nama_barang`: Nama barang (unique constraint)
- `kategori`: Kategori barang untuk pengelompokan
- `harga`: Harga jual per unit (dalam rupiah)
- `stok`: Jumlah stok tersedia
- `tanggal_masuk`: Tanggal barang ditambahkan

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

**Keterangan Kolom:**
- `id`: Primary key transaksi
- `id_barang`: Foreign key ke tabel barang
- `jumlah`: Jumlah barang yang dijual
- `harga_satuan`: Harga per unit saat transaksi
- `tanggal`: Timestamp transaksi

### Relasi Database:
- **One-to-Many**: Satu barang dapat memiliki banyak transaksi penjualan
- **Referential Integrity**: Foreign key constraint menjamin konsistensi data
- **Index**: Otomatis pada primary key dan foreign key untuk performa optimal

## 🏗️ Arsitektur Aplikasi

### Model-View-Controller (MVC)
```
src/main/java/com/depichan/
├── MainApp.java                    # Entry point aplikasi
├── controller/                     # Controller layer
│   ├── DashboardController.java    # Dashboard dan analytics
│   ├── BarangController.java       # Manajemen barang
│   └── NotaController.java         # Generate nota PDF
├── model/                          # Data model
│   └── Barang.java                 # Entity barang
└── db/                            # Database layer
    └── DBConnection.java           # Database connection manager
```

### Resources Structure:
```
src/main/resources/
├── view/                          # FXML files
│   ├── dashboard-view.fxml        # Dashboard interface
│   ├── barang-view.fxml           # Barang management interface
│   └── Nota.fxml                  # Nota template
└── img/                           # Assets
    └── icon.png                   # Application icon
```

## 🚀 Instalasi dan Penggunaan

### Prerequisites:
1. **Java 21** - Pastikan Java 21 terinstall
2. **XAMPP** - Untuk menjalankan MySQL server
3. **Maven** - Build tool (optional jika menggunakan IDE)

### Langkah Instalasi:

#### 1. Setup Database
```bash
# Jalankan XAMPP dan start MySQL
# Buka phpMyAdmin atau MySQL client
# Buat database baru
CREATE DATABASE tokodepi;
USE tokodepi;

# Import atau buat tabel yang diperlukan
# (Script SQL tersedia di bagian Struktur Database)
```

#### 2. Clone/Download Project
```bash
git clone [repository-url]
cd depistore-manager
```

#### 3. Konfigurasi Database
Edit file `src/main/java/com/depichan/db/DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/tokodepi";
private static final String USER = "root";
private static final String PASSWORD = ""; // Sesuaikan dengan password MySQL
```

#### 4. Compile dan Run
```bash
# Menggunakan Maven
mvn clean compile
mvn javafx:run

# Atau menggunakan IDE (IntelliJ IDEA/Eclipse)
# Import project sebagai Maven project
# Run MainApp.java
```

### Penggunaan Aplikasi:

#### 1. Dashboard
- Aplikasi akan membuka dashboard sebagai halaman utama
- Lihat metrik penjualan dan stok secara real-time
- Gunakan grafik untuk analisis tren penjualan

#### 2. Manajemen Barang
- Klik "Kelola Barang" untuk membuka manajemen barang
- Tambah barang baru dengan tombol "Tambah Barang"
- Edit/hapus barang dengan klik kanan pada baris

#### 3. Penjualan
- Pilih barang dari tabel
- Klik "Jual Barang"
- Masukkan jumlah yang dijual
- Nota PDF akan di-generate otomatis

#### 4. Laporan
- Akses laporan dari dashboard
- Pilih periode (harian/mingguan/bulanan)
- Export ke Excel untuk analisis lebih lanjut

## 📦 Dependencies

### Maven Dependencies (pom.xml):
```xml
<dependencies>
    <!-- JavaFX Framework -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- MySQL Database Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- PDF Generation -->
    <dependency>
        <groupId>com.github.librepdf</groupId>
        <artifactId>openpdf</artifactId>
        <version>1.3.30</version>
    </dependency>
    
    <!-- Excel Export -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.3</version>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.12.0</version>
    </dependency>
</dependencies>
```

## 🔧 Konfigurasi

### Database Configuration:
```java
// File: src/main/java/com/depichan/db/DBConnection.java
private static final String URL = "jdbc:mysql://localhost:3306/tokodepi";
private static final String USER = "root";
private static final String PASSWORD = ""; // Sesuaikan dengan setup MySQL
```

### Application Properties:
- **Window Size**: 1200x800 (minimum 900x700)
- **Database**: MySQL dengan charset UTF-8
- **File Output**: 
  - Nota PDF: `nota/nota-[timestamp].pdf`
  - Laporan Excel: `laporan_[periode]_[timestamp].xlsx`

## 🔐 Fitur Keamanan

### Database Security:
- **PreparedStatement**: Semua query menggunakan prepared statement
- **SQL Injection Prevention**: Parameter binding untuk mencegah SQL injection
- **Connection Pooling**: Manajemen koneksi yang efisien
- **Transaction Management**: Rollback otomatis pada error

### Application Security:
- **Input Validation**: Validasi komprehensif untuk semua input user
- **Double Confirmation**: Konfirmasi berlapis untuk operasi destructive
- **Error Handling**: Penanganan error yang robust tanpa crash
- **Data Integrity**: Constraint database untuk menjaga konsistensi

## 📁 Output Files

### Nota PDF:
- **Lokasi**: `nota/` folder (dibuat otomatis)
- **Format**: `nota-[yyyy-MM-dd-HH-mm-ss].pdf`
- **Konten**: Detail transaksi, barang, harga, total, tanggal

### Laporan Excel:
- **Lokasi**: Root project directory
- **Format**: `laporan_[periode]_[timestamp].xlsx`
- **Konten**: Data transaksi, statistik, summary

## 🐛 Troubleshooting

### Common Issues:

#### 1. Database Connection Error
```
Error: Communications link failure
```
**Solusi:**
- Pastikan XAMPP MySQL service running
- Cek port MySQL (default: 3306)
- Verifikasi username/password di DBConnection.java

#### 2. JavaFX Runtime Error
```
Error: JavaFX runtime components are missing
```
**Solusi:**
- Pastikan menggunakan Java 21 dengan JavaFX support
- Atau gunakan Maven plugin: `mvn javafx:run`

#### 3. PDF Generation Error
```
Error: Cannot create PDF file
```
**Solusi:**
- Pastikan folder `nota/` exists dan writable
- Cek permissions pada directory
- Pastikan OpenPDF dependency ter-load

#### 4. Excel Export Error
```
Error: Cannot write Excel file
```
**Solusi:**
- Pastikan Apache POI dependency ter-load
- Cek disk space untuk file output
- Tutup file Excel yang sedang terbuka

### Debug Mode:
Untuk debugging, enable logging di `DBConnection.java`:
```java
private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
LOGGER.info("Database connection established");
```

## 📊 Performance Tips

### Database Optimization:
- **Indexing**: Primary key dan foreign key sudah ter-index
- **Connection Reuse**: Menggunakan connection pooling
- **Batch Operations**: Untuk update stok multiple items

### Application Optimization:
- **Memory Management**: Proper disposal of resources
- **UI Threading**: Background tasks untuk operasi database
- **Caching**: Cache data untuk performa dashboard

## 🔄 Update & Maintenance

### Regular Tasks:
1. **Database Backup**: Backup database secara berkala
2. **Log Cleanup**: Hapus log files yang sudah lama
3. **File Cleanup**: Hapus nota PDF dan laporan Excel yang tidak diperlukan
4. **Security Update**: Update dependencies secara berkala

### Version Control:
- **Git**: Gunakan Git untuk version control
- **Branching**: Buat branch untuk fitur baru
- **Tagging**: Tag untuk setiap release version

## 🤝 Contributing

### Development Guidelines:
1. Follow Java naming conventions
2. Use meaningful variable and method names
3. Add comments for complex logic
4. Write unit tests for new features
5. Update documentation for changes

### Code Style:
- **Indentation**: 4 spaces
- **Line Length**: Max 120 characters
- **Imports**: Organize imports properly
- **JavaDoc**: Add JavaDoc for public methods

## 👨‍💻 Author & License

**Developer**: Depichan  
**Version**: 1.0-SNAPSHOT  
**Build Tool**: Maven  
**Language**: Java 21  
**Framework**: JavaFX  

### Project Info:
- **GroupId**: com.depichan
- **ArtifactId**: toko-depi
- **Main Class**: com.depichan.MainApp

---

## 📞 Support

Untuk pertanyaan, bug report, atau feature request:
1. Buat issue di repository
2. Sertakan screenshot jika ada error
3. Berikan informasi versi Java dan OS
4. Lampirkan log error jika tersedia

**Happy Coding! 🚀**
