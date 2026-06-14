import { useState, useEffect } from 'react';
import { collection, query, onSnapshot, orderBy, addDoc, updateDoc, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import type { FirestoreJob } from '../types';

export function useJobs() {
    const { currentUser } = useAuth();
    const [jobs, setJobs] = useState<FirestoreJob[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        if (!currentUser) {
            setJobs([]);
            setLoading(false);
            return;
        }

        const q = query(
            collection(db, 'users', currentUser.uid, 'jobs'),
            orderBy('createdAt', 'desc')
        );

        const unsubscribe = onSnapshot(q,
            (snapshot) => {
                const jobsData = snapshot.docs.map(doc => ({
                    id: doc.id,
                    ...doc.data()
                } as FirestoreJob));
                setJobs(jobsData);
                setLoading(false);
            },
            (err) => {
                console.error(err);
                setError(err);
                setLoading(false);
            }
        );

        return () => unsubscribe();
    }, [currentUser]);

    const addJob = async (job: Omit<FirestoreJob, 'id' | 'createdAt' | 'updatedAt'>) => {
        if (!currentUser) return;
        try {
            await addDoc(collection(db, 'users', currentUser.uid, 'jobs'), {
                ...job,
                createdAt: Date.now(),
                updatedAt: Date.now()
            });
        } catch (err) {
            console.error(err);
            throw err;
        }
    };

    const updateJob = async (id: string, updates: Partial<Omit<FirestoreJob, 'id' | 'createdAt' | 'updatedAt'>>) => {
        if (!currentUser) return;
        try {
            await updateDoc(doc(db, 'users', currentUser.uid, 'jobs', id), {
                ...updates,
                updatedAt: Date.now()
            });
        } catch (err) {
            console.error(err);
            throw err;
        }
    };

    const deleteJob = async (id: string) => {
        if (!currentUser) return;
        try {
            await deleteDoc(doc(db, 'users', currentUser.uid, 'jobs', id));
        } catch (err) {
            console.error(err);
            throw err;
        }
    };

    return { jobs, loading, error, addJob, updateJob, deleteJob };
}
