// File: src/main/java/com/depichan/model/Barang.java
package com.depichan.model;

public class Barang {
    private int id;
    private String nama;
    private String kategori;
    private int harga;
    private int stok;
    private String tanggal;

    public Barang(int id, String nama, String kategori, int harga, int stok, String tanggal) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
        this.tanggal = tanggal;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getKategori() { return kategori; }
    public int getHarga() { return harga; }
    public int getStok() { return stok; }
    public String getTanggal() { return tanggal; }
}