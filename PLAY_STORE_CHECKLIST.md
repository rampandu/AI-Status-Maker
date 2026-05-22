# Play Store Release Checklist — Status Maker

## STEP 1 — Firebase & AdMob (15 min)

### 1a. Create Firebase project
1. Go to https://console.firebase.google.com → "Add project"
2. Name: `StatusMaker`, disable Google Analytics (not needed)
3. Add Android app → package: `com.statusmaker.videoapp`
4. Download `google-services.json` → replace `app/google-services.json`

### 1b. Set up AdMob
1. Go to https://admob.google.com → "Add app" → Android
2. App name: `Status Maker - Video & Reels`
3. Copy your **App ID** (format: `ca-app-pub-XXXXXXXX~XXXXXXXXXX`)
4. Create 3 ad units:
   - Banner ad → copy unit ID
   - Interstitial ad → copy unit ID
   - Rewarded ad → copy unit ID
5. Replace in `app/build.gradle`:
   ```
   manifestPlaceholders = [admobAppId: "ca-app-pub-REAL_ID~REAL_ID"]
   ```
6. Replace test IDs in `AdManager.kt`:
   ```kotlin
   const val BANNER_AD_UNIT   = "ca-app-pub-REAL_BANNER_ID"
   const val INTERSTITIAL_ID  = "ca-app-pub-REAL_INTERSTITIAL_ID"
   const val REWARDED_AD_UNIT = "ca-app-pub-REAL_REWARDED_ID"
   ```

---

## STEP 2 — Keystore & Signing (10 min)

1. Open Android Studio → Build → Generate Signed Bundle / APK
2. Choose **Android App Bundle** (AAB) — required by Play Store
3. Create new keystore → save to `app/keystore/release.jks`
4. Fill keystore details in `app/build.gradle` signingConfigs.release
5. **Backup your keystore file** — losing it = cannot update the app forever

---

## STEP 3 — Play Billing Products (10 min)

1. Go to Play Console → your app → Monetize → Products
2. Create **Subscriptions**:
   - Product ID: `premium_monthly` | Price: ₹99/month
   - Product ID: `premium_annual`  | Price: ₹599/year
3. Create **In-app product**:
   - Product ID: `remove_watermark` | Price: ₹49 (one-time)
4. Activate all three products

---

## STEP 4 — Build the release AAB (5 min)

In Android Studio:
```
Build → Generate Signed Bundle / APK → Android App Bundle → release
```
Output: `app/release/app-release.aab`

Or via command line:
```
./gradlew bundleRelease
```

---

## STEP 5 — Play Console setup (30 min)

### App listing (Store listing tab)
| Field | Value |
|-------|-------|
| App name | Status Maker - Video & Reels |
| Short description | Create stunning WhatsApp Status & Reels videos with Telugu templates |
| Full description | See template below |
| Category | Photography |
| Tags | status maker, video maker, whatsapp status, telugu, reels |

### Full description template:
```
🎬 Status Maker — Create beautiful WhatsApp Status & Instagram Reels videos in seconds!

✨ FEATURES:
• 25+ stunning templates — Birthday, Festival, Devotional, Wedding, Baby, Political, Business
• Telugu & English templates perfect for West Godavari & AP
• Add your photo, name, village name & custom message
• 6 music styles — Filmy, Folk, Classical, Devotional, Instrumental
• Export as 720×1280 HD video (WhatsApp & Reels ready)
• Share directly to WhatsApp, Instagram & more

🎂 BIRTHDAY TEMPLATES
Golden, Floral, Blue Sparkle, Kids Fun

🎉 FESTIVAL TEMPLATES
Sankranti, Ugadi, Dasara, Diwali, Bathukamma, Vinayaka Chavithi

🙏 DEVOTIONAL
Lord Balaji, Goddess Durga, Lord Krishna, Lord Rama

💍 WEDDING | 👶 BABY | 🏪 BUSINESS | 🗳️ POLITICAL

⭐ PREMIUM
• No watermark • Skip ads • All premium templates
₹99/month or ₹599/year
```

### Required screenshots (create with emulator or phone):
- At least 2 phone screenshots (1080×1920 or similar 9:16)
- Recommended: Home screen, Template list, Editor, Preview with video playing
- Tool: https://screenshotmaker.app or just screenshot from a real device

### Feature graphic: 1024×500 px
- Create in Canva: https://canva.com → custom size 1024×500
- Dark background, golden play button, app name

---

## STEP 6 — Privacy Policy (required for AdMob) (10 min)

AdMob requires a privacy policy. Use this free generator:
https://app.privacypolicies.com/wizard/privacy-policy

Key points to include:
- We collect: device identifiers (for ads), usage data
- Third parties: Google AdMob, Google Firebase
- No selling of personal data
- Contact email

Host it on: GitHub Pages (free), or Google Sites, or your domain

---

## STEP 7 — Content rating & declarations

In Play Console → Policy:
- Content rating questionnaire: answer all questions (no violence, no adult content)
- Ads declaration: **Yes** (contains ads)
- In-app purchases: **Yes**
- Target age: 5+ (everyone)
- Data safety form: declare AdMob (advertising ID), no user accounts

---

## STEP 8 — Final upload

1. Play Console → Production → Create new release
2. Upload `app-release.aab`
3. Release notes (what's new): `"Initial release — 25 Telugu video templates for WhatsApp Status & Reels"`
4. Submit for review → typically 1-3 days

---

## QUICK REFERENCE — Files to change before building

| File | What to change |
|------|----------------|
| `app/google-services.json` | Replace with real Firebase download |
| `app/build.gradle` | AdMob App ID, signing passwords |
| `AdManager.kt` | 3 real AdMob unit IDs |
| `StatusMakerApp.kt` | ✅ Already cleaned (no test IDs) |
| `app/keystore/release.jks` | Generate and place here |
