# Credit Simulator

Aplikasi terminal untuk memvalidasi data pinjaman kendaraan dan menghitung cicilan per tahun.
Kode sumber utama berada di `src/main/java`; komentar pada class, metode, dan bagian algoritma
menjelaskan tanggung jawab setiap komponen tanpa memenuhi setiap baris dengan komentar yang sama.

## Alur aplikasi

1. `Main` memilih mode interaktif atau mode file berdasarkan argumen program.
2. `CreditSimulatorController` meminta jenis kendaraan, kondisi, tahun, total pinjaman, tenor,
   dan uang muka. Setiap jawaban divalidasi sebelum pertanyaan berikutnya.
3. `VehicleFactory` membentuk model `Car` atau `Motor`; `LoanRequest` menyimpan data pinjaman.
   Pokok yang dibiayai dihitung sebagai total pinjaman dikurangi uang muka.
4. `LoanCalculator` menghitung cicilan untuk tiap tahun dengan bantuan `InterestPolicy`.
5. `ConsoleView` dan `InstallmentFormatter` menampilkan cicilan bulanan dan bunga per tahun.

Mode interaktif menyediakan perintah `show`, `new`, `load`, `save`, `sheet`, dan `exit`.
Perhitungan terakhir dari `new` atau `load` dapat disimpan di workbook sementara dengan
`save <nama>`, dan dibuka kembali dengan `sheet <nama>`; `sheet` tanpa argumen menampilkan
daftar sheet tersimpan. Data workbook hanya tersedia selama aplikasi berjalan. Perintah `load`
mengambil data JSON dari URL (bawaan atau dari environment variable
`CREDIT_SIMULATOR_LOAD_URL`), lalu `LoanRequestMapper` memparse dan memvalidasi field yang sama
dengan mode input manual. URL dapat memakai `http://`, `https://`, atau `file://`.

Mode file membaca jawaban berurutan: jenis kendaraan, kondisi, tahun, total pinjaman, tenor,
lalu uang muka. Baris kosong dan baris yang diawali `#` diabaikan. Contoh tersedia di
`sample.txt`.

Aplikasi dapat dijalankan melalui `./credit_simulator` atau `bin/credit_simulator`, dengan
opsional path file input sebagai argumen. Launcher membutuhkan Java 17+ dan membangun ulang JAR
dengan Maven jika belum ada atau source lebih baru. `CREDIT_SIMULATOR_JAR` dapat menunjuk ke JAR
siap pakai untuk melewati build. Uji launcher dapat dijalankan dengan
`test/credit_simulator_test.sh`; uji tersebut menggunakan Java dan Maven palsu.

## Validasi input

- Jenis kendaraan harus `Motor` atau `Mobil`; kondisi harus `Baru` atau `Bekas`.
- Tahun kendaraan harus empat digit. Kendaraan baru tidak boleh lebih lama dari tahun berjalan
  dikurangi satu.
- Total pinjaman harus lebih dari nol dan paling banyak Rp1.000.000.000.
- Tenor harus 1 sampai 6 tahun.
- Uang muka harus lebih kecil daripada total pinjaman; minimum 35% untuk kendaraan baru dan 25%
  untuk kendaraan bekas.

## Algoritma bunga dan cicilan

Pokok awal `P1` adalah total pinjaman dikurangi uang muka. Bunga dasar mobil 8% dan motor 9%.
Tahun pertama memakai bunga dasar; untuk setiap tahun sesudahnya, bunga bertambah 0,1 poin
persentase pada tahun genap dan 0,5 poin persentase pada tahun ganjil. Contoh untuk mobil:
8% pada tahun pertama, 8,1% pada tahun kedua, dan 8,6% pada tahun ketiga.

Untuk setiap tahun cicilan `y`:

1. Hitung bunga tahun tersebut `r_y` dari jenis kendaraan dan nomor tahun.
2. Hitung total saldo setelah bunga: `T_y = P_y * (1 + r_y / 100)`.
3. Hitung bulan tersisa: `B_y = 12 * (tenor - y + 1)`.
4. Hitung cicilan bulanan: `M_y = T_y / B_y`.
5. Hitung pokok awal tahun berikutnya: `P_(y+1) = T_y - 12 * M_y`.

Perhitungan memakai `BigDecimal`, presisi internal 10 angka desimal, dan pembulatan tampilan
uang menjadi dua angka desimal dengan metode half-up. `M_y` memakai bulan yang masih tersisa
pada awal tahun tersebut; ini adalah rumus yang diterapkan aplikasi, bukan simulasi tabel
amortisasi bulanan standar.
