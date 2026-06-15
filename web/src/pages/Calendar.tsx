import { useMemo, useState } from 'react';
import { useEntries } from '../hooks/useEntries';
import { useSettings, formatCurrency } from '../hooks/useSettings';
import { parseISO, format, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth, isToday } from 'date-fns';
import { ChevronLeft, ChevronRight, X } from 'lucide-react';

export default function Calendar() {
  const { entries } = useEntries();
  const { settings } = useSettings();
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  const daysInMonth = useMemo(() => {
    const start = startOfMonth(currentMonth);
    const end = endOfMonth(currentMonth);
    return eachDayOfInterval({ start, end });
  }, [currentMonth]);

  const firstDayOfWeek = startOfMonth(currentMonth).getDay();
  
  const entriesByDate = useMemo(() => {
    const map = new Map<string, typeof entries>();
    entries.filter(e => isSameMonth(parseISO(e.date), currentMonth)).forEach(entry => {
      if (!map.has(entry.date)) {
        map.set(entry.date, []);
      }
      map.get(entry.date)!.push(entry);
    });
    return map;
  }, [entries, currentMonth]);

  const selectedEntries = selectedDate ? entriesByDate.get(selectedDate) || [] : [];
  const selectedStats = selectedEntries.reduce((acc, e) => ({
    turnover: acc.turnover + e.turnover,
    tipsCash: acc.tipsCash + (e.tipsCash || 0),
    tipsCard: acc.tipsCard + (e.tipsCard || 0),
    hours: acc.hours + (e.hoursWorked || 0)
  }), { turnover: 0, tipsCash: 0, tipsCard: 0, hours: 0 });

  const totalTips = selectedStats.tipsCash + selectedStats.tipsCard;
  const hourlyWage = selectedStats.hours * (settings.hourlyRate || 0);
  const notesForDay = selectedEntries
    .map(e => e.notes?.trim())
    .filter((note): note is string => Boolean(note));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight text-slate-900">Calendar</h2>
        <div className="flex items-center gap-4">
          <button
            onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1))}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ChevronLeft size={20} className="text-slate-600" />
          </button>
          <span className="text-lg font-semibold text-slate-900 min-w-[140px] text-center">
            {format(currentMonth, 'MMMM yyyy')}
          </span>
          <button
            onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1))}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ChevronRight size={20} className="text-slate-600" />
          </button>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        {/* Day Headers */}
        <div className="grid grid-cols-7 gap-2 mb-4">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
            <div key={day} className="text-center text-sm font-semibold text-slate-500 py-2">
              {day}
            </div>
          ))}
        </div>

        {/* Calendar Days */}
        <div className="grid grid-cols-7 gap-2">
          {/* Empty cells for days before month starts */}
          {Array.from({ length: firstDayOfWeek }).map((_, i) => (
            <div key={`empty-${i}`} className="aspect-square" />
          ))}

          {/* Actual days */}
          {daysInMonth.map(day => {
            const dateStr = format(day, 'yyyy-MM-dd');
            const dayEntries = entriesByDate.get(dateStr) || [];
            const dayTips = dayEntries.reduce((sum, e) => sum + (e.tipsCash || 0) + (e.tipsCard || 0), 0);
            const hasData = dayEntries.length > 0;
            const isSelected = selectedDate === dateStr;
            const isTodayDate = isToday(day);

            return (
              <button
                key={dateStr}
                onClick={() => setSelectedDate(dateStr)}
                className={`aspect-square p-2 rounded-xl border-2 transition-all relative ${
                  isSelected 
                    ? 'border-blue-500 bg-blue-50' 
                    : isTodayDate
                    ? 'border-blue-300 bg-blue-50/50'
                    : hasData
                    ? 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:shadow-sm'
                    : 'border-transparent hover:bg-slate-50'
                }`}
              >
                <div className="flex flex-col h-full">
                  <span className={`text-sm font-semibold ${
                    isSelected ? 'text-blue-600' : isTodayDate ? 'text-blue-600' : 'text-slate-700'
                  }`}>
                    {format(day, 'd')}
                  </span>
                  {hasData && (
                    <span className="text-xs text-emerald-600 font-medium mt-auto">
                      {formatCurrency(dayTips, settings.currency)}
                    </span>
                  )}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Selected Day Modal */}
      {selectedDate && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50" onClick={() => setSelectedDate(null)}>
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-2xl font-bold text-slate-900">
                Entry for {format(parseISO(selectedDate), 'MMM d, yyyy')}
              </h3>
              <button
                onClick={() => setSelectedDate(null)}
                className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <X size={20} className="text-slate-600" />
              </button>
            </div>

            {selectedEntries.length === 0 ? (
              <p className="text-slate-500 text-center py-8">No entries for this day</p>
            ) : (
              <div className="space-y-4">
                <div className="bg-slate-50 rounded-xl p-4 space-y-3">
                  <div className="flex justify-between">
                    <span className="text-slate-600">Turnover:</span>
                    <span className="font-semibold text-slate-900">{formatCurrency(selectedStats.turnover, settings.currency)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600">Tips Cash:</span>
                    <span className="font-semibold text-slate-900">{formatCurrency(selectedStats.tipsCash, settings.currency)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600">Tips Card:</span>
                    <span className="font-semibold text-slate-900">{formatCurrency(selectedStats.tipsCard, settings.currency)}</span>
                  </div>
                  <div className="flex justify-between border-t border-slate-200 pt-3">
                    <span className="text-slate-700 font-medium">Total Tips:</span>
                    <span className="font-bold text-blue-600 text-lg">{formatCurrency(totalTips, settings.currency)}</span>
                  </div>
                </div>

                <div className="bg-purple-50 rounded-xl p-4 space-y-3">
                  <div className="flex justify-between">
                    <span className="text-slate-600">Hours Worked:</span>
                    <span className="font-semibold text-slate-900">{selectedStats.hours.toFixed(1)} hrs</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600">Hourly Wage:</span>
                    <span className="font-semibold text-purple-600">{formatCurrency(hourlyWage, settings.currency)}</span>
                  </div>
                </div>

                {notesForDay.length > 0 && (
                  <div className="bg-amber-50 rounded-xl p-4 space-y-2">
                    <span className="text-sm font-semibold text-slate-700">Notes:</span>
                    {notesForDay.map((note, index) => (
                      <p key={index} className="text-sm text-slate-600 whitespace-pre-wrap">{note}</p>
                    ))}
                  </div>
                )}

                <button
                  onClick={() => setSelectedDate(null)}
                  className="w-full bg-blue-600 text-white font-semibold py-3 rounded-xl hover:bg-blue-700 transition-colors mt-4"
                >
                  Close
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
