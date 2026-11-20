# NFS Most Wanted — غلاف أندرويد أصلي

تطبيق أندرويد يفتح لعبة **Need for Speed: Most Wanted (2005)** من `web.nfsmwo.com`
داخل WebView أصلي، مع أهم اشتراطات المستخدم:

| الاشتراك | كيف تحقق |
|---|---|
| **يفتح مباشرة على single player** | لا صفحة وسيطة؛ التطبيق يحمّل اللعبة فورًا ويكمل الجلسة المحفوظة |
| **تحميل الموارد مرة واحدة** | WebView يخزّن ملفات اللعبة (~2GB) في `data/data/com.latchi.mwo` — لا يُعاد التحميل |
| **اللعبة تبقى شغالة** | `onPause` يحفظ الـ cookies/localStorage فقط، ولا يمسح الكاش ولا يدمّر الـ WebView |
| **أفقي** | `sensorLandscape` + fullscreen + `KEEP_SCREEN_ON` |
| **رجوع ذكي** | زر الرجوع لا يُغلق التطبيق |

## البناء
Codemagic فقط (⛔ GitHub Actions): workflow `android-release-build`
- توقيع: `latchi.jks` (نفس مفتاح LATCHI ⇒ تحديث نظيف)
- المخرجات: `dist/MWO_GAME.apk`

## ملاحظة صريحة
اللعبة مملوكة لـ EA/Plak، وهذا التطبيق **غلاف عرض** لا يُوزَّع بموارده؛
كل الموارد تُحمَّل مباشرة من `web.nfsmwo.com` أثناء التشغيل.
