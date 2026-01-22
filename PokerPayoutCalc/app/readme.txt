google-services.json file that you get from firebase server should replace existing google-services.json file!



insturctions are in readme file but here is reminder what to do to setup firebase studio and get google-services.json file:



Firebase Setup (For Personal Use / Hosting Your Own Game):

To host your own private games and manage your own dataset, you'll need to set up your own Firebase project. This ensures your games are isolated from others using the app.



Create a Google Account: If you don't have one already, create a Google account.

Access Firebase Console: Go to the Firebase Console (https://console.firebase.google.com/).


Create a New Project:

Click "Add project" and follow the on-screen instructions to create a new Firebase project.

You can enable Google Analytics if desired (optional).


Add Android App to Project:

In your new Firebase project, click the Android icon to add an Android app.

Follow the steps, providing your app's package name (e.g., com.yourcompany.pokerpayoutcalculator).

Download google-services.json: Download the google-services.json file when prompted.

Place the file: Copy this google-services.json file into your Android project's app/ directory (e.g., your_project_root/app/google-services.json).


Configure Firestore Database:

In the Firebase Console, navigate to Build > Firestore Database.

Click "Create database" and choose to start in "production mode" (or "test mode" if you're just experimenting, but remember to secure your rules later). Select a server location closest to you.


Enable Anonymous Authentication:

In the Firebase Console, navigate to Build > Authentication.

Go to the "Sign-in method" tab.

Enable the "Anonymous" provider. This allows users to access the app without requiring explicit sign-up, while still leveraging Firebase's security features.


Change rules:
In the Firebase Console, navigate to Build > Firestore Database > Rules tab instead of defaul rule change last line from " allow read, write: if false; " into " allow read, write: if true;"
Entire code in rules should look like this:
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}



After this you need to compile and build app again to get aplication that is connected to your new firebase studio