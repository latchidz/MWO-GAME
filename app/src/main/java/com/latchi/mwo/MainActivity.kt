package com.latchi.mwo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension

/**
 * Full-screen in-app Firefox/Gecko browser for the online NFS:MW game.
 * Unlike Android WebView or a Chrome Custom Tab, this keeps the whole game
 * surface inside the app and uses a browser engine that supports COOP/COEP,
 * crossOriginIsolated WebAssembly, touch, and the browser Gamepad API.
 */
class MainActivity : Activity() {

    private lateinit var gameView: GeckoView
    private var session: GeckoSession? = null
    private var loaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersiveMode()

        gameView = GeckoView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
        }
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(gameView, FrameLayout.LayoutParams(-1, -1))
        })
        gameView.requestFocus()

        val browser = runtime()
        // The built-in content script presses the official Single player button
        // once it becomes visible. If installation fails, we still show the site
        // so that the user can tap Single player manually.
        browser.webExtensionController
            .ensureBuiltIn(AUTOSTART_EXTENSION, AUTOSTART_ID)
            .accept(
                { _: WebExtension? -> openGame(browser) },
                { _: Throwable? -> openGame(browser) }
            )
    }

    private fun runtime(): GeckoRuntime {
        synchronized(MainActivity::class.java) {
            if (sharedRuntime == null) sharedRuntime = GeckoRuntime.create(this)
            return sharedRuntime!!
        }
    }

    private fun openGame(browser: GeckoRuntime) {
        if (loaded || isFinishing || isDestroyed) return
        loaded = true

        val gameSession = GeckoSession()
        gameSession.setContentDelegate(object : GeckoSession.ContentDelegate {
            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                runOnUiThread { if (fullScreen) enterImmersiveMode() else enterImmersiveMode() }
            }
        })
        gameSession.open(browser)
        gameView.setSession(gameSession)
        gameSession.setActive(true)
        session = gameSession
        gameSession.loadUri(GAME_URL)
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
        session?.setActive(true)
    }

    override fun onPause() {
        session?.setActive(false)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    private fun enterImmersiveMode() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onDestroy() {
        // Do not clear Gecko's profile/cache/cookies: the web game owns those
        // browser data and uses them to speed up later starts and keep local saves.
        session?.close()
        session = null
        super.onDestroy()
    }

    companion object {
        private const val GAME_URL = "https://web.nfsmwo.com/"
        private const val AUTOSTART_EXTENSION = "resource://android/assets/mwo-autostart/"
        private const val AUTOSTART_ID = "mwo-single-player@latchi.app"
        @Volatile private var sharedRuntime: GeckoRuntime? = null
    }
}
