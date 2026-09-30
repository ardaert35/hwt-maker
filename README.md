# HWT Maker GT6

Telefon üzerinden Huawei Watch GT 6 Pro için HWT kadranı hazırlamak üzere başlangıç Android projesi.

- GT 6 Pro çalışma alanı: 466×466
- Çalışan HWT şablonu açma
- Galeriden görsel seçme ve 466×466 kırpma
- Mevcut HWT paket yapısını koruyarak PNG kaynağını değiştirme
- Yeni `.hwt` olarak kaydetme

Huawei'nin resmi dokümanı 466×466 watch-face çözünürlüğünü ve HWT dışa aktarımını doğruluyor. Bu proje tahmini bir HWT formatı uydurmak yerine çalışan bir şablonun paket yapısını korur.

## Derleme
Android Studio ile klasörü açıp `Build > Build APK(s)` seçilebilir. Android Gradle Plugin 8.6.1, compileSdk 35, minSdk 26 kullanır.
