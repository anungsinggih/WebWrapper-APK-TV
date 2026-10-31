# PRD — TV Stream
## Android TV WebView Application

**Dokumen:** Product Requirements & Technical Specification  
**Versi:** 1.0  
**Platform target:** Android TV Box  
**OS pengujian awal:** Android 11 (API 30)  
**Status:** Ready for implementation  
**Nama aplikasi:** TV Stream (working title)

---

## 1. Ringkasan Produk

TV Stream adalah aplikasi Android untuk Android TV Box yang membuka website pilihan pengguna di dalam aplikasi menggunakan Android WebView.

Aplikasi bukan browser umum. Pengguna mengatur satu alamat website HTTPS melalui halaman Settings, lalu aplikasi membuka alamat tersebut dalam mode fullscreen dan menyediakan pengalaman navigasi menggunakan remote control TV.

Aplikasi harus bersifat generik dan tidak mengunci domain tertentu. Pengguna dapat mengganti URL kapan saja tanpa rebuild APK.

**Batas penggunaan:** aplikasi hanya ditujukan untuk website yang dimiliki pengguna atau yang pengguna berwenang akses dan distribusikan. Jangan hardcode, bundling, atau mempromosikan domain layanan streaming pihak ketiga tertentu.

## 2. Tujuan

1. Mengakses website pilihan pengguna melalui satu aplikasi Android TV.
2. Mengoptimalkan interaksi untuk remote control (D-pad, OK/Enter, Back, Menu).
3. Menyediakan pengaturan URL yang dapat diubah tanpa rebuild.
4. Mendukung HTML5 video fullscreen sejauh didukung WebView, website, codec, dan firmware perangkat.
5. Menghasilkan APK yang dapat dipasang melalui sideload pada Android TV Box.
6. Menjaga aplikasi sederhana, ringan, dan mudah dirawat.

## 3. Bukan Tujuan (Out of Scope)

- Membuat layanan streaming atau menyediakan konten video.
- Mengunduh, merekam, atau mengekstrak video dari website.
- Melewati DRM, paywall, login, geo-blocking, anti-bot, atau mekanisme proteksi website.
- Menyediakan daftar situs atau sumber konten.
- Menjamin semua website kompatibel dengan WebView.
- Meniru atau mengubah identitas/merek website yang dibuka.
- Menyediakan casting, account synchronization, atau backend cloud pada MVP.

## 4. Target Pengguna dan Perangkat

### 4.1 Pengguna
Pengguna Android TV Box yang ingin membuka website milik sendiri atau website yang berhak mereka gunakan melalui remote TV.

### 4.2 Target perangkat
- Android TV Box dengan Android 11 sebagai perangkat validasi utama.
- Aplikasi juga diupayakan kompatibel dengan Android 6.0/API 23 ke atas.
- Perangkat tanpa touchscreen harus tetap dapat menjalankan seluruh alur utama.
- Remote control dengan D-pad, OK/Enter, Back, dan Menu/Settings.

### 4.3 Orientasi
- Main screen: landscape.
- Settings: landscape.
- Video fullscreen: mengikuti perilaku WebView/HTML5 dan orientasi layar yang sesuai.

## 5. Prinsip UX

1. **Remote-first:** semua kontrol penting dapat dijangkau tanpa touchscreen.
2. **Simple:** aplikasi langsung membuka website terakhir setelah URL tersimpan.
3. **Predictable:** tombol Back memiliki perilaku konsisten.
4. **Recoverable:** kesalahan jaringan menyediakan cara mencoba kembali dan membuka Settings.
5. **Safe by default:** HTTPS saja; jangan mengaktifkan akses file lokal atau mixed content secara global.
6. **No hidden behavior:** aplikasi tidak mengumpulkan data browsing atau mengirim URL pengguna ke server eksternal.

## 6. Alur Pengguna

### 6.1 First launch
1. Pengguna membuka TV Stream.
2. Jika belum ada URL tersimpan, aplikasi menampilkan Settings.
3. Pengguna memasukkan URL HTTPS.
4. Pengguna memilih **Simpan dan Buka**.
5. Aplikasi memvalidasi URL, menyimpan URL, lalu membuka website.

### 6.2 Subsequent launch
1. Aplikasi membaca URL terakhir dari penyimpanan lokal.
2. Jika URL valid dan tersedia, WebView memuat URL tersebut.
3. Jika URL belum tersimpan, buka Settings.

### 6.3 Change URL
1. Pengguna menekan tombol Menu/Settings pada remote.
2. Aplikasi membuka halaman Settings.
3. URL saat ini ditampilkan.
4. Pengguna mengganti URL dan menekan Simpan dan Buka.
5. Aplikasi memvalidasi dan menyimpan URL baru.
6. Main screen memuat URL baru.

### 6.4 Navigation
- D-pad: navigasi fokus pada elemen website.
- OK/Enter: aktivasi elemen yang sedang fokus.
- Back: tutup video fullscreen jika sedang aktif; jika tidak, kembali ke halaman sebelumnya di WebView; jika tidak ada riwayat, tampilkan dialog keluar atau biarkan aplikasi berada di halaman awal sesuai keputusan implementasi.
- Menu/Settings: buka halaman Settings.
- Jika remote tidak memiliki Menu/Settings, sediakan fallback yang terdokumentasi (misalnya tombol keyboard Menu atau shortcut yang tidak mengganggu input website).

### 6.5 Network error
1. Jika main-frame gagal dimuat, tampilkan error state native yang jelas.
2. Sediakan tombol **Coba Lagi**, **Pengaturan**, dan **Keluar**.
3. Jangan mengganti halaman dengan konten dari domain lain.
4. Error subresource tidak boleh menggantikan seluruh halaman.

## 7. Functional Requirements

### FR-01 — URL Settings
- Pengguna dapat memasukkan alamat website.
- URL wajib menggunakan skema `https://`.
- URL wajib memiliki hostname valid.
- Tolak URL dengan username/password di dalam authority.
- Trim whitespace.
- Jangan menerima `javascript:`, `file:`, `content:`, `intent:`, `data:`, `http:`, atau skema non-HTTPS.
- Simpan URL secara lokal menggunakan Preferences/DataStore.
- Tampilkan URL tersimpan saat Settings dibuka.
- Sediakan tombol Simpan dan Buka.
- Sediakan tombol Batal/Kembali.
- Sediakan tombol Hapus URL Tersimpan dengan konfirmasi.

### FR-02 — WebView
- Gunakan Android WebView resmi dari Android System WebView/Chrome provider perangkat.
- JavaScript dan DOM storage dapat diaktifkan untuk kompatibilitas website.
- Media playback dapat berjalan tanpa gesture hanya jika dibutuhkan pengalaman TV; dokumentasikan implikasi UX dan uji pada perangkat.
- Nonaktifkan file access dan content access kecuali ada kebutuhan yang dibuktikan.
- Nonaktifkan mixed content.
- Jangan mengaktifkan debugging WebView pada release build.
- Gunakan WebViewClient dan WebChromeClient yang sesuai.
- Tangani lifecycle WebView dengan benar.

### FR-03 — Navigation Policy
- Navigasi utama hanya boleh menuju HTTPS.
- Terapkan validasi skema pada semua navigasi yang dikendalikan aplikasi.
- Jangan membuka skema eksternal secara otomatis.
- Jika website membutuhkan aplikasi eksternal atau skema khusus, tampilkan pesan yang aman dan jangan crash.
- Pertimbangkan allowlist hostname opsional di Settings sebagai hardening tambahan, tetapi jangan wajibkan untuk MVP karena pengguna perlu bisa mengganti domain.
- Jangan mengklaim bahwa pemeriksaan skema saja menjamin keamanan website.

### FR-04 — Remote Control
- D-pad Up/Down/Left/Right: navigasi fokus WebView atau komponen native.
- Center/Enter: klik/aktivasi elemen fokus.
- Back: urutan perilaku sesuai bagian 6.4.
- Menu/Settings: buka Settings.
- Semua tombol Settings harus dapat difokuskan dan dioperasikan tanpa touchscreen.
- Fokus harus terlihat jelas (focus indicator).
- Jangan menangkap tombol D-pad secara global jika itu menghalangi navigasi halaman website.
- Uji beberapa variasi remote dan keycode Android yang umum.

### FR-05 — Video Fullscreen
- Implementasikan `WebChromeClient.onShowCustomView()` dan `onHideCustomView()`.
- Tampilkan custom video view pada container fullscreen.
- Sembunyikan WebView selama custom view tampil.
- Pulihkan WebView dan system UI setelah fullscreen ditutup.
- Tangani Back untuk keluar dari fullscreen terlebih dahulu.
- Pastikan callback fullscreen dipanggil satu kali dan view dibersihkan saat Activity dihancurkan.
- Jangan mencoba mengekstrak URL media atau melewati DRM.
- Ketersediaan codec, DRM, subtitle, dan kualitas video bergantung pada website, WebView provider, dan firmware.

### FR-06 — Fullscreen / Immersive
- Main screen menggunakan immersive fullscreen.
- Status bar dan navigation bar disembunyikan saat pengalaman browsing.
- System UI harus dipulihkan dengan benar ketika aplikasi keluar atau dialog native ditampilkan.
- Jangan menghalangi kontrol fullscreen video milik website.

### FR-07 — Loading State
- Tampilkan loading indicator ketika main-frame mulai dimuat.
- Hilangkan loading indicator saat halaman selesai atau gagal.
- Jangan menutupi halaman terlalu lama jika callback tidak diterima.
- Loading tidak boleh memblokir remote Back atau Settings.

### FR-08 — Error State
- Tangani error jaringan, DNS, timeout, SSL, dan HTTP error yang relevan.
- Jangan mengabaikan kesalahan sertifikat SSL.
- Jangan pernah menggunakan handler yang menerima sertifikat SSL tidak valid.
- Tampilkan pesan ringkas dan tindakan pemulihan.
- Sediakan retry.
- Bedakan error main-frame dari error resource pendukung.
- Jangan menampilkan detail teknis sensitif yang tidak perlu.

### FR-09 — Cache dan Reload
- Sediakan Reload Website.
- Sediakan Clear Cache di Settings dengan konfirmasi.
- Clear Cache tidak boleh menghapus data login/cookies kecuali pengguna memilih opsi terpisah.
- Sediakan opsi Hapus Data Website/Cookies secara terpisah bila diimplementasikan.
- Jangan menghapus cookies secara otomatis setiap kali aplikasi dibuka.

### FR-10 — App Lifecycle
- Pertahankan state yang wajar saat Activity pause/resume.
- Hindari membuat ulang WebView tanpa kebutuhan.
- Bersihkan WebView dan custom fullscreen view pada lifecycle yang tepat.
- Jangan menjaga CPU/wakelock tanpa kebutuhan.
- Opsi keep-screen-on hanya aktif ketika pengguna sedang menggunakan aplikasi dan harus dievaluasi untuk konsumsi daya.

## 8. Settings Screen Requirements

Halaman Settings minimal memuat:

| Elemen | Perilaku |
|---|---|
| Website URL | Input URL HTTPS |
| Simpan dan Buka | Validasi, simpan, lalu buka |
| Test Connection | Opsional; cek koneksi HTTPS secara aman tanpa mengeksekusi konten |
| Reload Website | Kembali ke Main dan reload URL |
| Clear Cache | Hapus cache WebView setelah konfirmasi |
| Hapus URL | Hapus URL tersimpan setelah konfirmasi |
| Tentang | Versi aplikasi dan informasi WebView |

### Settings UX
- Layout landscape.
- Ukuran teks nyaman dibaca dari jarak TV.
- Kontras tinggi.
- Tombol besar.
- Focus state jelas.
- Input URL harus dapat digunakan dengan remote/keyboard eksternal.
- Jangan mengasumsikan remote memiliki keyboard.
- Jika keyboard on-screen perangkat tidak muncul untuk EditText, dokumentasikan workaround input melalui keyboard USB/Bluetooth.

## 9. Non-Functional Requirements

### NFR-01 — Performance
- Cold start wajar pada TV Box kelas entry-level.
- Tidak membuat WebView ganda.
- Tidak memuat URL lebih dari sekali tanpa alasan.
- Gunakan hardware acceleration default kecuali ada bug perangkat yang teridentifikasi.

### NFR-02 — Reliability
- Aplikasi tidak crash ketika jaringan putus.
- Aplikasi tidak crash ketika website gagal dimuat.
- Aplikasi tidak crash ketika video fullscreen dibuka/ditutup berulang.
- URL tersimpan tetap tersedia setelah aplikasi ditutup atau perangkat restart.

### NFR-03 — Security & Privacy
- HTTPS only.
- Mixed content disabled.
- File/content access disabled.
- Safe Browsing aktif jika tersedia pada WebView provider.
- Jangan bypass SSL errors.
- Jangan menyimpan password website di aplikasi.
- Jangan mengirim telemetry atau browsing history.
- Jangan menambahkan analytics/ads SDK.
- Jangan membuka intent eksternal tanpa persetujuan pengguna.
- Pertimbangkan third-party cookies: default nonaktif; jika website tepercaya membutuhkan login yang bergantung padanya, jelaskan trade-off sebelum mengaktifkan.

### NFR-04 — Compatibility
- Minimum SDK target implementasi: API 23 kecuali ada alasan teknis.
- Perangkat validasi utama: Android 11/API 30.
- Target SDK ditentukan dengan mempertimbangkan kebijakan distribusi dan kompatibilitas; untuk sideload internal, dokumentasikan pilihan.
- Uji dengan WebView provider yang terpasang dan diperbarui pada perangkat.

### NFR-05 — Accessibility
- Ukuran teks terbaca dari jarak TV.
- Focus state kontras.
- Semua aksi utama dapat dijalankan dengan remote.
- Jangan hanya mengandalkan warna untuk status.

## 10. Rekomendasi Teknologi

- Kotlin.
- Android Studio.
- Android Gradle Plugin versi stabil yang kompatibel.
- AndroidX seperlunya.
- Android WebView.
- Preferences DataStore atau SharedPreferences untuk konfigurasi sederhana.
- Native Android Views atau Jetpack Compose; pilih satu pendekatan UI dan konsisten.
- Gradle Kotlin DSL.

Jangan menambahkan dependency besar tanpa kebutuhan yang jelas.

## 11. Arsitektur yang Disarankan

Contoh struktur:

```text
app/
  src/main/
    AndroidManifest.xml
    java/<package>/
      MainActivity.kt
      SettingsActivity.kt
      web/
        TvWebViewClient.kt
        TvWebChromeClient.kt
        WebViewFactory.kt
        NavigationPolicy.kt
      settings/
        UrlPreferences.kt
      ui/
        ErrorView.kt
        LoadingView.kt
        SettingsView.kt
      util/
        ImmersiveMode.kt
        RemoteKeyMapper.kt
    res/
      drawable/
      mipmap/
      values/
```

Tanggung jawab:
- `MainActivity`: lifecycle, layar utama, integrasi WebView.
- `SettingsActivity`/Settings screen: konfigurasi URL dan tindakan pengguna.
- `WebViewFactory`: konfigurasi WebView terpusat.
- `TvWebViewClient`: navigation policy dan network error.
- `TvWebChromeClient`: progress, JavaScript dialogs bila diperlukan, video fullscreen.
- `UrlPreferences`: simpan/baca URL.
- `RemoteKeyMapper`: mapping tombol remote yang benar-benar dibutuhkan.
- `ErrorView`: state error dan recovery actions.

Arsitektur boleh disederhanakan bila tetap mudah dites dan dipelihara.

## 12. Android Manifest & Platform

Pastikan:
- `INTERNET` permission tersedia.
- `ACCESS_NETWORK_STATE` hanya jika benar-benar digunakan.
- Main Activity memiliki launcher intent.
- Tambahkan `LEANBACK_LAUNCHER` agar muncul di launcher Android TV.
- Touchscreen tidak diwajibkan.
- Main Activity landscape.
- Label aplikasi dan icon launcher tersedia.
- `usesCleartextTraffic=false`.
- Jangan menambahkan permission yang tidak diperlukan.

Catatan: `LEANBACK_LAUNCHER` dan visibilitas aplikasi di launcher dapat berbeda menurut firmware TV Box.

## 13. Acceptance Criteria

MVP dianggap selesai jika seluruh kriteria berikut lolos:

1. Fresh install membuka Settings ketika URL belum ada.
2. URL HTTP ditolak.
3. URL HTTPS valid dapat disimpan.
4. URL tetap tersimpan setelah aplikasi ditutup dan dibuka kembali.
5. Aplikasi membuka URL tersimpan saat startup.
6. Pengguna dapat mengganti URL tanpa rebuild APK.
7. D-pad dapat mengoperasikan Settings.
8. OK/Enter dapat memilih tombol dan elemen website.
9. Back kembali ke halaman sebelumnya.
10. Back menutup video fullscreen sebelum kembali ke halaman sebelumnya.
11. Tombol Menu/Settings membuka Settings.
12. Network failure menampilkan error state dan retry.
13. SSL certificate error tidak pernah di-bypass.
14. Aplikasi tidak crash ketika orientasi/video/lifecycle berubah.
15. Aplikasi tampil di launcher Android TV jika firmware mendukung kategori Leanback.
16. APK debug berhasil dibangun dan dipasang pada Android 11 TV Box.
17. Release APK dapat dibangun dengan signing key milik pemilik aplikasi.

## 14. Test Plan

### 14.1 Settings
- Fresh install, URL kosong.
- Simpan URL valid.
- Simpan URL tanpa scheme.
- Simpan URL HTTP.
- Simpan URL dengan hostname kosong.
- Simpan URL dengan user-info.
- Ganti URL.
- Hapus URL.
- Reopen app dan pastikan URL bertahan.

### 14.2 Remote
- D-pad pada halaman Settings.
- D-pad pada website.
- OK/Enter.
- Back dengan history.
- Back tanpa history.
- Menu/Settings.
- Remote tanpa tombol Menu.
- Keyboard USB/Bluetooth.

### 14.3 WebView & Network
- Website HTTPS normal.
- DNS gagal.
- Internet terputus ketika loading.
- Timeout.
- SSL certificate invalid (harus ditolak).
- Redirect HTTPS.
- Redirect ke HTTP (harus ditolak atau diblokir sesuai policy).
- Pop-up/new window.
- External intent.
- Website dengan JavaScript dan DOM storage.

### 14.4 Video
- Mulai video HTML5.
- Masuk fullscreen.
- Keluar fullscreen dengan kontrol player.
- Keluar fullscreen dengan Back.
- Ulangi fullscreen beberapa kali.
- Pause/resume aplikasi saat video aktif.
- Periksa subtitle dan audio jika didukung sumber uji yang legal.

### 14.5 Device
- Android 11 TV Box.
- Uji resolusi 720p dan 1080p bila tersedia.
- Uji remote bawaan.
- Uji performa pada jaringan Wi-Fi dan Ethernet bila tersedia.

## 15. Build & Delivery

OpenCode harus:
1. Memeriksa environment dan versi Gradle/JDK/SDK.
2. Membuat project Android yang dapat dibuka di Android Studio.
3. Menjalankan Gradle sync/build.
4. Menjalankan unit test yang relevan.
5. Memperbaiki error compile.
6. Menghasilkan debug APK.
7. Menyediakan instruksi sideload ke Android TV Box.
8. Menjelaskan cara membuat release APK bertanda tangan.
9. Tidak menyertakan signing key atau password di repository.
10. Menyediakan README dengan build, install, kontrol remote, dan troubleshooting.

Output yang diharapkan:
- Source code lengkap.
- `README.md`.
- Debug APK di `app/build/outputs/apk/debug/`.
- Release build instructions.
- Daftar pengujian yang sudah dan belum dilakukan.

### Perintah build yang diharapkan

```bash
./gradlew assembleDebug
```

Jika Gradle wrapper belum ada, buat wrapper menggunakan versi Gradle yang kompatibel dengan Android Gradle Plugin yang dipilih.

Untuk release:

```bash
./gradlew assembleRelease
```

Release APK harus ditandatangani dengan key milik pemilik aplikasi. Jangan membuat atau mengunggah keystore rahasia ke repository.

## 16. Instruksi Implementasi untuk OpenCode

Baca PRD ini secara menyeluruh sebelum mengubah file.

Kerjakan bertahap:
1. Inspect environment dan repository.
2. Buat project skeleton dan konfigurasi Gradle.
3. Implementasikan URL settings dan persistence.
4. Implementasikan WebView dengan security defaults.
5. Implementasikan remote navigation dan Settings shortcut.
6. Implementasikan loading/error state.
7. Implementasikan video fullscreen lifecycle.
8. Implementasikan launcher Android TV.
9. Tambahkan test dan README.
10. Build APK dan perbaiki semua compile error.

Jangan menganggap implementasi selesai hanya karena source code sudah dibuat. Jalankan build nyata dan laporkan hasilnya.

Jika ada requirement yang tidak bisa dipenuhi karena keterbatasan Android WebView atau firmware, dokumentasikan secara eksplisit. Jangan membuat klaim kompatibilitas tanpa pengujian.

Jangan hardcode URL tertentu. Saat pertama kali dibuka, tampilkan Settings dan minta pengguna memasukkan URL HTTPS miliknya atau URL yang berhak ia gunakan.

## 17. Risiko dan Batasan yang Harus Didokumentasikan

- Beberapa website mendeteksi atau tidak mendukung WebView.
- Website desktop/mobile mungkin tidak dioptimalkan untuk layar TV.
- Navigasi fokus D-pad pada website ditentukan juga oleh HTML/CSS website.
- Beberapa remote memetakan tombol Menu/Settings secara berbeda.
- Video playback bergantung pada codec, DRM, browser engine, dan firmware.
- DRM-protected content tidak dijamin dapat diputar.
- Pop-up, OAuth, external app links, dan third-party cookies dapat membutuhkan penanganan khusus.
- Android TV Box murah dapat memiliki WebView lama atau implementasi firmware yang tidak stabil.

---

**Definition of Done:** Aplikasi dapat dibangun menjadi APK, dipasang pada Android 11 TV Box, membuka URL HTTPS pilihan pengguna, dapat dinavigasi dengan remote, dan menangani Settings, error, Back, serta fullscreen video sesuai acceptance criteria yang telah diuji.
