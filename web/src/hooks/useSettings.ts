import { useState, useEffect } from 'react';
import { doc, onSnapshot, setDoc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';

export interface UserSettings {
    currency: string;
    notificationsEnabled: boolean;
    hourlyRate: number;
    commissionPercent: number;
    /** Payroll months the user has closed (YYYY-MM). */
    closedMonths?: string[];
}

export const CURRENCIES = [
    { code: 'EUR', symbol: '€', name: 'Euro' },
    { code: 'USD', symbol: '$', name: 'US Dollar' },
    { code: 'ZAR', symbol: 'R', name: 'South African Rand' },
];

export const formatCurrency = (amount: number, currencyCode: string) => {
    const currency = CURRENCIES.find(c => c.code === currencyCode) || CURRENCIES[0];
    const val = typeof amount === 'number' && !isNaN(amount) ? amount : 0;
    return `${currency.symbol}${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const DEFAULT_SETTINGS: UserSettings = {
    currency: 'EUR',
    notificationsEnabled: false,
    hourlyRate: 0,
    commissionPercent: 0.01
};

export function useSettings() {
    const { currentUser } = useAuth();
    const [settings, setSettings] = useState<UserSettings>(DEFAULT_SETTINGS);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        if (!currentUser) {
            setSettings(DEFAULT_SETTINGS);
            setLoading(false);
            return;
        }

        const settingsRef = doc(db, 'users', currentUser.uid, 'settings', 'preferences');

        const unsubscribe = onSnapshot(settingsRef,
            (docSnap) => {
                if (docSnap.exists()) {
                    setSettings({ ...DEFAULT_SETTINGS, ...docSnap.data() } as UserSettings);
                } else {
                    // Initialize if doesn't exist
                    setDoc(settingsRef, DEFAULT_SETTINGS).catch(console.error);
                    setSettings(DEFAULT_SETTINGS);
                }
                setLoading(false);
            },
            (err) => {
                console.error("Error fetching settings:", err);
                setError(err);
                setLoading(false);
            }
        );

        return () => unsubscribe();
    }, [currentUser]);

    const updateSettings = async (newSettings: Partial<UserSettings>) => {
        if (!currentUser) return;
        try {
            const settingsRef = doc(db, 'users', currentUser.uid, 'settings', 'preferences');
            await setDoc(settingsRef, newSettings, { merge: true });
        } catch (err) {
            console.error("Error updating settings:", err);
            throw err;
        }
    };

    return { settings, updateSettings, loading, error };
}
