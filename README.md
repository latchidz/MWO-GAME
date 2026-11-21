# NFS Most Wanted — fullscreen Android game launcher

This ARM64 Android app embeds Mozilla GeckoView (Firefox engine) directly inside the Activity. It does not launch Chrome or Android WebView. The site sends COOP/COEP headers and its own error page says it cannot run in blocked embedded contexts; GeckoView provides a full browser engine in an app-owned, fullscreen surface.

## Launch behavior
- Opens `https://web.nfsmwo.com/` directly, with Android Activity locked to landscape and immersive screen flags.
- A small built-in WebExtension looks for the visible, exact `Single player` button and presses it once. If the page structure changes or synthetic activation is rejected, the website remains available so the user can tap its own button.
- The game is cloud/web hosted; no copyrighted game data is bundled in this APK. The first game start can fetch about 2 GB and requires internet/Wi-Fi.
- Game files, browser cache, and local saves remain in GeckoView's app-private browser profile. Android may clear cache under storage pressure; this is not an offline game guarantee.

## Controller support
The site says game controllers work in its menus and while driving. GeckoView/Firefox Gamepad API receives controllers that Android recognizes, including standard Bluetooth HID and USB/OTG controllers. Pair a Bluetooth controller in Android settings or connect a USB controller before launching. Compatibility still depends on the controller exposing a standard Android gamepad profile; no app can promise every proprietary model.

## Requirements and build
- Android 8.0 or newer (API 26+); GeckoView 150 ARM64 artifact.
- Codemagic only (no GitHub Actions).
- The APK is signed with the same key used by previous MWO versions so it can update the existing app.
