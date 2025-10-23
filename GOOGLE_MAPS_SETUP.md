# Google Maps API Key Setup Guide

## Overview
The Google Maps API key is stored securely in `local.properties` which is **never committed to the repository**. This keeps your API key private and safe.

## Setup Steps

### 1. Get Your Google Maps API Key

1. Go to https://console.developers.google.com
2. Create a new project or select an existing one
3. Enable "Maps SDK for Android" in APIs & Services → Library
4. Go to Credentials → Create API Key
5. Restrict the key to Android apps with:
   - **Package name**: `com.sameerasw.nextbus`
   - **SHA-1 fingerprint**: `26:B4:0A:FC:D8:4B:43:93:F9:F9:79:47:06:22:AD:00:73:58:15:F3`

### 2. Add API Key to local.properties

Open `/local.properties` and add your API key:

```properties
GOOGLE_MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE
```

Replace `YOUR_ACTUAL_API_KEY_HERE` with your actual API key from Google Cloud Console.

### 3. Build and Run

```bash
./gradlew clean build
```

The build system will automatically:
- Read the API key from `local.properties`
- Inject it into `AndroidManifest.xml` at build time
- Build the app with the API key embedded

## Security

✅ **Safe Approach:**
- `local.properties` is in `.gitignore` - never committed to git
- API key is injected at build time, not hardcoded in source
- Each developer can have their own API key
- CI/CD systems can use environment variables

❌ **Never Do:**
- Don't commit `local.properties` with your API key
- Don't hardcode API keys in source files
- Don't share API keys in messages or emails

## For New Team Members

When a new developer clones the repo:
1. They need to generate their own API key from Google Cloud Console
2. Add it to their local `local.properties` file
3. Run `./gradlew clean build`
4. Done! The app will build with their API key

## Troubleshooting

If you see "Authorization failure" error:
- Check that the API key is correctly added to `local.properties`
- Verify "Maps SDK for Android" is enabled in Google Cloud Console
- Confirm the SHA-1 fingerprint matches your signing key
- Rebuild with `./gradlew clean build`

## How It Works

The `build.gradle.kts` file reads `local.properties` and passes the API key to `AndroidManifest.xml` using a placeholder variable `${GOOGLE_MAPS_API_KEY}`. This way:
- The manifest file doesn't contain the actual key (safe to commit)
- The key is only injected at build time
- Different build machines can have different keys

