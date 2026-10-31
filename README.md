# TV Stream

Android TV WebView wrapper for one user-configured HTTPS website. No domain, content source, analytics, ads, or telemetry is bundled.

## Build

Requirements: JDK 17, Android SDK platform 35 or newer, Android Build Tools, and network access for the first Gradle dependency download.

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

Release builds are unsigned unless an owner supplies signing configuration. Do not commit a keystore or password. For a local release build, configure `signingConfigs` in a private `~/.gradle/gradle.properties`-driven convention, then run:

```bash
./gradlew assembleRelease
```

## Install

Enable developer options and USB/network debugging on the TV Box, then use `adb`:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch **TV Stream** from the Android TV launcher. Fresh install opens Settings. Enter an HTTPS URL, select **Simpan dan Buka**, and operate buttons with D-pad plus OK. Menu/Settings opens native Settings; Back closes video fullscreen first, then WebView history.

Pada HP, aplikasi mengikuti orientasi perangkat dan memakai layout responsif: padding, ukuran teks, tinggi tombol, dan Settings scroll menyesuaikan layar portrait maupun landscape. Tombol **Menu** selalu terlihat di pojok kanan atas layar utama HP dan membuka Settings. Pada Android TV, layout tetap memakai ukuran dan fullscreen TV yang nyaman dari jarak jauh.

## Security behavior

Only HTTPS URLs with a hostname and no URL username/password are accepted. HTTP, file/content/data/javascript/intent schemes and external navigations are blocked. WebView file/content access and mixed content are disabled; SSL errors are cancelled, never bypassed. Cookies and login data remain unless the WebView provider manages them; Clear Cache does not clear cookies.

URL input tanpa scheme otomatis dinormalisasi ke HTTPS. Contoh `google.com` menjadi `https://google.com`. Scheme eksplisit selain HTTPS, seperti `http://`, tetap ditolak.

Custom Filter URLs menerima beberapa URL HTTPS, satu URL per baris. Semua list diunduh dan divalidasi sebagai satu batch; jika satu list gagal, filter aktif lama tetap dipakai.

Popup/new window dari iklan atau `target=_blank` ditolak oleh WebView (`onCreateWindow()` mengembalikan `false`). WebView utama tidak diganti dan video tetap berada pada halaman/player yang sedang berjalan. Ini mencegah blank page yang membuat video kembali dari awal. Link normal HTTPS tetap mengikuti navigasi utama.

Filter list besar seperti uAssets boleh dimasukkan beberapa URL, satu URL per baris. Parser hanya menyimpan rule network yang didukung engine; komentar, exception rule, regex, dan cosmetic rule dilewati. Batas tetap 5 MB per list, 20 MB total, dan 100.000 rule supported; rule di atas batas dipotong dari snapshot baru, bukan menggagalkan penyimpanan konfigurasi.

Jika website memakai redirect top-level lintas-domain untuk login atau payment, redirect tersebut dapat diblokir oleh popup protection. Tambahkan dukungan allowlist domain khusus bila alur legal website membutuhkannya.

## Ad Blocker

Settings → **Ad Blocker** menyediakan enable/disable, built-in filter sets, custom uBO-subset/hosts rules, HTTPS custom filter URL, whitelist hostname, update manual, dan blocked request counter. Built-in snapshot bertanggal `2026-11-01` berisi subset curated yang mengikuti sintaks dari EasyList (`https://easylist.to/easylist/easylist.txt`), EasyPrivacy (`https://easylist.to/easylist/easyprivacy.txt`), dan AdGuard DNS Filter (`https://filters.adtidy.org/extension/chromium/filters/2.txt`); ini bukan full list resmi. Filtering berjalan lokal pada `WebViewClient.shouldInterceptRequest`; main-frame navigation tidak diblokir oleh filter engine. Filter engine mendukung domain/token rules umum seperti `||ads.example^` dan `ads.example`, bukan seluruh sintaks uBO. Filter remote hanya diunduh dari HTTPS, dibatasi 5 MB/100.000 rule, diparse di worker thread, dan dipasang atomik setelah validasi berhasil. Response blokir memakai HTTP 204 dengan body kosong. `shouldInterceptRequest()` tidak mencakup semua trafik WebView seperti sebagian WebSocket/internal browser traffic.

## Limitations and troubleshooting

- Website focus navigation depends on its HTML/CSS. USB/Bluetooth keyboard can help enter long URLs when TV on-screen keyboard is unavailable.
- HTML5 video fullscreen depends on device codec, WebView provider, firmware, website, and DRM. DRM bypass and media extraction are not implemented.
- Some websites reject embedded WebView, require OAuth/external apps, or require third-party cookies. External schemes are intentionally not opened automatically.
- Menu key mappings differ by remote. The Settings button and native Settings screen remain available through the device launcher; `KEYCODE_MENU` and `KEYCODE_SETTINGS` are supported when sent by the remote.
- Invalid SSL certificates, DNS failures, timeout, and main-frame network errors show retry, Settings, and exit actions. Subresource errors do not replace the page.
- Android 11/API 30 is the primary target. API 23+ is supported by source configuration; no physical TV Box validation is available in this build environment.
