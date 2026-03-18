import React, { createContext, useContext, useState, useEffect } from 'react';
import { onAuthStateChanged, signInWithEmailAndPassword, signOut } from 'firebase/auth';
import { ref, get } from 'firebase/database';
import { auth, rtdb } from '../firebase';

const AuthContext = createContext(null);

// Flag to prevent onAuthStateChanged from interfering during login
let loginInProgress = false;

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}

export function AuthProvider({ children }) {
    const [currentUser, setCurrentUser] = useState(null);
    const [userRole, setUserRole] = useState(null);
    const [loading, setLoading] = useState(true);

    // Fetch user role by querying RTDB users by email
    async function fetchUserRole(email) {
        try {
            const usersRef = ref(rtdb, 'Users');
            const snapshot = await get(usersRef);

            if (snapshot.exists()) {
                const allUsers = snapshot.val();
                for (const [key, userData] of Object.entries(allUsers)) {
                    if (userData.email && userData.email.toLowerCase() === email.toLowerCase()) {
                        return { role: userData.role || null, data: userData };
                    }
                }
                return { role: null, error: `No user with email "${email}" found.` };
            }

            return { role: null, error: 'The /Users path is empty in RTDB.' };
        } catch (error) {
            return { role: null, error: `RTDB read failed: ${error.message}` };
        }
    }

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, async (user) => {
            // Don't interfere during active login
            if (loginInProgress) return;

            if (user) {
                const result = await fetchUserRole(user.email);
                if (result.role === 'admin') {
                    setCurrentUser(user);
                    setUserRole(result.role);
                } else {
                    // Not admin on session restore — sign out quietly
                    await signOut(auth);
                    setCurrentUser(null);
                    setUserRole(null);
                }
            } else {
                setCurrentUser(null);
                setUserRole(null);
            }
            setLoading(false);
        });
        return unsubscribe;
    }, []);

    async function login(email, password) {
        loginInProgress = true;
        try {
            const userCredential = await signInWithEmailAndPassword(auth, email, password);
            const result = await fetchUserRole(userCredential.user.email);

            if (result.role !== 'admin') {
                await signOut(auth);
                loginInProgress = false;
                const detail = result.error || `Your role is "${result.role}", admin access required.`;
                throw { code: 'auth/not-admin', message: detail };
            }

            setCurrentUser(userCredential.user);
            setUserRole(result.role);
            loginInProgress = false;
            setLoading(false);
            return userCredential;
        } catch (error) {
            loginInProgress = false;
            throw error;
        }
    }

    async function logout() {
        setUserRole(null);
        setCurrentUser(null);
        return signOut(auth);
    }

    const value = {
        currentUser,
        userRole,
        loading,
        login,
        logout,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}
