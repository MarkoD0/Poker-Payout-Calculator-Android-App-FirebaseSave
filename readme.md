# 🃏 Poker Payout Calculator
**The Poker Payout Calculator is an intuitive mobile application designed to simplify the tracking and management of buy-ins during your home poker games.**

---

## 📱 App Preview
<video src="https://github.com/user-attachments/assets/9dce4e98-e4a8-42f5-8665-4a7b225f9454" controls title="Poker Payout Calculator Demo" style="max-width: 100%;"></video>

---


## 🌟 Key Features

* **👤 Player Management:** Easily add new players to your game, with an option to designate VIP players for special tracking and recognition.

* **💳 Flexible Buy-ins:** Record buy-ins using two distinct methods:
    * **Cash:** For instances where a player pays with immediate physical cash when they buy in.
    * **Loan:** If a player doesn't have cash on hand at the time of their buy-in, a "loan" can be recorded, to be settled after the game concludes.

* **📈 Real-time Tracking:** Monitor current active players, total buy-ins per player, and the house's cash holdings at a glance.

* **✏️ Error Correction:** Quickly edit and correct any buy-in mistakes for individual players.

* **💰 Effortless Payouts:** At the end of your game, the app precisely calculates payouts, indicating who should receive money and how much. For cashouts, players can choose between two options:
    * **Cash Payouts:** For immediate physical cash distribution.
    * **Loan Payout:** If a player is comfortable receiving their winnings later, particularly from players who entered with loans. The app also clearly shows players with outstanding loans, who they need to pay, and the exact amount.


## 🔒 Data Persistence & Real-time Collaboration

All game data is securely saved on Google's Firebase Cloud Firestore. The application is designed for real-time multi-user collaboration, meaning all users connected to the same Firebase project will see and interact with the same game data instantly. This allows for seamless tracking and management across multiple devices during a single game.


## Firebase Setup (For Personal Use / Hosting Your Own Game)

#### To host your own private games and manage your own dataset, you'll need to set up your own Firebase project. This ensures your games are isolated from others using the app.

    1. Create a Google Account: If you don't have one already, create a Google account.

    2. Access Firebase Console: Go to the Firebase Console (https://console.firebase.google.com/).

    3. Create a New Project:
        - Click "Add project" and follow the on-screen instructions to create a new Firebase project.
        - You can enable Google Analytics if desired (optional).

    4. Add Android App to Project:
        - In your new Firebase project, click the Android icon to add an Android app.
        - Follow the steps, providing your app's package name (e.g., com.yourcompany.pokerpayoutcalculator).

    5. Download google-services.json: Download the google-services.json file when prompted.

    6. Place the file: Copy this google-services.json file into your Android project's app/ directory (e.g., your_project_root/app/google-services.json).

    7. Configure Firestore Database:
        - In the Firebase Console, navigate to Build > Firestore Database.
        - Click "Create database" and choose to start in "production mode" (or "test mode" if you're just experimenting, but remember to secure your rules later). Select a server location closest to you.

    8. Enable Anonymous Authentication:
        - In the Firebase Console, navigate to Build > Authentication.
        - Go to the "Sign-in method" tab.
        - Enable the "Anonymous" provider. This allows users to access the app without requiring explicit sign-up, while still leveraging Firebase's security features.

    9. Change rules:
        - In the Firebase Console, navigate to Build > Firestore Database > Rules tab instead of defaul rule change last line from " allow read, write: if false; " into " allow read, write: if true;"
        - entire code in rules should look like this:
            rules_version = '2';
            service cloud.firestore {
            match /databases/{database}/documents {
                match /{document=**} {
                allow read, write: if true;
                }
            }
            }


#### It's crucial to understand that games on the same Firebase server will share data. For this reason, I highly recommend that you set up your own dedicated Firebase server for your games. This ensures data isolation, security, and optimal performance for each individual user.


## 📥 Installation
You can download the latest installable APK from releases section or [this link](https://github.com/MarkoD0/Poker-Payout-Calculator-Android-App-FirebaseSave/releases/download/v1.0/app-debug.apk). 
#### (Note: This is a Debug Build)
1. Download the `.apk` file.
2. Transfer it to your Android device.
3. Open the file to install (ensure "Install from unknown sources" is enabled).
4. ⚠️ Since this is a debug-signed version, Google Play Protect may show a warning. Select "Install anyway" to proceed.
