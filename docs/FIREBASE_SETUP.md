# Firebase Setup Guide — RITIK SC

RITIK SC utilizes Firebase Authentication for secure user accounts, Cloud Firestore for device registries & WebRTC signaling, and Cloud Messaging (FCM) for push notifications.

---

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and name it `ritik-sc-remoteaccess`.
3. Disable Google Analytics (optional) and click **Create Project**.

---

## Step 2: Register Android App

1. Click the **Android** icon in the project overview.
2. In **Android package name**, enter exactly:
   ```
   com.ritiksc.remoteaccess
   ```
3. In **App nickname**, enter `RITIK SC`.
4. Enter your Debug SHA-1 signing certificate (generated via `keytool` or Gradle `signingReport`).
5. Click **Register app**.
6. Download `google-services.json` and save it to `app/google-services.json`.

---

## Step 3: Enable Authentication

1. In the Firebase left navigation, go to **Build → Authentication**.
2. Click **Get Started**.
3. Under **Sign-in method**, enable **Email/Password**.
4. Save the configuration.

---

## Step 4: Configure Cloud Firestore

1. In the Firebase navigation, go to **Build → Firestore Database**.
2. Click **Create database**.
3. Select your preferred database location (e.g., `asia-south1` or `us-central1`).
4. Choose **Start in production mode**.
5. Once created, click the **Rules** tab.
6. Replace the existing contents with the rules from the project file:
   ```
   firestore.rules
   ```
7. Click **Publish**.

---

## Step 5: Firebase Admin SDK for Backend

1. In Firebase Console, go to **Project Settings** (gear icon) → **Service accounts**.
2. Select **Node.js** and click **Generate new private key**.
3. Save the downloaded JSON file as `backend/serviceAccountKey.json`.
4. Update `backend/.env` with:
   ```env
   FIREBASE_SERVICE_ACCOUNT_PATH=./serviceAccountKey.json
   FIREBASE_PROJECT_ID=ritik-sc-remoteaccess
   ```
