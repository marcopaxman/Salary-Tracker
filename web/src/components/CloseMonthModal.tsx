import { useState, useEffect } from 'react';
import { format, parseISO } from 'date-fns';
import { X, CalendarCheck } from 'lucide-react';
import type { FirestoreDailyEntry } from '../types';
import { formatCurrency } from '../hooks/useSettings';
import { getNextPayrollMonth } from '../utils/payrollMonth';

interface CloseMonthModalProps {
  open: boolean;
  month: string;
  candidates: FirestoreDailyEntry[];
  currency: string;
  loading: boolean;
  onConfirm: (carryOverIds: string[]) => void;
  onCancel: () => void;
}

export default function CloseMonthModal({
  open,
  month,
  candidates,
  currency,
  loading,
  onConfirm,
  onCancel,
}: CloseMonthModalProps) {
  const [selected, setSelected] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (open) setSelected(new Set());
  }, [open, month]);

  if (!open) return null;

  const monthLabel = format(parseISO(`${month}-01`), 'MMMM yyyy');
  const nextMonth = getNextPayrollMonth(month);
  const nextMonthLabel = format(parseISO(`${nextMonth}-01`), 'MMMM yyyy');

  function toggle(id: string) {
    setSelected(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50" onClick={onCancel}>
      <div
        className="bg-white rounded-2xl shadow-xl max-w-lg w-full p-6 max-h-[90vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 text-blue-600 rounded-xl">
              <CalendarCheck size={22} />
            </div>
            <div>
              <h3 className="text-xl font-bold text-slate-900">Close {monthLabel}</h3>
              <p className="text-sm text-slate-500 mt-0.5">Start your {nextMonthLabel} payroll period</p>
            </div>
          </div>
          <button onClick={onCancel} disabled={loading} className="p-2 hover:bg-slate-100 rounded-lg text-slate-400">
            <X size={20} />
          </button>
        </div>

        <p className="text-sm text-slate-600 mb-4 leading-relaxed">
          Select any late-month shifts (e.g. the 30th or 31st) that should count toward{' '}
          <strong>{nextMonthLabel}</strong> instead of {monthLabel}. Unselected entries stay in {monthLabel}.
        </p>

        {candidates.length === 0 ? (
          <p className="text-sm text-slate-400 italic py-4 text-center bg-slate-50 rounded-xl">
            No late-month entries found to carry over.
          </p>
        ) : (
          <div className="space-y-2 mb-6 max-h-60 overflow-y-auto">
            {candidates.map(entry => {
              const tips = (entry.tipsCash || 0) + (entry.tipsCard || 0);
              const isChecked = selected.has(entry.id);
              return (
                <label
                  key={entry.id}
                  className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-colors ${
                    isChecked ? 'border-blue-300 bg-blue-50' : 'border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={isChecked}
                    onChange={() => toggle(entry.id)}
                    className="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-slate-900 text-sm">
                      {format(parseISO(entry.date), 'MMM d, yyyy')}
                    </p>
                    <p className="text-xs text-slate-500">
                      Tips: {formatCurrency(tips, currency)} · Turnover: {formatCurrency(entry.turnover, currency)}
                    </p>
                  </div>
                </label>
              );
            })}
          </div>
        )}

        <div className="flex gap-3 justify-end">
          <button
            onClick={onCancel}
            disabled={loading}
            className="px-5 py-2.5 text-slate-600 font-medium hover:bg-slate-100 rounded-xl transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(Array.from(selected))}
            disabled={loading}
            className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl transition-colors disabled:opacity-50"
          >
            {loading ? 'Closing...' : `Close ${monthLabel}`}
          </button>
        </div>
      </div>
    </div>
  );
}
