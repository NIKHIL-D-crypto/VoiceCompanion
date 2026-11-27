# Voice Companion — Android starter

A first Android foundation for the voice-first companion project.

## Current prototype features

- Kotlin + Jetpack Compose interface
- Text input and conversation bubbles
- Tap-to-talk speech recognition using Android's speech service
- Text-to-speech playback
- GitHub Actions workflow to build a debug APK

## Important: AI is not connected yet

The current reply is a clearly marked placeholder that repeats what was heard. No online AI provider, API key, custom model, persistent memory, or offline language model is included yet.

Speech recognition availability and offline support depend on the Android device and installed speech services. The app requests microphone permission only when Talk is pressed.

## Build an APK using GitHub Actions

1. Create a new GitHub repository.
2. Upload the contents of this project folder to the repository root.
3. Make sure the default branch is named `main`.
4. Open the repository's **Actions** tab.
5. Select **Build Android APK** and run the workflow, or push a commit to `main`.
6. Open the completed workflow run and download the `voice-companion-debug-apk` artifact.

The workflow creates a debug APK. It is for testing, not a signed Play Store release.

## Package details

- Application ID: `com.example.voicecompanion`
- Minimum Android version: Android 8.0 (API 26)
- Target / compile SDK: 35
- Java: 17

## Next development milestones

1. Replace the placeholder reply with a conversation-engine interface.
2. Add online AI through a secure backend (never put provider API keys in the APK).
3. Add local memory with Room.
4. Add offline intent handling.
5. Train and evaluate the project's own small model.
6. Improve speech handling, interruption, and voice settings.
