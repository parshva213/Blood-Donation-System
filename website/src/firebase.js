import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

// Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyD_mV6j_2GCD9asK7bX7Ur5lUu5Wy1oduc",
  authDomain: "bd-system-parshva.firebaseapp.com",
  databaseURL: "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "bd-system-parshva",
  storageBucket: "bd-system-parshva.firebasestorage.app",
  messagingSenderId: "775623513742",
  appId: "1:775623513742:web:6ac77cef559279ff02dbdf",
  measurementId: "G-664YQKE61W"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const rtdb = getDatabase(app);
const auth = getAuth(app);

export { app, db, rtdb, auth };
export default app;