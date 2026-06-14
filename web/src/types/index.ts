export interface FirestoreDailyEntry {
    id: string; // "YYYY-MM-DD"
    date: string; // "YYYY-MM-DD"
    turnover: number;
    tipsCash?: number;
    tipsCard?: number;
    notes?: string;
    jobId?: string; // Stringified Long
    hoursWorked?: number;
    active?: boolean;
    createdAt: number;
    updatedAt: number;
}

export interface FirestoreJob {
    id: string; // Stringified Long
    name: string;
    createdAt: number;
    updatedAt: number;
}

export interface FirestoreMonthlyGoal {
    id: string; // "YYYY-MM"
    yearMonth: string; // "YYYY-MM"
    goalTips: number;
    commissionPercent: number; // default 0.01
    createdAt: number;
    updatedAt: number;
}

// Helper types for local app state
export interface DailyEntry extends Omit<FirestoreDailyEntry, 'jobId'> {
    jobId?: string;
}

export interface Job extends FirestoreJob { }
