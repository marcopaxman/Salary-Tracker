import type { FirestoreDailyEntry } from '../types';
import { format, parseISO, addMonths } from 'date-fns';

/** The payroll month an entry counts toward (defaults to calendar month of date). */
export function getEntryPayrollMonth(entry: FirestoreDailyEntry): string {
  return entry.payrollMonth ?? entry.date.substring(0, 7);
}

export function filterEntriesByPayrollMonth(
  entries: FirestoreDailyEntry[],
  payrollMonth: string
): FirestoreDailyEntry[] {
  return entries.filter(e => getEntryPayrollMonth(e) === payrollMonth);
}

/** Entries dated in `month` on late days (28+) that may roll into the next payroll period. */
export function getCarryOverCandidates(
  entries: FirestoreDailyEntry[],
  month: string
): FirestoreDailyEntry[] {
  return entries.filter(entry => {
    const entryDate = parseISO(entry.date);
    const calendarMonth = format(entryDate, 'yyyy-MM');
    if (calendarMonth !== month) return false;
    return entryDate.getDate() >= 28;
  });
}

export function getNextPayrollMonth(month: string): string {
  return format(addMonths(parseISO(`${month}-01`), 1), 'yyyy-MM');
}

export function isMonthClosed(closedMonths: string[] | undefined, month: string): boolean {
  return closedMonths?.includes(month) ?? false;
}

/** Entries from `month` that were carried into the next payroll period. */
export function getCarriedOverEntries(
  entries: FirestoreDailyEntry[],
  month: string
): FirestoreDailyEntry[] {
  const nextMonth = getNextPayrollMonth(month);
  return entries.filter(
    entry => entry.date.substring(0, 7) === month && entry.payrollMonth === nextMonth
  );
}
