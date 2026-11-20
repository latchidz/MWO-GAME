# NFS Most Wanted — غلاف أندرويد أصلي

تطبيق أندرويد يفتح لعبة **Need for Speed: Most Wanted (2005)** من `web.nfsmwo.com`
في Chrome Custom Tab (Chromium كامل) بدل Android WebView، لأن الموقع رفض سياق التشغيل الحالي.

| الاشتراك | كيف تحقق |
|---|---|
| **يفتح اللعبة فورًا** | يفتح `web.nfsmwo.com` في Chrome Custom Tab؛ اختيار single player يتم من واجهة الموقع إذا طلبه |
| **موارد أونلاين** | التخزين في Chrome/المتصفح المشترك؛ يبقى عادةً بين الفتحات، لكن Chrome قد يحذف Cache تحت ضغط المساحة، فلا أضمن عدم إعادة تنزيل كل ملف في كل ظرف |
| **المتصفح الكامل** | لا Android WebView: اللعبة تحتاج سياق متصفح علوي حديث لتشغيل WebAssembly المعزول |
| **الأفقي** | شاشة التطبيق Landscape، واللعبة/Chrome يطبّقان اتجاه العرض المناسب أثناء اللعب |
| **أيقونة خاصة** | رسم سيارة سباق ليلية مخصص مع علامة MW، ضمن موارد launcher وadaptive icon |

## البناء
Codemagic فقط (⛔ GitHub Actions): workflow `android-release-build`
- توقيع: `latchi.jks` (نفس مفتاح LATCHI ⇒ تحديث نظيف)
- المخرجات: `dist/MWO_GAME.apk`

## ملاحظة صريحة
اللعبة مملوكة لـ EA/Plak، وهذا التطبيق **غلاف عرض** لا يُوزَّع بموارده؛
كل الموارد تُحمَّل مباشرة من `web.nfsmwo.com` أثناء التشغيل.
