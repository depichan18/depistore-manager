# Fitur Context Menu (Klik Kanan) pada Tabel

## Deskripsi
Fitur ini menambahkan menu konteks (context menu) yang muncul saat klik kanan pada baris tabel barang. Menu ini menyediakan akses cepat untuk operasi yang sering digunakan pada barang yang dipilih.

## Cara Menggunakan:
1. **Klik kanan** pada baris barang dalam tabel
2. Pilih salah satu opsi dari menu yang muncul:
   - **Edit Barang**
   - **Tambah Stok Manual**
   - **Hapus Barang**

## Fitur-Fitur Menu Konteks

### 1. 📝 **Edit Barang**
Mengedit informasi barang yang dipilih.

#### Cara Menggunakan:
1. Klik kanan pada barang → pilih "Edit Barang"
2. Form edit akan muncul dengan data saat ini:
   - **Nama Barang** (bisa diubah)
   - **Kategori** (ComboBox editable)
   - **Harga** (bisa diubah)
   - **Stok** (bisa diubah)
3. Ubah data yang diperlukan
4. Klik "Simpan" untuk menyimpan perubahan

#### Validasi:
- Nama barang harus unik (tidak boleh sama dengan barang lain)
- Harga harus lebih dari 0
- Stok tidak boleh negatif
- Semua field harus diisi

### 2. 📦 **Tambah Stok Manual**
Menambah stok untuk barang yang dipilih (alternatif dari fitur batch).

#### Cara Menggunakan:
1. Klik kanan pada barang → pilih "Tambah Stok Manual"
2. Masukkan jumlah stok yang ingin ditambahkan
3. Klik OK untuk konfirmasi

#### Keunggulan:
- Lebih cepat untuk satu barang
- Tidak perlu mengingat ID barang
- Langsung memilih dari tabel

### 3. 🗑️ **Hapus Barang**
Menghapus barang beserta riwayat penjualannya dengan konfirmasi berlapis.

#### Cara Menggunakan:
1. Klik kanan pada barang → pilih "Hapus Barang"
2. **Konfirmasi Tahap 1**: Konfirmasi umum penghapusan
3. **Konfirmasi Tahap 2**: Peringatan detail dengan informasi lengkap barang

#### Fitur Keamanan:
- **Double Confirmation**: Dua tahap konfirmasi untuk mencegah penghapusan tidak sengaja
- **Detail Preview**: Menampilkan informasi lengkap barang yang akan dihapus
- **Transaction Safety**: Menggunakan database transaction
- **Cascade Delete**: Otomatis menghapus riwayat penjualan terkait

#### Data yang Dihapus:
- Data barang dari tabel `barang`
- Semua riwayat penjualan terkait dari tabel `penjualan`
- Laporan jumlah record yang dihapus

## Keamanan & Validasi

### Validasi Umum:
- Harus memilih barang terlebih dahulu
- Validasi input untuk semua operasi
- Konfirmasi untuk operasi yang berisiko

### Keamanan Database:
- PreparedStatement untuk mencegah SQL injection
- Database transaction untuk operasi kompleks
- Rollback otomatis jika ada error
- Validasi duplikasi nama barang

### Error Handling:
- Pesan error yang informatif
- Rollback transaction jika ada kegagalan
- Refresh otomatis setelah operasi berhasil

## Struktur Database yang Terlibat

### Query Edit Barang:
```sql
-- Cek duplikasi nama (kecuali barang yang sedang diedit)
SELECT COUNT(*) FROM barang WHERE nama_barang = ? AND id_barang != ?

-- Update barang
UPDATE barang SET nama_barang = ?, kategori = ?, harga = ?, stok = ? WHERE id_barang = ?
```

### Query Tambah Stok:
```sql
UPDATE barang SET stok = stok + ? WHERE id_barang = ?
```

### Query Hapus Barang:
```sql
-- Hapus riwayat penjualan terlebih dahulu
DELETE FROM penjualan WHERE id_barang = ?

-- Hapus barang
DELETE FROM barang WHERE id_barang = ?
```

## File yang Dimodifikasi:
1. **`BarangController.java`** - Menambah method:
   - `setupTableContextMenu()` - Setup context menu
   - `editBarang()` - Dialog edit barang
   - `tambahStokManual()` - Tambah stok untuk satu barang
   - `hapusBarang()` - Hapus barang dengan double confirmation

## Catatan Penting:
- **Penghapusan Permanen**: Hapus barang bersifat permanen dan tidak dapat dibatalkan
- **Backup Database**: Disarankan backup sebelum operasi hapus
- **Cascade Delete**: Menghapus barang akan menghapus semua riwayat penjualannya
- **Transaction Safety**: Semua operasi menggunakan database transaction

## Tips Penggunaan:
- Gunakan "Edit Barang" untuk memperbaiki kesalahan input
- "Tambah Stok Manual" untuk penambahan stok cepat satu barang
- Hati-hati dengan "Hapus Barang" - bacalah konfirmasi dengan teliti
- Backup data secara berkala
- Periksa kembali data sebelum menyimpan perubahan
