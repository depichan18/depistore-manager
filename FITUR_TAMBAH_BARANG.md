# Fitur Tambah Barang Baru

## Deskripsi
Fitur ini memungkinkan pengguna untuk menambahkan barang baru ke dalam database aplikasi manajemen toko dengan kemudahan dalam mengelola kategori.

## Cara Menggunakan:
1. Klik tombol **"Tambah Barang"**
2. Isi form yang muncul:
   - **Nama Barang**: Nama unik untuk barang baru
   - **Kategori**: Pilih dari dropdown atau ketik kategori baru langsung
   - **Harga**: Harga jual barang (dalam rupiah)
   - **Stok Awal**: Jumlah stok awal barang
3. Klik "Tambah" untuk menyimpan

## Fitur Kategori:
- **ComboBox Editable**: Bisa pilih kategori yang sudah ada atau ketik kategori baru
- **Auto-Load**: Kategori yang sudah ada otomatis dimuat dari database
- **Kategori Baru**: Ketik langsung kategori baru, akan otomatis tersimpan saat barang ditambahkan
- **Fleksibilitas Tinggi**: Tidak perlu menu khusus untuk mengelola kategori

## Validasi yang Diterapkan:
- Semua field harus diisi
- Nama barang harus unik (tidak boleh duplikat)
- Harga harus lebih dari 0
- Stok tidak boleh negatif
- Input harga dan stok harus berupa angka

## Keamanan & Validasi

### Validasi Input:
- Nama barang: Tidak boleh kosong dan harus unik
- Kategori: Tidak boleh kosong
- Harga: Harus angka positif
- Stok: Harus angka tidak negatif

### Keamanan Database:
- Menggunakan PreparedStatement untuk mencegah SQL injection
- Validasi duplikasi sebelum insert
- Tanggal masuk otomatis (timestamp saat ini)

## Struktur Database

### Tabel `barang`:
- `id_barang` (AUTO_INCREMENT)
- `nama_barang` (VARCHAR, UNIQUE)
- `kategori` (VARCHAR)
- `harga` (INT)
- `stok` (INT)
- `tanggal_masuk` (DATE/DATETIME)

### Query yang Digunakan:
```sql
-- Tambah barang baru
INSERT INTO barang (nama_barang, kategori, harga, stok, tanggal_masuk) 
VALUES (?, ?, ?, ?, NOW())

-- Cek duplikasi nama
SELECT COUNT(*) FROM barang WHERE nama_barang = ?

-- Load kategori yang ada
SELECT DISTINCT kategori FROM barang ORDER BY kategori
```

## File yang Dimodifikasi:
1. **`BarangController.java`** - Method:
   - `onTambahBarang()` - Form tambah barang baru
   - `loadKategoriToComboBox()` - Helper load kategori ke ComboBox

2. **`barang-view.fxml`** - Menambah tombol:
   - "Tambah Barang"

## Catatan Penting:
- Pastikan koneksi database ke MySQL/XAMPP sudah aktif
- Tabel `barang` harus sudah ada dengan struktur yang sesuai
- Nama barang harus unik di seluruh database
- Kategori baru akan otomatis tersedia setelah barang pertama dengan kategori tersebut ditambahkan

## Tips Penggunaan:
- Gunakan kategori yang konsisten (misal: "Makanan", "Minuman", "Elektronik")
- Periksa dropdown kategori sebelum mengetik kategori baru
- Gunakan nama barang yang deskriptif dan mudah dicari
- Kategori baru langsung tersedia untuk barang selanjutnya setelah disimpan
