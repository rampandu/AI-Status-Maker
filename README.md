# Status Maker - Video & Reels

An Android app for creating WhatsApp Status and Instagram Reels videos with Telugu/Indian festival templates.

**Package:** `com.statusmaker.videoapp`  
**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 34  
**Language:** Kotlin

---

## Setup Instructions

### 1. Firebase / AdMob
- Create a Firebase project at https://console.firebase.google.com
- Add an Android app with package `com.statusmaker.videoapp`
- Download `google-services.json` and replace `app/google-services.json`
- In AdMob (https://admob.google.com), create an app and get:
  - App ID → update in `AndroidManifest.xml` (`com.google.android.gms.ads.APPLICATION_ID`)
  - Banner Ad Unit ID → update in `AdManager.kt` (`BANNER_ID`)
  - Interstitial Ad Unit ID → update in `AdManager.kt` (`INTERSTITIAL_ID`)
  - Rewarded Ad Unit ID → update in `AdManager.kt` (`REWARDED_ID`)

### 2. Play Billing (In-App Purchases)
Create these products in Google Play Console → Monetize → Products:
- **Subscriptions:**
  - `premium_monthly` — ₹99/month
  - `premium_annual` — ₹599/year
- **One-time products:**
  - `remove_watermark` — ₹49

### 3. Signing
- Generate a release keystore:
  ```
  keytool -genkey -v -keystore statusmaker-release.jks -alias statusmaker \
    -keyalg RSA -keysize 2048 -validity 10000
  ```
- Add signing config to `app/build.gradle`

### 4. Build
```bash
./gradlew assembleRelease
```

---

## Architecture

```
app/
├── ads/               AdMob integration (banner, interstitial, rewarded)
├── data/
│   ├── db/            Room database (projects)
│   ├── model/         Data models (Template, UserInput, Project)
│   └── repository/    Template catalogue (25+ templates)
├── ui/
│   ├── home/          Home screen with category grid
│   ├── template/      Template list with category filtering
│   ├── editor/        User input form (photo, name, message, music)
│   ├── preview/       Video preview + export with rewarded ad gate
│   └── premium/       Play Billing subscriptions
├── utils/             FileUtils, PreferenceManager (DataStore)
└── video/
    ├── VideoGenerator.kt   MediaCodec H.264 encoder (720×1280 @30fps)
    └── FrameRenderer.kt    Canvas-based per-frame rendering
```

---

## Template Categories (25 templates)

| Category   | Count | Templates |
|------------|-------|-----------|
| Birthday   | 4     | Gold Glam, Rose Bokeh, Blue Sparkle, Kids Fun |
| Festival   | 6     | Sankranti, Ugadi, Dasara, Diwali, Bathukamma, Vinayaka Chavithi |
| Devotional | 4     | Lord Balaji, Goddess Durga, Lord Krishna, Lord Rama |
| Political  | 3     | Blue, Red, Green |
| Baby       | 4     | Baby Boy, Baby Girl, Baby Shower, Naming Ceremony |
| Wedding    | 2     | Classic, Modern |
| Business   | 2     | Offer, Grand Opening |

---

## Monetization

1. **Rewarded Ads** — Required before each export (free users). Skipping = watermark added.
2. **Banner Ads** — Home screen bottom
3. **Interstitial Ads** — On navigation
4. **Premium Subscription** — ₹99/month or ₹599/year (no ads, no watermark, all templates)
5. **One-time IAP** — ₹49 watermark removal only

---

## Key Files

| File | Purpose |
|------|---------|
| `video/VideoGenerator.kt` | MediaCodec MP4 encoder with Flow progress |
| `video/FrameRenderer.kt` | Canvas rendering for all 7 template styles |
| `ads/AdManager.kt` | Singleton ad management |
| `data/repository/TemplateRepository.kt` | All 25 template definitions |
| `ui/premium/PremiumFragment.kt` | Full Play Billing v6 implementation |

---

## Test AdMob IDs (replace before release)
- App ID: `ca-app-pub-3940256099942544~3347511713`
- Banner: `ca-app-pub-3940256099942544/6300978111`
- Interstitial: `ca-app-pub-3940256099942544/1033173712`
- Rewarded: `ca-app-pub-3940256099942544/5224354917`
