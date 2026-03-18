# Blood Donation System - Admin Dashboard

This is the administration dashboard for the Blood Donation System, built with React, Electron, and Firebase.

## Setup and Installation

### Prerequisites
- Node.js (Latest LTS version recommended)
- npm (Node Package Manager)

### Installation
1. Clone the repository or navigate to the project directory:
   ```bash
   cd d:\Blood-Donation-System\website
   ```
2. Install dependencies:
   ```bash
   npm install
   ```

### Important: Electron Setup
> [!IMPORTANT]
> To run the desktop application, you must extract the `electron.zip` file at this location only:
> **Path:** `d:\Blood-Donation-System\website`
>
> Ensure it is extracted directly into the `website` folder to maintain the correct internal path structure.

## Available Scripts

In the project directory, you can run:

### `npm run dev`
Runs the app in development mode using Vite and Electron concurrently.
The app will automatically reload if you change any source files.

### `npm run build`
Builds the app for production to the `dist` folder.
It correctly bundles React in production mode and optimizes the build for the best performance.

### `npm run electron`
Starts the Electron application specifically.

### `npm run lint`
Runs ESLint to check for code quality and potential issues.

## Project Structure
- `electron/`: Contains the main process for Electron.
- `src/`: Contains the React source code (components, pages, styles).
- `public/`: Static assets.
- `dist/`: Production build output.
