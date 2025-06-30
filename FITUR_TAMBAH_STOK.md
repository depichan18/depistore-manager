# Fitur Tambah Stok

## Deskripsi
Fitur ini memungkinkan pengguna untuk menambahkan stok barang dalam aplikasi manajemen toko. Fitur ini mendukung penambahan stok untuk banyak barang sekaligus dalam satu operasi.

## Cara Menggunakan:
1. Klik tombol **"Tambah Stok"**
2. Masukkan data dalam format: `ID_Barang,Jumlah_Stok`
3. Satu baris per barang
4. Contoh input:
   ```
   1,50
   2,30
   3,100
   ```
5. Klik "Tambah" untuk memproses

## Fitur:
- Proses batch untuk banyak barang sekaligus
- Validasi format input
- Validasi keberadaan barang berdasarkan ID
- Transaksi database (rollback jika ada error)
- Laporan hasil: berhasil vs gagal
- Detail error untuk setiap baris yang gagal

## Validasi yang Diterapkan:
- Input harus berupa angka
- Jumlah stok harus lebih dari 0
- ID barang harus ada di database
- Format input harus benar (ID,Jumlah)

## Keamanan Database:
- Menggunakan PreparedStatement untuk mencegah SQL injection
- Transaksi database untuk batch update (commit/rollback)
- Validasi data sebelum update ke database

## File yang Dimodifikasi:
1. `BarangController.java` - Method `onTambahStok()` untuk batch update
2. `barang-view.fxml` - Tombol "Tambah Stok"
3. `Barang.java` - Setter methods untuk update data

## Struktur Database:
Fitur ini mengupdate tabel `barang` dengan query:
```sql
UPDATE barang SET stok = stok + ? WHERE id_barang = ?
```

## Catatan Penting:
- Pastikan koneksi database ke MySQL/XAMPP sudah aktif
- Tabel `barang` harus sudah ada dengan kolom: `id_barang`, `stok`
- Aplikasi akan menampilkan pesan error jika ada masalah koneksi database
- Untuk menambah stok satu barang saja, cukup masukkan satu baris: `ID,Jumlah`
