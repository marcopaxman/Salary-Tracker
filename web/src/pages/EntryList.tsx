import { useEntries } from '../hooks/useEntries';
import { useSettings, formatCurrency } from '../hooks/useSettings';
import { format, parseISO } from 'date-fns';
import { Edit2, Plus, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function EntryList() {
  const { entries, loading } = useEntries();
  const { settings } = useSettings();
  const navigate = useNavigate();

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
        <button 
          onClick={() => navigate('/entries/new')}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-semibold shadow-lg shadow-blue-600/20 transition-all active:scale-95"
        >
          <Plus size={20} />
          <span>Add Entry</span>
        </button>
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
                {entries.map((entry) => (
                  <tr key={entry.id} className="hover:bg-slate-50 transition-colors group">
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
                       <button 
                            onClick={() => navigate(`/entries/${entry.id}`)}
                            className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                       >
                            <Edit2 size={18} />
                       </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
