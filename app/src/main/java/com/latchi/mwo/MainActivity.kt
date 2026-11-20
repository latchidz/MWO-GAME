package com.latchi.mwo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.content.pm.PackageManager
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Lightweight native launcher for the online NFS Most Wanted browser game.
 * The game itself is intentionally opened in a real Chrome Custom Tab (or
 * the user's default browser), not Android WebView. The site needs a modern,
 * top-level browser context for its cross-origin-isolated WebAssembly runtime.
 * Cache and game files belong to Chrome/browser storage and remain there across
 * launches; this app does not claim to download or own the game assets.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        makeLauncherScreen()

        // Open the game immediately on fresh launch, then leave a clear resume button behind.
        if (savedInstanceState == null) openGame()
    }

    private fun makeLauncherScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(20), dp(28), dp(20))
            setBackgroundColor(Color.rgb(8, 10, 13))
        }

        val logo = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Need for Speed Most Wanted"
        }
        root.addView(logo, LinearLayout.LayoutParams(dp(128), dp(128)).apply {
            bottomMargin = dp(14)
        })

        val title = TextView(this).apply {
            text = "NEED FOR SPEED\nMOST WANTED"
            textSize = 21f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        root.addView(title, LinearLayout.LayoutParams(-2, -2))

        val subtitle = TextView(this).apply {
            text = "لعبة أونلاين — تفتح في متصفح آمن متوافق مع اللعبة"
            textSize = 14f
            setTextColor(Color.rgb(185, 190, 197))
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(18))
        }
        root.addView(subtitle, LinearLayout.LayoutParams(-2, -2))

        val button = Button(this).apply {
            text = "افتح اللعبة / تابع اللعب"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(22, 102, 195)))
            setOnClickListener { openGame() }
        }
        root.addView(button, LinearLayout.LayoutParams(dp(300), dp(54)))

        val note = TextView(this).apply {
            text = "تُحمّل موارد اللعبة أونلاين وتُخزَّن في بيانات المتصفح.\nلا تمسح بيانات Chrome إذا أردت الاحتفاظ بالتخزين المحلي."
            textSize = 12f
            setTextColor(Color.rgb(145, 150, 157))
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        }
        root.addView(note, LinearLayout.LayoutParams(-2, -2))

        setContentView(root)
    }

    private fun openGame() {
        try {
            val builder = CustomTabsIntent.Builder()
                .setToolbarColor(Color.rgb(8, 10, 13))
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
            val customTab = builder.build()

            // Prefer Chrome's full Chromium browser engine; if absent, let Android
            // select the user's installed full browser through the standard view intent.
            if (isPackageInstalled(CHROME_PACKAGE)) {
                customTab.intent.setPackage(CHROME_PACKAGE)
            }
            customTab.launchUrl(this, Uri.parse(GAME_URL))
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GAME_URL)))
            } catch (_: Exception) {
                Toast.makeText(this, "ثبّت Google Chrome أو متصفحًا حديثًا ثم أعد المحاولة", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isPackageInstalled(packageName: String): Boolean = try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (_: Exception) { false }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val GAME_URL = "https://web.nfsmwo.com/"
        private const val CHROME_PACKAGE = "com.android.chrome"
    }
}
