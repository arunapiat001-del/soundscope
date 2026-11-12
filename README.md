# SoundScope

Aplikasi pencarian metadata MusicBrainz, analisis sinyal audio lokal dan review manual. Ini source code, bukan APK siap pasang. Antarmuka web yang sama dibundel ke Android WebView dan dapat dipasang sebagai PWA.

**Status pengujian pembuat:** tes unit Node untuk rumus rating, status backend, CORS dan validasi ID telah dijalankan. Deploy Netlify, akses layanan eksternal, kompilasi Android/GitHub Actions, pemasangan APK, pemilih file dan interaksi PWA di perangkat nyata belum diuji: lingkungan pembuatan tidak memiliki akses internet atau Android SDK. Tidak ada klaim APK sudah berhasil dibuat. Lakukan pengujian di bawah dan periksa log bila ada kegagalan.

## Fitur dan batas yang nyata
- Lagu dan album dicari lewat backend Netlify ke MusicBrainz. Ketik `judul` atau `judul - artis`, pilih kandidat dan lihat sumber; sebuah recording dapat memiliki banyak release, hasil menampilkan release pertama yang disediakan MusicBrainz, bukan memastikan edisi yang dimaksud. Tanggal serta cover mengikuti release tersebut. Cover Art Archive bukan jaminan cover terverifikasi secara independen.
- Mode album menampilkan daftar track bila MusicBrainz menyediakannya; klik track untuk review individual. Album dapat direview secara manual juga.
- Link yang didukung hanya `https://musicbrainz.org/recording/ID` dan `https://musicbrainz.org/release/ID`. Spotify/YouTube tidak didukung; tidak ada scraping atau akses audio dari link.
- File lokal maksimum **12 MiB**. Browser/WebView harus memiliki decoder format tersebut. Analisis dibatasi durasi **5 menit** untuk menekan penggunaan memori; file lebih panjang bisa tetap dicoba untuk diputar. Duration dibaca pemutar, sample rate dan jumlah channel berasal dari decoded AudioBuffer dan dapat berbeda dari nilai asli file. Waveform memakai sampel channel pertama. Pada perangkat lemah decoding tetap bisa menghabiskan banyak memori.
- Pengenalan opsional via AudD mengirim seluruh file yang dipilih (maks. **2 MiB**) ke backend lalu AudD hanya setelah konfirmasi. Ini bukan deteksi lokal. Hasil mengikuti respons AudD dan bukan jaminan identitas versi/rekaman. File tidak disimpan secara permanen oleh kode backend, tetapi penyedia infrastruktur atau AudD mungkin memproses/menyimpan sesuai kebijakan mereka. Periksa kebijakan AudD sebelum memakai. Biaya dan kuota tergantung akun, lihat situs provider.
- Review dan mood sepenuhnya diisi pengguna, bukan AI. Isi dasar untuk setiap kategori yang dinilai. Bobot yang tidak dinilai dikeluarkan dari penyebut; keseluruhan = Σ(skor × bobot) / Σ(bobot yang diisi). Bobot yang dinilai di bawah 70% diberi peringatan belum representatif; 70% adalah ambang peringatan UI, bukan standar ilmiah. Skor dibulatkan 1 desimal sebelum label. Untuk instrumental, biarkan vokal/lirik kosong jika tidak relevan.
- Perbandingan memperlihatkan nilai dan dasar per kategori, sumber, serta cakupan bobot; tidak memberikan ranking atau menyatakan skor dengan cakupan berbeda setara. Review dan mood disimpan di localStorage perangkat/browser saat ini, tidak tersinkron. Maksimal 30 riwayat dan 12 item perbandingan.
- Tidak ada BPM, key, loudness, analisis instrumen/vokal/lirik, chord, struktur/timestamp, genre/mood otomatis, pengukuran mixing/mastering, atau review AI. Tidak ada angka palsu.
- Metadata di MusicBrainz adalah data komunitas, tidak selalu lengkap atau diverifikasi independen. Orang yang tercantum dalam relasi ditampilkan dengan perannya saja. Data penulis/komposer/producer belum tentu dimuat karena relasi work terpisah belum diintegrasikan.

## Isi proyek
```
soundscope/
├── .env.example
├── .github/workflows/build-android.yml
├── .gitignore
├── README.md
├── netlify.toml
├── package.json
├── web/
│   ├── index.html
│   ├── config.js
│   ├── manifest.webmanifest
│   ├── service-worker.js
│   ├── assets/icon-192.png, icon-512.png
│   ├── css/style.css
│   └── js/app.js, review.js
├── netlify/functions/api.js
├── tests/api.test.js, review.test.js
└── android/
    ├── settings.gradle, build.gradle, gradle.properties
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            └── java/com/soundscope/app/MainActivity.java
```

**Gradle Wrapper:** ZIP tidak menyertakan wrapper JAR/scripts karena lingkungan ini tidak punya akses internet untuk memperoleh dan memverifikasinya. Workflow mengunduh Gradle 8.9 resmi melalui `gradle/actions/setup-gradle`, membuat `gradlew`, `gradlew.bat` dan `gradle/wrapper/gradle-wrapper.jar` dari Gradle tersebut pada runner, lalu mengambil checksum distribusi Gradle 8.9 dari `services.gradle.org` dan menuliskannya ke `gradle-wrapper.properties` sebelum menjalankan wrapper. Checksum memverifikasi ZIP distribusi, bukan merupakan audit independen terhadap hasil build atau JAR. File wrapper yang dibuat hanya ada di runner dan tidak otomatis dikomit ke repo. Untuk menyimpannya di repo, hasilkan dari instalasi Gradle resmi 8.9 pada mesin yang sesuai, periksa melalui [Gradle Wrapper validation](https://github.com/gradle/actions/tree/main/wrapper-validation), lalu commit semua file wrapper; jangan unduh JAR dari sumber acak. Build utama tetap Actions, bukan Gradle lokal di Termux.

## 1. Yang dibutuhkan
- HP Android, koneksi internet, ruang kosong, browser, akun GitHub; Netlify diperlukan untuk pencarian metadata dan deploy PWA. Audio lokal dan review tetap dapat digunakan ketika backend tidak aktif, review lagu/album metadata memerlukan pilihan dari hasil online atau riwayat; file audio yang dipilih dapat direview secara lokal tanpa metadata.
- Instal Termux dari sumber yang direkomendasikan proyek resminya: https://termux.dev/en/ atau https://github.com/termux/termux-app#installation. Hindari tutorial usang dan jangan campur APK/plugin Termux dari sumber penandatangan berbeda.
- Simpan ZIP source yang diunduh di folder Download HP. File audio dipilih lewat Android system picker. Aplikasi hanya meminta izin INTERNET; tidak membutuhkan izin penyimpanan menyeluruh. Android/WebView harus cukup baru (Android 7/API 24 atau lebih baru, dengan Android System WebView terbaru). Tidak ada jaminan tiap codec didukung.

## 2. Siapkan Termux
Buka Termux dan jalankan satu per satu:
```sh
pkg update && pkg upgrade -y
pkg install git unzip -y
termux-setup-storage
```
Perintah pertama memperbarui paket, kedua memasang Git dan unzip, ketiga meminta akses ke penyimpanan bersama. Setujui hanya bila perlu membaca file ZIP di Download. Ini adalah izin untuk Termux, bukan izin SoundScope.

## 3. Ekstrak ZIP
Contoh nama unduhan `SoundScope-source.zip`; jika browser mengganti namanya, lihat isi Download dan gunakan nama yang benar:
```sh
ls ~/storage/downloads
mkdir -p ~/projects
unzip ~/storage/downloads/SoundScope-source.zip -d ~/projects
cd ~/projects/soundscope
ls -la
```
Harus terlihat `web`, `android`, `netlify`, dan `.github`. Jika muncul pertanyaan overwrite, hentikan dan ekstrak ke direktori baru, jangan timpa proyek penting. Kerjakan repo di folder pribadi Termux (`~/projects`), bukan storage bersama, agar Git dan izin file lebih konsisten.

## 4. Buat repository GitHub
1. Login GitHub di browser HP. Pilih **New repository**, beri nama `soundscope`, pilih Private bila ingin, dan **jangan** centang inisialisasi README/.gitignore agar tidak muncul konflik awal. Buat repo dan salin URL HTTPS yang ditampilkan, misalnya `https://github.com/NAMA_KAMU/soundscope.git`.
2. Di Termux, dari `~/projects/soundscope`:
```sh
git init -b main
git config user.name "Nama Kamu"
git config user.email "email-kamu@example.com"
git add .
git status
git commit -m "Add SoundScope source"
git remote add origin https://github.com/NAMA_KAMU/soundscope.git
git push -u origin main
```
Ganti nama, email, dan URL dengan milikmu. Periksa `git status` sebelum commit, dan jangan pernah menyertakan token/API key. Contoh `.env.example` hanya berisi contoh tanpa kunci asli.
3. GitHub tidak menerima password akun untuk operasi Git melalui HTTPS. Jika diminta username/password, gunakan username GitHub dan personal access token (PAT) sebagai **password**. Di GitHub: Settings > Developer settings > Personal access tokens > Fine-grained tokens > Generate new token. Pilih hanya repo ini dan izin repository **Contents: Read and write**; untuk mengunggah/ubah file workflow `.github/workflows/`, berikan juga **Workflows: Read and write** bila diminta oleh GitHub. Tentukan masa berlaku singkat, simpan rahasia dan hapus token bila bocor. Saat mengetik token karakter tidak terlihat: ini normal. Jangan taruh token di URL, source, screenshot, atau chat. Kamu juga dapat menggunakan SSH yang telah dikonfigurasi sendiri.

## 5. Deploy backend dan web Netlify
1. Masuk Netlify, **Add new site / Import an existing project**, hubungkan GitHub, pilih repo `soundscope`. Build settings berasal dari `netlify.toml`: Publish directory `web`; Functions directory `netlify/functions`; tidak perlu build command. Deploy.
2. Di Site configuration > Environment variables, tambahkan:
   - `MUSICBRAINZ_CONTACT`: alamat email kontak valid milikmu, misalnya `you@example.com`. Backend mengirim identitas `SoundScope/1.0 (kontak)` ke MusicBrainz sesuai praktik identifikasi klien. Jangan pakai alamat contoh.
   - `APP_ORIGINS`: origin HTTPS Netlify sebenarnya, misalnya `https://nama-situs.netlify.app` tanpa slash akhir. Jika memakai domain lain, pisahkan origin dengan koma, misalnya `https://nama-situs.netlify.app,https://music.example.com`. Jangan gunakan wildcard. Origin WebView `https://appassets.androidplatform.net` diizinkan otomatis.
   - `AUDD_API_TOKEN`: opsional, dijelaskan di bagian 8. Jangan masukkan token dalam file repo atau config frontend.
3. Setelah mengubah environment variables, lakukan **redeploy** agar Functions mendapat nilai baru. Buka situs dan cari `Nights - Frank Ocean`; hasil bisa berubah sesuai data penyedia. Jika berhasil, pilih satu kandidat, cek link sumber dan cover. Kesalahan harus muncul sebagai pesan, bukan data rekaan.
4. Web di domain Netlify yang sama otomatis memakai backend sendiri melalui `backendOrigin: ''`. Jika web dan backend berbeda origin, buka `web/config.js`, isi `backendOrigin: 'https://nama-backend.netlify.app'`, lalu cantumkan origin web di APP_ORIGINS backend. Commit dan push agar web terdeploy ulang.
5. Untuk APK, **wajib** isi `web/config.js` dengan origin backend HTTPS milikmu sebelum build, misalnya:
```js
window.SOUNDSCOPE_CONFIG = { backendOrigin: 'https://nama-situs.netlify.app' };
```
Jangan tambahkan path atau slash akhir. Ubah menggunakan editor teks Termux `nano` (instal dengan `pkg install nano`), kemudian simpan Ctrl+O, Enter, Ctrl+X. Di beberapa keyboard Termux tombol Ctrl tersedia di bar tombol ekstra. Atau edit file tersebut lewat GitHub browser. Commit dan push perubahan:
```sh
git add web/config.js
git commit -m "Configure backend origin"
git push
```
Perubahan ini membundel URL publik, **bukan secret**. Aset web disalin otomatis dari `web/` ke `android/app/build/generated/webAssets/web/` oleh task `copyWebAssets` sebelum build. Jika backend berubah, ubah config, push dan bangun ulang APK. Web yang terpasang perlu update service worker (lihat troubleshooting).

## 6. Build dan download APK dengan GitHub Actions
1. Push akan otomatis memicu workflow `Build SoundScope debug APK`. Setelah URL backend diatur dan dipush, buka repo GitHub di browser HP > tab **Actions**. Bila Actions belum diaktifkan, izinkan dahulu di pengaturan repo. Bisa juga pilih workflow itu dan tekan **Run workflow**.
2. Buka run paling baru. Jika gagal, buka job `android` dan tahap merah untuk membaca penyebab. SDK Android, Java 17, Gradle 8.9 dan wrapper disiapkan oleh workflow; kamu tidak perlu memasang Android Studio di HP.
3. Jika status hijau, scroll ke **Artifacts** lalu unduh **SoundScope-debug-apk**. GitHub mungkin meminta login. Artifact berisi `app-debug.apk`, berasal dari `android/app/build/outputs/apk/debug/app-debug.apk` dan disimpan 14 hari. Ekstrak ZIP artifact melalui aplikasi file HP atau `unzip` di Termux. Jika tidak muncul atau kedaluwarsa, jalankan workflow lagi.
4. Jalur lokal Termux tidak dijanjikan atau disediakan karena SDK resmi dan binary build bisa tidak cocok dengan Android/ARM.

## 7. Instal APK
Buka `app-debug.apk` dari aplikasi file. Android mungkin meminta izin **Install unknown apps / Instal aplikasi tidak dikenal** untuk aplikasi file/browser yang kamu pakai. Izinkan hanya jika kamu percaya asal APK dan repo, instal, lalu matikan kembali izin tersebut di Settings. APK debug ditandatangani debug key untuk pengujian pribadi, **bukan** otomatis aman untuk distribusi atau siap Play Store. Play Store membutuhkan build release, signing dan persyaratan lain. Jangan menginstal APK yang sumbernya tidak dipercaya.

## 8. Aktifkan identifikasi audio opsional
Buat akun dan dapatkan token dari https://audd.io/ sesuai dokumentasi dan tarif berlaku. Di Netlify, set `AUDD_API_TOKEN` sebagai secret environment variable untuk Functions, lalu redeploy. Jangan menaruhnya di repository, `config.js`, APK, URL atau pesan. Untuk mematikan, hapus variable dan redeploy. Tanpa token, pemutaran/analisis lokal dan pencarian metadata (jika `MUSICBRAINZ_CONTACT` dikonfigurasi) tetap berfungsi. Status muncul pada panel audio; jika pengguna menekan tombol, konfirmasi wajib muncul sebelum audio dikirim. Gagal/cocok tidak ditemukan ditampilkan jelas. Tidak ada jaminan kuota gratis atau deteksi versi spesifik.

## 9. PWA dan offline
Di Chrome Android buka situs Netlify HTTPS, buka menu tiga titik > **Install app** atau **Add to Home screen** (nama menu bervariasi). Instalasi membutuhkan browser yang mendukung dan kondisi instalabilitas yang berlaku. Saat online pertama kali, service worker menyimpan shell aplikasi lokal saja: HTML, CSS, JS, manifest, ikon. Audio pengguna, respons API, hasil pencarian dan cover tidak di-cache oleh service worker. File audio yang dipilih diberi ID SHA-256 lokal untuk mengaitkan review; riwayat menyimpan nama dan identitasnya, bukan isi file. Pilih ulang file untuk memutarnya lagi. localStorage menyimpan riwayat/detail dan review yang dipilih. Offline: tampilan, file yang dipilih dan data riwayat lokal tetap dapat digunakan; pencarian/identifikasi dan cover online tidak tersedia. PWA dan APK memiliki penyimpanan terpisah.

## 10. Uji manual
1. Cari judul saja dan judul - artis. Pastikan muncul kandidat nyata atau error informatif. Coba judul ambigu, pilih kandidat, periksa sumber. Cari album lalu pilih track.
2. Tempel URL MusicBrainz yang valid, lalu coba URL Spotify/YouTube: harus ditolak secara jelas. Cover yang gagal dimuat harus berubah menjadi placeholder.
3. Pilih audio yang didukung dan kurang dari 12 MiB/5 menit. Cek nama, ukuran, durasi, pemutaran, sample rate/channel setelah decoding dan waveform. Coba format tidak didukung, file melebihi 12 MiB, dan audio lebih dari 5 menit. Hasil harus sesuai batas di atas, tidak ada BPM/key palsu.
4. Isi review pengguna: atmosfer 8/10 (15%), vokal 4/10 (10%), kategori lain kosong, dan isi dasar untuk keduanya. Nilai 6,4/10, cakupan 25%, peringatan kurang representatif. Tanpa nilai, hasil N/A. Coba nilai 11: harus ditolak. Simpan, buka kembali, lalu hapus riwayat dan review.
5. Tambahkan dua item dengan review ke perbandingan. Pastikan nilai/dasar kategori dan cakupan tampak, tanpa ranking otomatis. Cari album dan simpan review album serta track secara terpisah.
6. Matikan koneksi. Pastikan status offline; fitur online tidak berpura-pura berhasil. Coba data lokal dan audio. Aktifkan kembali koneksi.
7. Tanpa token AudD, tombol menyatakan belum aktif. Bila dikonfigurasi, tolak konfirmasi dan pastikan tidak ada pengiriman; kemudian uji dengan persetujuan dan file <=2 MiB. Jangan bagikan audio tanpa hak.
8. Di Android, pilih file melalui system picker, coba navigasi kembali, putar audio. Perbarui WebView jika decode atau picker gagal. Di browser, periksa opsi instalasi PWA.
9. Untuk tes otomatis di lingkungan yang memiliki Node 20+: `node --test tests/*.test.js`. Tes unit tidak membuktikan layanan live, UI, APK, atau keamanan produksi.

## 11. Troubleshooting
- **Git push ditolak**: pastikan URL repo dan akses benar, token berizin Contents dan Workflows bila perlu; repo sebaiknya baru/kosong. Jika repo memiliki commit lain, tarik perubahan dan selesaikan konflik dengan hati-hati, jangan force-push tanpa memahami dampaknya.
- **Autentikasi gagal**: gunakan PAT bukan password akun. Pastikan belum kedaluwarsa dan izin ke repo benar. Jangan tempel token ke URL.
- **Actions gagal/tidak ada artifact**: periksa job dan log; pastikan Actions diaktifkan, runner dapat mengunduh dependensi, kuota GitHub tersedia dan workflow terbaru berjalan sukses. Artifact hanya diunggah jika `assembleDebug` berhasil; retensi 14 hari.
- **APK gagal instal**: periksa Android >=7, ruang penyimpanan, file unduhan lengkap, izin sumber instalasi, dan konflik versi/tanda tangan jika ada aplikasi dengan applicationId sama. Menghapus aplikasi lama menghapus data lokalnya; cadangkan hal penting terlebih dahulu.
- **Picker/format gagal**: update Android System WebView, gunakan system picker dan file MP3/WAV yang dikenal; dukungan FLAC/M4A/OGG dan decoder berbeda antar perangkat. File rusak, terlalu besar atau durasi terlalu panjang juga dapat gagal dianalisis.
- **Pencarian/CORS**: periksa koneksi, deploy aktif, `MUSICBRAINZ_CONTACT`, `APP_ORIGINS` yang tepat (scheme + host + port jika ada), dan `web/config.js`; lihat Netlify Functions logs. WebView origin sudah masuk allowlist otomatis. Kode CORS bukan sistem autentikasi; pihak non-browser dapat memalsukan origin.
- **AudD belum aktif atau error**: pastikan token sah, environment sudah redeploy, akun/kuota tersedia, berkas <=2 MiB dan provider menerima format. Cek Functions logs tanpa mempublikasikan token atau file pribadi.
- **PWA tampak belum diperbarui**: tutup dan buka aplikasi kembali; bila perlu hapus service worker/data situs dari pengaturan browser dan muat ulang online. Menghapus data situs juga menghapus riwayat/review. Cache versi perlu dinaikkan pada `web/service-worker.js` setiap kali aset shell diperbarui agar versi lama tidak bertahan.

## Privasi, penggunaan dan operasi
Pencarian mengirim teks ke Netlify dan MusicBrainz; cover dimuat dari Cover Art Archive. File audio tetap lokal kecuali pengguna memilih identifikasi AudD dan menyetujui dialog. URL dan permintaan dapat dicatat oleh penyedia hosting/jaringan sesuai kebijakan mereka. Jangan masukkan informasi pribadi ke catatan bila perangkat dipakai bersama. Hapus data lewat tombol di aplikasi atau data situs/aplikasi Android. Pematian aplikasi tidak secara permanen menyimpan file audio.

Backend hanya memanggil endpoint tetap MusicBrainz dan AudD, tanpa fetch URL arbitrer. Ada batas input, timeout, cache dan limiter best-effort **per proses**, bukan global. Lingkungan serverless dapat menjalankan banyak instance, sehingga limiter/cache ini tidak menjamin batas MusicBrainz 1 request/detik secara global dan tidak cukup untuk melindungi token AudD dari penyalahgunaan. Sebelum membuka aplikasi untuk umum, tambahkan rate limiting terdistribusi/authentication dan kontrol kuota yang sesuai, atau batasi pemakaian pribadi. CORS membatasi akses browser, bukan mencegah pemalsuan Origin oleh klien langsung. Netlify/penyedia dapat menerapkan batas request sendiri. Patuhi [MusicBrainz API](https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting), [Cover Art Archive](https://coverartarchive.org/) dan [AudD](https://docs.audd.io/); ketentuan dan biaya dapat berubah.
