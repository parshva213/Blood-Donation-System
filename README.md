# Blood Donation System

This is a comprehensive Blood Donation System consisting of an Android application for users and an Electron/React-based desktop administration website dashboard. Both components utilize Firebase as their backend.

## Project Structure

This repository contains two main sub-projects:

1. **`android-app/`** - The mobile application built with Kotlin, Jetpack Compose, and XML/Navigation Components.
2. **`website/`** - The admin dashboard built as a desktop application using React, Vite, and Electron.

---

## 1. Android Application (`android-app/`)

A modern Android application built for users of the Blood Donation System. 

### Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose & Android Views (XML)
* **Backend:** Firebase (Auth, Database, Analytics)
* **Architecture/Navigation:** AndroidX Navigation Components

### Setup & Installation
1. Navigate to the `android-app` directory.
2. Open the project in Android Studio.
3. Ensure you have the `google-services.json` file placed in the `app/` directory (required for Firebase configuration).
4. Sync gradle dependencies.
5. Build and run the project on an emulator or physical device.

---

## 2. Admin Dashboard (`website/`)

A desktop-based administration panel to manage the Blood Donation System. 

### Tech Stack
* **Frontend:** React 19, React Router v7, Vite
* **Desktop Wrapper:** Electron
* **Design/UI:** Framer Motion, Lucide React, Recharts
* **Backend:** Firebase

### Setup & Installation
1. Navigate to the `website` directory:
   ```bash
   cd website
   ```
2. Install the necessary dependencies:
   ```bash
   npm install
   ```

> **Important:** To run the desktop application, you must extract the provided `electron.zip` directly into the `website` folder to maintain the correct internal path structure.

### Available Scripts in `website/`

* `npm run dev`: Runs the app in development mode using Vite and Electron concurrently.
* `npm run build`: Builds the React app for production into the `dist` folder.
* `npm run electron`: Starts the Electron application specifically.
* `npm run lint`: Runs ESLint checks.

---

## License
MIT License.
