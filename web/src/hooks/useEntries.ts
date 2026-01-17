import { useEffect, useState } from 'react';
import { collection, query, orderBy, onSnapshot } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import type { FirestoreDailyEntry } from '../types';

export function useEntries() {
    const { currentUser } = useAuth();
    const [entries, setEntries] = useState<FirestoreDailyEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        if (!currentUser) {
            setEntries([]);
            setLoading(false);
            return;
        }

        const entriesRef = collection(db, 'users', currentUser.uid, 'entries');
        const q = query(entriesRef, orderBy('date', 'desc'));

        const unsubscribe = onSnapshot(q,
            (snapshot) => {
                const data = snapshot.docs.map(doc => ({
                    ...doc.data(),
                    id: doc.id
                } as FirestoreDailyEntry));
                setEntries(data);
                setLoading(false);
            },
            (err) => {
                console.error("Error fetching entries:", err);
                setError(err);
                setLoading(false);
            }
        );

        return () => unsubscribe();
    }, [currentUser]);

    return { entries, loading, error };
}

export function useRecentEntries(limitCount = 5) {
    // Simplified version, usually you'd use limit() in query but handling it clientside for now is fine for small datasets
    const { entries, loading, error } = useEntries();
    return { entries: entries.slice(0, limitCount), loading, error };
}
