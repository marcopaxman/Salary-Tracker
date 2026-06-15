import { useCallback, useEffect, useMemo, useState } from 'react';
import { collection, query, orderBy, onSnapshot, doc, setDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import type { FirestoreDailyEntry } from '../types';

export function isEntryActive(entry: FirestoreDailyEntry): boolean {
    return entry.active !== false;
}

export function useEntries() {
    const { currentUser } = useAuth();
    const [allEntries, setAllEntries] = useState<FirestoreDailyEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        if (!currentUser) {
            setAllEntries([]);
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
                setAllEntries(data);
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

    const entries = useMemo(
        () => allEntries.filter(isEntryActive),
        [allEntries]
    );

    const inactiveEntries = useMemo(
        () => allEntries.filter(entry => !isEntryActive(entry)),
        [allEntries]
    );

    const deactivateEntry = useCallback(async (entryId: string) => {
        if (!currentUser) return;
        await setDoc(
            doc(db, 'users', currentUser.uid, 'entries', entryId),
            { active: false, updatedAt: Date.now() },
            { merge: true }
        );
    }, [currentUser]);

    const reactivateEntry = useCallback(async (entryId: string) => {
        if (!currentUser) return;
        await setDoc(
            doc(db, 'users', currentUser.uid, 'entries', entryId),
            { active: true, updatedAt: Date.now() },
            { merge: true }
        );
    }, [currentUser]);

    const deleteEntry = useCallback(async (entryId: string) => {
        if (!currentUser) return;
        await deleteDoc(doc(db, 'users', currentUser.uid, 'entries', entryId));
    }, [currentUser]);

    const updateEntryPayrollMonth = useCallback(async (entryId: string, payrollMonth: string | null) => {
        if (!currentUser) return;
        if (payrollMonth) {
            await setDoc(
                doc(db, 'users', currentUser.uid, 'entries', entryId),
                { payrollMonth, updatedAt: Date.now() },
                { merge: true }
            );
        } else {
            await setDoc(
                doc(db, 'users', currentUser.uid, 'entries', entryId),
                { payrollMonth: null, updatedAt: Date.now() },
                { merge: true }
            );
        }
    }, [currentUser]);

    return { entries, inactiveEntries, loading, error, deactivateEntry, reactivateEntry, deleteEntry, updateEntryPayrollMonth };
}

export function useRecentEntries(limitCount = 5) {
    const { entries, loading, error } = useEntries();
    return { entries: entries.slice(0, limitCount), loading, error };
}
