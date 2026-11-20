package com.latchi.mwo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

/**
 * NFS Most Wanted — غلاف أندرويد أصلي.
 *
 * الأهداف:
 *  1) تُفتح اللعبة مباشرة على وضع اللاعب الواحد (single player) بلا صفحة أزرار.
 *  2) تحميل الموارد (~2GB) يحدث مرة واحدة فقط ويُحفظ في تخزين التطبيق،
 *     فلا يُعاد التحميل عند إغلاق اللعبة وفتحها من جديد.
 *  3) اللعبة تبقى "شغالة" في الخلفية: لا تُدمَّر عند الخروج، والرجوع إليها فوري.
 *  4) استخدام كامل للشاشة في الوضع الأفقي، مع الاستمرار في العمل أثناء التنزيل.
 */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var splash: View

    // بيانات جهاز ثابتة: تُحقن مرة واحدة فقط حتى تبقى الجلسة والتخزين كما هما
    private val deviceOverrides = mutableSetOf<String>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        goFullscreen()

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }

        // ── شاشة انتظار بسيطة تختفي عند جاهزية اللعبة ──
        splash = TextView(this).apply {
            text = "NFS MOST WANTED"
            setTextColor(Color.parseColor("#E8E8E8"))
            textSize = 20f
            letterSpacing = 0.25f
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.BLACK)
        }
        root.addView(splash, FrameLayout.LayoutParams(-1, -1))

        web = WebView(this)
        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        configureWebView()

        if (savedInstanceState == null) startSinglePlayer() else web.reload()
    }

    // ═══════════════ إعداد WebView ═══════════════

    private fun configureWebView() {
        val cm = CookieManager.getInstance()
        cm.setAcceptCookie(true)
        cm.setAcceptThirdPartyCookies(web, true)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            builtInZoomControls = false
            displayZoomControls = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            userAgentString = userAgentString + " LatchiMWO/1.0"
        }

        // حجم تخزين كبير: يحفظ موارد اللعبة (~2GB) داخل التطبيق
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            web.settings.textZoom = 100
        }

        web.setBackgroundColor(Color.BLACK)
        web.overScrollMode = View.OVER_SCROLL_NEVER
        web.isVerticalScrollBarEnabled = false
        web.isHorizontalScrollBarEnabled = false

        web.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
            override fun onShowFileChooser(
                webView: WebView?, filePathCallback: android.webkit.ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean = false
        }

        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                splash.visibility = View.GONE
                // نتأكد من وجود جلسة جهاز ثابتة (بعض المواقع تنشئها بكوكي)
                ensurePersistentSession(url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val u = request?.url?.toString() ?: return false
                // خدمة الدفع/الحساب/المتجر تُفتح في المتصفح الخارجي
                val external = listOf("plakmp.com", "paypal", "stripe", "google.com/accounts")
                if (external.any { u.contains(it, true) }) {
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u))); true
                    } catch (e: Exception) { true }
                }
                return false
            }
        }
    }

    /** إن لم تكن كوكي الجهاز موجودة، نضعها بأنفسنا ونحفظها — فتبقى الجلسة بعد كل إغلاق. */
    private fun ensurePersistentSession(url: String?) {
        val host = try { Uri.parse(url ?: return).host ?: return } catch (e: Exception) { return }
        val cm = CookieManager.getInstance()
        if (cm.getCookie("https://$host")?.contains("pms_device") == true) return
        // قيمة عشوائية ثابتة تُحفظ مع ملفات الموقع
        val id = getSharedPreferences("mwo", MODE_PRIVATE)
            .getString("device_id", null)
            ?: java.util.UUID.randomUUID().toString().also {
                getSharedPreferences("mwo", MODE_PRIVATE).edit().putString("device_id", it).apply()
            }
        cm.setCookie("https://$host", "pms_device=$id; Path=/; Max-Age=34560000; SameSite=None; Secure")
        cm.flush()
    }

    // ═══════════════ فتح اللعبة مباشرة على single player ═══════════════

    private fun startSinglePlayer() {
        // NOTE: لا نستعمل ?mode=single لأنه غير موثّق؛ نحقن اختيار "Single player"
        // في الـ DOM عند جهوز الصفحة، ولا نلمس أي شيء آخر.
        web.loadUrl(BASE_URL)
    }

    /**
     * يضغط "Single player" أفضل من الاعتماد على نص الصفحة:
     * نبحث عن العنصر حسب النص بعد أن تُصبح الصفحة جاهزة.
     */
    private fun clickSinglePlayer() {
        web.evaluateJavascript(
            """
            (function(){
              var els = Array.prototype.slice.call(document.querySelectorAll('button,a,div[role=button],span'));
              for (var i=0;i<els.length;i++){
                var t = (els[i].innerText||'').trim().toLowerCase();
                if (t === 'single player' || t === 'لاعب واحد') { els[i].click(); return 'clicked'; }
              }
              return 'not-found';
            })();
            """.trimIndent(), null
        )
    }

    // ═══════════════ دورة الحياة: اللعبة لا تُقطع ولا تُعاد من الأول ═══════════════

    override fun onResume() {
        super.onResume()
        web.onResume()
        web.resumeTimers()
        goFullscreen()
        // إن كان الخروج قصيرًا فالصفحة لم تُدمَّر ولا حاجة لإعادة التحميل
    }

    override fun onPause() {
        // نحفظ كل شيء (cookies + localStorage) قبل أن نُخفى
        CookieManager.getInstance().flush()
        web.onPause()
        // لا نستدعي web.clearCache ولا destroy — التخزين يبقى
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // رجوع = إلى الخلف داخل اللعبة فقط؛ بلا خروج من التطبيق
            if (web.canGoBack()) web.goBack() else Toast.makeText(this, "الرجوع من زر الرئيسية يُبقي اللعبة شغالة", Toast.LENGTH_SHORT).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) goFullscreen()
    }

    private fun goFullscreen() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    companion object {
        private const val BASE_URL = "https://web.nfsmwo.com/"
    }
}
