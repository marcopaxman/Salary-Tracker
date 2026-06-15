import { useState } from 'react';
import { useEntries } from '../hooks/useEntries';
import { useSettings, formatCurrency } from '../hooks/useSettings';
import { format, parseISO } from 'date-fns';
import { Edit2, Plus, Calendar, Archive, RotateCcw, Trash2, ChevronDown, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { FirestoreDailyEntry } from '../types';
import ConfirmModal from '../components/ConfirmModal';
import { getEntryPayrollMonth } from '../utils/payrollMonth';

type ConfirmAction = 'archive' | 'delete' | null;

function EntryCard({
  entry,
  settings,
  muted = false,
  actions,
}: {
  entry: FirestoreDailyEntry;
  settings: { currency: string };
  muted?: boolean;
  actions: React.ReactNode;
}) {
  const totalTips = (entry.tipsCash || 0) + (entry.tipsCard || 0);
  const payrollMonth = getEntryPayrollMonth(entry);
  const calendarMonth = entry.date.substring(0, 7);
  const carriedOver = payrollMonth !== calendarMonth;

  return (
    <div className={`bg-white rounded-xl border border-slate-200 p-4 space-y-3 ${muted ? 'opacity-70' : ''}`}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-bold text-slate-900">{format(parseISO(entry.date), 'MMM d, yyyy')}</p>
          {carriedOver && (
            <p className="text-xs text-blue-600 font-medium mt-0.5">
              Counted in {format(parseISO(`${payrollMonth}-01`), 'MMM yyyy')}
            </p>
          )}
          {entry.notes && (
            <p className="text-xs text-slate-400 mt-0.5 line-clamp-1">{entry.notes}</p>
          )}
        </div>
        <div className="flex items-center gap-1 shrink-0">
          {actions}
          <Link
            to={`/dashboard/entry/${entry.id}`}
            className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          >
            <Edit2 size={18} />
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 text-sm">
        <div>
          <p className="text-xs text-slate-400 uppercase tracking-wide">Turnover</p>
          <p className="font-semibold text-slate-800">{formatCurrency(entry.turnover, settings.currency)}</p>
        </div>
        <div>
          <p className="text-xs text-slate-400 uppercase tracking-wide">Total Tips</p>
          <p className="font-semibold text-blue-600">{formatCurrency(totalTips, settings.currency)}</p>
        </div>
        <div>
          <p className="text-xs text-slate-400 uppercase tracking-wide">Cash / Card</p>
          <p className="font-medium text-slate-700 text-xs mt-0.5">
            {formatCurrency(entry.tipsCash || 0, settings.currency)} / {formatCurrency(entry.tipsCard || 0, settings.currency)}
          </p>
        </div>
        <div>
          <p className="text-xs text-slate-400 uppercase tracking-wide flex items-center gap-1">
            <Clock size={11} /> Hours
          </p>
          <p className="font-semibold text-slate-800">{entry.hoursWorked ?? '—'}</p>
        </div>
      </div>
    </div>
  );
}

function EntryTableRow({
  entry,
  settings,
  actions,
  muted = false,
}: {
  entry: FirestoreDailyEntry;
  settings: { currency: string };
  actions: React.ReactNode;
  muted?: boolean;
}) {
  return (
    <tr className={`hover:bg-slate-50 transition-colors group ${muted ? 'opacity-60' : ''}`}>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500">
        {format(parseISO(entry.date), 'MMM d, yyyy')}
      </td>
      <td className="px-6 py-4 text-slate-600 font-medium">
        {formatCurrency(entry.turnover, settings.currency)}
      </td>
      <td className="px-6 py-4">
        <div className="flex items-center gap-2 text-sm">
          <span className="px-2 py-1 bg-green-50 text-green-700 rounded-md font-medium text-xs">
            {formatCurrency(entry.tipsCash || 0, settings.currency)} cash
          </span>
          <span className="text-slate-300">|</span>
          <span className="px-2 py-1 bg-blue-50 text-blue-700 rounded-md font-medium text-xs">
            {formatCurrency(entry.tipsCard || 0, settings.currency)} card
          </span>
        </div>
      </td>
      <td className="px-6 py-4 text-slate-600 font-medium">
        {entry.hoursWorked || '-'}
      </td>
      <td className="px-6 py-4 text-right">
        <div className="flex items-center justify-end gap-1">
          {actions}
          <Link
            to={`/dashboard/entry/${entry.id}`}
            className="inline-block p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all"
          >
            <Edit2 size={18} />
          </Link>
        </div>
      </td>
    </tr>
  );
}

export default function EntryList() {
  const { entries, inactiveEntries, loading, deactivateEntry, reactivateEntry, deleteEntry } = useEntries();
  const { settings } = useSettings();

  const [confirmAction, setConfirmAction] = useState<ConfirmAction>(null);
  const [pendingEntryId, setPendingEntryId] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [archivedOpen, setArchivedOpen] = useState(false);

  const pendingEntry = [...entries, ...inactiveEntries].find(e => e.id === pendingEntryId);

  function openConfirm(action: ConfirmAction, entryId: string) {
    setConfirmAction(action);
    setPendingEntryId(entryId);
  }

  function closeConfirm() {
    if (actionLoading) return;
    setConfirmAction(null);
    setPendingEntryId(null);
  }

  async function handleConfirm() {
    if (!pendingEntryId || !confirmAction) return;
    setActionLoading(true);
    try {
      if (confirmAction === 'archive') {
        await deactivateEntry(pendingEntryId);
      } else if (confirmAction === 'delete') {
        await deleteEntry(pendingEntryId);
      }
      closeConfirm();
    } finally {
      setActionLoading(false);
    }
  }

  async function handleReactivate(entryId: string) {
    await reactivateEntry(entryId);
  }

  const confirmConfig = {
    archive: {
      title: 'Archive this entry?',
      message: 'This entry will be moved to your archived list and excluded from dashboard calculations. You can reactivate it later.',
      confirmLabel: 'Archive Entry',
      variant: 'warning' as const,
    },
    delete: {
      title: 'Permanently delete this entry?',
      message: `This will permanently remove the entry for ${pendingEntry ? format(parseISO(pendingEntry.date), 'MMM d, yyyy') : 'this date'}. This action cannot be undone.`,
      confirmLabel: 'Delete Permanently',
      variant: 'danger' as const,
    },
  };

  const activeConfig = confirmAction ? confirmConfig[confirmAction] : null;

  if (loading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="h-8 bg-gray-200 w-1/4 rounded"></div>
        <div className="h-64 bg-gray-200 rounded-xl"></div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-slate-900">Entries</h2>
          <p className="text-slate-500 mt-1">Manage your daily salary records</p>
        </div>
        <Link
          to="/dashboard/entry/new"
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-semibold shadow-lg shadow-blue-600/20 transition-all active:scale-95"
        >
          <Plus size={20} />
          <span>Add Entry</span>
        </Link>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        {entries.length === 0 ? (
          <div className="p-16 text-center flex flex-col items-center justify-center text-slate-400">
            <div className="p-4 bg-slate-50 rounded-full mb-4">
              <Calendar size={32} className="text-slate-400" />
            </div>
            <p className="text-lg font-medium text-slate-600">No entries yet</p>
            <p className="text-sm mt-1">Start by adding your first shift!</p>
          </div>
        ) : (
          <>
            {/* Mobile card layout */}
            <div className="md:hidden p-4 space-y-3">
              {entries.map((entry) => (
                <EntryCard
                  key={entry.id}
                  entry={entry}
                  settings={settings}
                  actions={
                    <button
                      onClick={() => openConfirm('archive', entry.id)}
                      className="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
                      title="Archive entry"
                    >
                      <Archive size={18} />
                    </button>
                  }
                />
              ))}
            </div>

            {/* Desktop table layout */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50/50 border-b border-slate-200">
                    <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Turnover</th>
                    <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Tips (Cash / Card)</th>
                    <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Hours</th>
                    <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {entries.map((entry) => (
                    <EntryTableRow
                      key={entry.id}
                      entry={entry}
                      settings={settings}
                      actions={
                        <button
                          onClick={() => openConfirm('archive', entry.id)}
                          className="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-all"
                          title="Archive entry"
                        >
                          <Archive size={18} />
                        </button>
                      }
                    />
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>

      {inactiveEntries.length > 0 && (
        <div className="space-y-3">
          <button
            onClick={() => setArchivedOpen(prev => !prev)}
            className="flex items-center gap-2 text-slate-600 hover:text-slate-900 transition-colors"
          >
            <ChevronDown
              size={18}
              className={`transition-transform ${archivedOpen ? 'rotate-0' : '-rotate-90'}`}
            />
            <span className="text-sm font-semibold">
              Archived Entries ({inactiveEntries.length})
            </span>
          </button>

          {archivedOpen && (
            <div className="space-y-3">
              {/* Mobile */}
              <div className="md:hidden space-y-3">
                {inactiveEntries.map((entry) => (
                  <EntryCard
                    key={entry.id}
                    entry={entry}
                    settings={settings}
                    muted
                    actions={
                      <>
                        <button
                          onClick={() => handleReactivate(entry.id)}
                          className="p-2 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                          title="Reactivate entry"
                        >
                          <RotateCcw size={18} />
                        </button>
                        <button
                          onClick={() => openConfirm('delete', entry.id)}
                          className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Delete permanently"
                        >
                          <Trash2 size={18} />
                        </button>
                      </>
                    }
                  />
                ))}
              </div>

              {/* Desktop */}
              <div className="hidden md:block bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden opacity-90">
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50/50 border-b border-slate-200">
                        <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Date</th>
                        <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Turnover</th>
                        <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Tips (Cash / Card)</th>
                        <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Hours</th>
                        <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {inactiveEntries.map((entry) => (
                        <EntryTableRow
                          key={entry.id}
                          entry={entry}
                          settings={settings}
                          muted
                          actions={
                            <>
                              <button
                                onClick={() => handleReactivate(entry.id)}
                                className="p-2 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-all"
                                title="Reactivate entry"
                              >
                                <RotateCcw size={18} />
                              </button>
                              <button
                                onClick={() => openConfirm('delete', entry.id)}
                                className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all"
                                title="Delete permanently"
                              >
                                <Trash2 size={18} />
                              </button>
                            </>
                          }
                        />
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {activeConfig && (
        <ConfirmModal
          open={!!confirmAction}
          title={activeConfig.title}
          message={activeConfig.message}
          confirmLabel={activeConfig.confirmLabel}
          variant={activeConfig.variant}
          loading={actionLoading}
          onConfirm={handleConfirm}
          onCancel={closeConfirm}
        />
      )}
    </div>
  );
}
