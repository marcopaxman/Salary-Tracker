import { useEffect, useState } from 'react';
import { collection, query, onSnapshot, doc, setDoc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import type { FirestoreMonthlyGoal } from '../types';
import { format } from 'date-fns';

export function useGoals() {
    const { currentUser } = useAuth();
    const [goals, setGoals] = useState<FirestoreMonthlyGoal[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        if (!currentUser) {
            setGoals([]);
            setLoading(false);
            return;
        }

        const goalsRef = collection(db, 'users', currentUser.uid, 'goals');
        const q = query(goalsRef);

        const unsubscribe = onSnapshot(q,
            (snapshot) => {
                const data = snapshot.docs.map(doc => doc.data() as FirestoreMonthlyGoal);
                setGoals(data);
                setLoading(false);
            },
            (err) => {
                console.error("Error fetching goals:", err);
                setError(err);
                setLoading(false);
            }
        );

        return () => unsubscribe();
    }, [currentUser]);

    const getGoalForMonth = (date: Date) => {
        const yearMonth = format(date, 'yyyy-MM');
        return goals.find(g => g.yearMonth === yearMonth);
    };

    const updateGoal = async (date: Date, goalTips: number, commissionPercent: number = 0.01) => {
        if (!currentUser) return;
        const yearMonth = format(date, 'yyyy-MM');

        try {
            const goalData: FirestoreMonthlyGoal = {
                id: yearMonth,
                yearMonth: yearMonth,
                goalTips,
                commissionPercent,
                createdAt: Date.now(), // In a real app we'd preserve original creation time
                updatedAt: Date.now()
            };

            await setDoc(doc(db, 'users', currentUser.uid, 'goals', yearMonth), goalData, { merge: true });
        } catch (err) {
            console.error("Error updating goal:", err);
            throw err;
        }
    };

    return { goals, loading, error, getGoalForMonth, updateGoal };
}
