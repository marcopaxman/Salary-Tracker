import { useMemo, useState } from 'react';
import { useEntries } from '../hooks/useEntries';
import { useGoals } from '../hooks/useGoals';
import { useSettings, formatCurrency, CURRENCIES } from '../hooks/useSettings';
import { parseISO, format, subMonths, subDays, isSameMonth, endOfMonth } from 'date-fns';
import { TrendingUp, ChevronRight, Calculator, Clock, Wallet, CreditCard, Banknote, CalendarCheck, CalendarPlus } from 'lucide-react';
import WeeklyTipsChart from '../components/WeeklyTipsChart';
import MonthlyEarningsChart from '../components/MonthlyEarningsChart';
import CloseMonthModal from '../components/CloseMonthModal';
import ConfirmModal from '../components/ConfirmModal';
import {
  filterEntriesByPayrollMonth,
  getCarryOverCandidates,
  getCarriedOverEntries,
  getNextPayrollMonth,
  isMonthClosed,
} from '../utils/payrollMonth';

export default function Dashboard() {
  const { entries, loading: entriesLoading, updateEntryPayrollMonth } = useEntries();
  const { getGoalForMonth, updateGoal } = useGoals();
  const { settings, updateSettings } = useSettings();

  const [isEditingGoal, setIsEditingGoal] = useState(false);
  const [newGoalAmount, setNewGoalAmount] = useState('');
  const [selectedMonth, setSelectedMonth] = useState(format(new Date(), 'yyyy-MM'));
  const [showCloseConfirm, setShowCloseConfirm] = useState(false);
  const [showCloseModal, setShowCloseModal] = useState(false);
  const [showReopenConfirm, setShowReopenConfirm] = useState(false);
  const [closeLoading, setCloseLoading] = useState(false);

  const currentMonthDate = parseISO(`${selectedMonth}-01`);
  const currentGoal = getGoalForMonth(currentMonthDate);
  const monthClosed = isMonthClosed(settings.closedMonths, selectedMonth);

  const monthlyEntries = useMemo(
    () => filterEntriesByPayrollMonth(entries, selectedMonth),
    [entries, selectedMonth]
  );

  const stats = useMemo(() => {
    return monthlyEntries.reduce((acc, entry) => ({
      turnover: acc.turnover + entry.turnover,
      tips: acc.tips + (entry.tipsCash || 0) + (entry.tipsCard || 0),
      tipsCash: acc.tipsCash + (entry.tipsCash || 0),
      tipsCard: acc.tipsCard + (entry.tipsCard || 0),
      hours: acc.hours + (entry.hoursWorked || 0),
    }), { turnover: 0, tips: 0, tipsCash: 0, tipsCard: 0, hours: 0 });
  }, [monthlyEntries]);

  const weeklyData = useMemo(() => {
    const today = new Date();
    const anchor = isSameMonth(today, currentMonthDate) ? today : endOfMonth(currentMonthDate);

    const days = Array.from({ length: 7 }, (_, i) =>
      format(subDays(anchor, 6 - i), 'yyyy-MM-dd')
    );

    return days.map(dayStr => {
      const dayEntries = entries.filter(e => e.date === dayStr);
      const totalTips = dayEntries.reduce((sum, e) => sum + (e.tipsCash || 0) + (e.tipsCard || 0), 0);
      return {
        name: format(parseISO(dayStr), 'EEE'),
        tips: totalTips,
      };
    });
  }, [entries, currentMonthDate, selectedMonth]);

  const monthlyEarningsData = useMemo(() => {
    const months = [];
    for (let i = 5; i >= 0; i--) {
      const monthDate = subMonths(currentMonthDate, i);
      const payrollMonth = format(monthDate, 'yyyy-MM');
      const monthEntries = filterEntriesByPayrollMonth(entries, payrollMonth);

      const monthStats = monthEntries.reduce((acc, entry) => ({
        tips: acc.tips + (entry.tipsCash || 0) + (entry.tipsCard || 0),
        turnover: acc.turnover + entry.turnover,
        hours: acc.hours + (entry.hoursWorked || 0),
      }), { tips: 0, turnover: 0, hours: 0 });

      const commission = monthStats.turnover * (settings.commissionPercent || 0.01);
      const wages = monthStats.hours * (settings.hourlyRate || 0);
      const total = monthStats.tips + commission + wages;

      months.push({
        name: format(monthDate, 'MMM'),
        total,
      });
    }
    return months;
  }, [entries, currentMonthDate, settings.commissionPercent, settings.hourlyRate]);

  const carryOverCandidates = useMemo(
    () => getCarryOverCandidates(entries, selectedMonth),
    [entries, selectedMonth]
  );

  const handleSaveGoal = async () => {
    if (!newGoalAmount) return;
    await updateGoal(currentMonthDate, parseFloat(newGoalAmount));
    setIsEditingGoal(false);
  };

  async function handleCloseMonth(carryOverIds: string[]) {
    const nextMonth = getNextPayrollMonth(selectedMonth);
    setCloseLoading(true);
    try {
      await Promise.all(
        carryOverIds.map(id => updateEntryPayrollMonth(id, nextMonth))
      );
      const closed = [...(settings.closedMonths || [])];
      if (!closed.includes(selectedMonth)) {
        closed.push(selectedMonth);
      }
      await updateSettings({ closedMonths: closed });
      setShowCloseModal(false);
      setSelectedMonth(nextMonth);
    } finally {
      setCloseLoading(false);
    }
  }

  async function handleReopenMonth() {
    setCloseLoading(true);
    try {
      const carried = getCarriedOverEntries(entries, selectedMonth);
      await Promise.all(
        carried.map(entry => updateEntryPayrollMonth(entry.id, null))
      );
      const closed = (settings.closedMonths || []).filter(m => m !== selectedMonth);
      await updateSettings({ closedMonths: closed });
      setShowReopenConfirm(false);
    } finally {
      setCloseLoading(false);
    }
  }

  const monthLabel = format(currentMonthDate, 'MMMM yyyy');
  const carriedCount = getCarriedOverEntries(entries, selectedMonth).length;

  if (entriesLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 animate-pulse">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-40 bg-gray-200 rounded-xl"></div>
        ))}
      </div>
    );
  }

  const goalProgress = currentGoal && currentGoal.goalTips > 0
    ? Math.min((stats.tips / currentGoal.goalTips) * 100, 100)
    : 0;

  const estimatedCommission = stats.turnover * (settings.commissionPercent || 0.01);
  const hourlyWages = stats.hours * (settings.hourlyRate || 0);
  const totalMade = stats.tips + estimatedCommission + hourlyWages;
  const currencySymbol = CURRENCIES.find(c => c.code === settings.currency)?.symbol || '€';

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-slate-900">Dashboard</h2>
          <p className="text-slate-500 mt-1">
            Payroll period: {format(currentMonthDate, 'MMMM yyyy')}
            {monthClosed && (
              <span className="ml-2 text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                Closed
              </span>
            )}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <input
            type="month"
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="px-4 py-2 border border-slate-200 rounded-lg shadow-sm text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {!monthClosed ? (
            <button
              onClick={() => setShowCloseConfirm(true)}
              className="flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm font-semibold rounded-lg transition-colors"
            >
              <CalendarCheck size={16} />
              Close Month
            </button>
          ) : (
            <button
              onClick={() => setShowReopenConfirm(true)}
              className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold rounded-lg transition-colors"
            >
              <CalendarPlus size={16} />
              Reopen Month
            </button>
          )}
        </div>
      </div>

      {/* Total Made Card */}
      <div className="bg-gradient-to-br from-blue-600 to-blue-700 p-8 rounded-2xl shadow-lg text-white">
        <div className="flex items-center gap-3 mb-3">
          <Wallet size={28} className="opacity-90" />
          <h3 className="text-lg font-medium opacity-90">Total Made This Month</h3>
        </div>
        <p className="text-5xl font-bold tracking-tight mb-2">{formatCurrency(totalMade, settings.currency)}</p>
        <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm opacity-80 mt-4">
          <span>Tips: {formatCurrency(stats.tips, settings.currency)}</span>
          <span>•</span>
          <span>Commission: {formatCurrency(estimatedCommission, settings.currency)}</span>
          <span>•</span>
          <span>Wages: {formatCurrency(hourlyWages, settings.currency)}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity flex items-center justify-center">
            <span className="text-[80px] leading-none text-blue-600 font-light select-none">{currencySymbol}</span>
          </div>
          <div>
            <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Total Turnover</h3>
            <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
              {formatCurrency(stats.turnover, settings.currency)}
            </p>
          </div>
          <div className="mt-4 flex items-center gap-2 text-sm text-green-600 font-medium bg-green-50 w-fit px-2 py-1 rounded-full">
            <TrendingUp size={16} />
            <span>Monthly</span>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <Clock size={80} className="text-purple-600" />
          </div>
          <div>
            <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Hours Worked</h3>
            <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
              {stats.hours.toFixed(1)}
            </p>
          </div>
          <div className="mt-4 text-sm text-slate-600">
            <span className="font-medium">Hourly Wages: </span>
            <span className="text-purple-600 font-semibold">{formatCurrency(hourlyWages, settings.currency)}</span>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <Calculator size={80} className="text-emerald-600" />
          </div>
          <div>
            <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">
              Commission ({(settings.commissionPercent * 100).toFixed(1)}%)
            </h3>
            <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
              {formatCurrency(estimatedCommission, settings.currency)}
            </p>
          </div>
          <div className="mt-4 text-sm text-slate-500">From turnover</div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <Banknote size={80} className="text-green-600" />
          </div>
          <div>
            <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Cash Tips</h3>
            <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
              {formatCurrency(stats.tipsCash, settings.currency)}
            </p>
          </div>
          <div className="mt-4 text-sm text-slate-500">Total cash tips received</div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <CreditCard size={80} className="text-blue-500" />
          </div>
          <div>
            <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Card Tips</h3>
            <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
              {formatCurrency(stats.tipsCard, settings.currency)}
            </p>
          </div>
          <div className="mt-4 text-sm text-slate-500">Total card tips received</div>
        </div>
      </div>

      <MonthlyEarningsChart data={monthlyEarningsData} currency={settings.currency} />
      <WeeklyTipsChart data={weeklyData} currency={settings.currency} />

      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Monthly Tips</h3>
            <p className="text-4xl font-bold text-slate-900 mt-2 tracking-tight">
              {formatCurrency(stats.tips, settings.currency)}
            </p>
          </div>
          <button
            onClick={() => {
              setNewGoalAmount(currentGoal?.goalTips.toString() || '');
              setIsEditingGoal(true);
            }}
            className="text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1 bg-blue-50 px-2 py-1 rounded-lg"
          >
            {currentGoal ? 'Edit Goal' : 'Set Goal'} <ChevronRight size={12} />
          </button>
        </div>

        {isEditingGoal ? (
          <div className="flex gap-2 animate-in fade-in slide-in-from-top-2">
            <input
              type="number"
              autoFocus
              placeholder="Goal amount"
              className="w-full px-2 py-1 text-sm border rounded"
              value={newGoalAmount}
              onChange={e => setNewGoalAmount(e.target.value)}
            />
            <button onClick={handleSaveGoal} className="bg-emerald-600 text-white px-3 py-1 rounded text-xs font-bold">Save</button>
            <button onClick={() => setIsEditingGoal(false)} className="text-slate-400 text-xs hover:text-slate-600">Cancel</button>
          </div>
        ) : currentGoal ? (
          <div className="space-y-1.5">
            <div className="flex justify-between text-xs font-medium">
              <span className="text-slate-500">Goal: {formatCurrency(currentGoal.goalTips, settings.currency)}</span>
              <span className={goalProgress >= 100 ? 'text-emerald-600' : 'text-blue-600'}>{goalProgress.toFixed(0)}%</span>
            </div>
            <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-1000 ${goalProgress >= 100 ? 'bg-emerald-500' : 'bg-blue-500'}`}
                style={{ width: `${goalProgress}%` }}
              />
            </div>
          </div>
        ) : (
          <div className="text-sm text-slate-400 italic">No goal set for this month</div>
        )}
      </div>

      <ConfirmModal
        open={showCloseConfirm}
        title={`Close ${monthLabel}?`}
        message={`This will finalize your ${monthLabel} payroll period and move you to ${format(parseISO(`${getNextPayrollMonth(selectedMonth)}-01`), 'MMMM yyyy')}. You'll be able to select any late-month shifts to carry over.`}
        confirmLabel="Continue"
        variant="warning"
        onConfirm={() => {
          setShowCloseConfirm(false);
          setShowCloseModal(true);
        }}
        onCancel={() => setShowCloseConfirm(false)}
      />

      <ConfirmModal
        open={showReopenConfirm}
        title={`Reopen ${monthLabel}?`}
        message={
          carriedCount > 0
            ? `This will reopen ${monthLabel} and move ${carriedCount} carried-over ${carriedCount === 1 ? 'entry' : 'entries'} back into this month's calculations.`
            : `This will reopen ${monthLabel} so you can continue editing this payroll period.`
        }
        confirmLabel="Reopen Month"
        variant="warning"
        loading={closeLoading}
        onConfirm={handleReopenMonth}
        onCancel={() => setShowReopenConfirm(false)}
      />

      <CloseMonthModal
        open={showCloseModal}
        month={selectedMonth}
        candidates={carryOverCandidates}
        currency={settings.currency}
        loading={closeLoading}
        onConfirm={handleCloseMonth}
        onCancel={() => setShowCloseModal(false)}
      />
    </div>
  );
}
