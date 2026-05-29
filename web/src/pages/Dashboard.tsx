import { useMemo, useState } from 'react';
import { useEntries } from '../hooks/useEntries';
import { useGoals } from '../hooks/useGoals';
import { useSettings, formatCurrency, CURRENCIES } from '../hooks/useSettings';
import { startOfMonth, endOfMonth, isWithinInterval, parseISO, format, subMonths } from 'date-fns';
import { TrendingUp, ChevronRight, Calculator, Clock, Wallet, CreditCard, Banknote } from 'lucide-react';
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

export default function Dashboard() {
  const { entries, loading: entriesLoading } = useEntries();
  const { getGoalForMonth, updateGoal } = useGoals();
  const { settings } = useSettings();
  
  const [isEditingGoal, setIsEditingGoal] = useState(false);
  const [newGoalAmount, setNewGoalAmount] = useState('');
  const [selectedMonth, setSelectedMonth] = useState(format(new Date(), 'yyyy-MM'));

  const currentMonthDate = parseISO(`${selectedMonth}-01`);
  const currentGoal = getGoalForMonth(currentMonthDate);

  const stats = useMemo(() => {
    const start = startOfMonth(currentMonthDate);
    const end = endOfMonth(currentMonthDate);

    const monthlyEntries = entries.filter(entry => 
      isWithinInterval(parseISO(entry.date), { start, end })
    );

    return monthlyEntries.reduce((acc, entry) => ({
      turnover: acc.turnover + entry.turnover,
      tips: acc.tips + (entry.tipsCash || 0) + (entry.tipsCard || 0),
      tipsCash: acc.tipsCash + (entry.tipsCash || 0),
      tipsCard: acc.tipsCard + (entry.tipsCard || 0),
      hours: acc.hours + (entry.hoursWorked || 0)
    }), { turnover: 0, tips: 0, tipsCash: 0, tipsCard: 0, hours: 0 });
  }, [entries, currentMonthDate]);

  // Chart Data Preparation - Last 7 days
  const weeklyData = useMemo(() => {
     const days = [];
     for (let i = 6; i >= 0; i--) {
         const d = new Date(currentMonthDate);
         d.setDate(d.getDate() - i);
         days.push(format(d, 'yyyy-MM-dd'));
     }

     return days.map(dayStr => {
         const dayEntries = entries.filter(e => e.date === dayStr);
         const totalTips = dayEntries.reduce((sum, e) => sum + (e.tipsCash || 0) + (e.tipsCard || 0), 0);
         return {
             name: format(parseISO(dayStr), 'EEE'), // Mon, Tue
             tips: totalTips
         };
     });
  }, [entries, currentMonthDate]);

  // Monthly Earnings Data - Last 6 months
  const monthlyEarningsData = useMemo(() => {
    const months = [];
    for (let i = 5; i >= 0; i--) {
      const monthDate = subMonths(currentMonthDate, i);
      const start = startOfMonth(monthDate);
      const end = endOfMonth(monthDate);

      const monthEntries = entries.filter(entry =>
        isWithinInterval(parseISO(entry.date), { start, end })
      );

      const monthStats = monthEntries.reduce((acc, entry) => ({
        tips: acc.tips + (entry.tipsCash || 0) + (entry.tipsCard || 0),
        turnover: acc.turnover + entry.turnover,
        hours: acc.hours + (entry.hoursWorked || 0)
      }), { tips: 0, turnover: 0, hours: 0 });

      const commission = monthStats.turnover * (settings.commissionPercent || 0.01);
      const wages = monthStats.hours * (settings.hourlyRate || 0);
      const total = monthStats.tips + commission + wages;

      months.push({
        name: format(monthDate, 'MMM'),
        total: total
      });
    }
    return months;
  }, [entries, currentMonthDate, settings.commissionPercent, settings.hourlyRate]);

  const handleSaveGoal = async () => {
      if (!newGoalAmount) return;
      await updateGoal(currentMonthDate, parseFloat(newGoalAmount));
      setIsEditingGoal(false);
  };

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
    <div className="space-y-8 pb-24 md:pb-0">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-4">
        <div>
           <h2 className="text-3xl font-bold tracking-tight text-slate-900">Dashboard</h2>
           <p className="text-slate-500 mt-1">Overview for {format(currentMonthDate, 'MMMM yyyy')}</p>
        </div>
        <div className="flex items-center">
          <input
            type="month"
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="px-4 py-2 border border-slate-200 rounded-lg shadow-sm text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Total Made Card - Featured */}
      <div className="bg-gradient-to-br from-blue-600 to-blue-700 p-8 rounded-2xl shadow-lg text-white">
        <div className="flex items-center gap-3 mb-3">
          <Wallet size={28} className="opacity-90" />
          <h3 className="text-lg font-medium opacity-90">Total Made This Month</h3>
        </div>
        <p className="text-5xl font-bold tracking-tight mb-2">{formatCurrency(totalMade, settings.currency)}</p>
        <div className="flex gap-4 text-sm opacity-80 mt-4">
          <span>Tips: {formatCurrency(stats.tips, settings.currency)}</span>
          <span>•</span>
          <span>Commission: {formatCurrency(estimatedCommission, settings.currency)}</span>
          <span>•</span>
          <span>Wages: {formatCurrency(hourlyWages, settings.currency)}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Turnover Card */}
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

        {/* Hours Worked Card */}
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

        {/* Commission Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
               <Calculator size={80} className="text-emerald-600" />
            </div>
            <div>
              <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Commission ({(settings.commissionPercent * 100).toFixed(1)}%)</h3>
              <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
                {formatCurrency(estimatedCommission, settings.currency)}
              </p>
            </div>
            <div className="mt-4 text-sm text-slate-500">
              From turnover
            </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Cash Tips Card */}
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
            <div className="mt-4 text-sm text-slate-500">
              Total cash tips received
            </div>
        </div>

        {/* Card Tips Card */}
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
            <div className="mt-4 text-sm text-slate-500">
              Total card tips received
            </div>
        </div>
      </div>

      {/* Monthly Earnings Chart */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
        <h3 className="text-lg font-semibold text-slate-900 mb-4">Monthly Earnings Trend</h3>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={monthlyEarningsData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="name" tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
              <YAxis tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
              <Tooltip 
                contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                formatter={(value: any) => [`${currencySymbol}${Number(value || 0).toFixed(2)}`, 'Total Earnings']}
              />
              <Line type="monotone" dataKey="total" stroke="#3b82f6" strokeWidth={3} dot={{ fill: '#3b82f6', r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Tips & Goals Card */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group md:col-span-2">
         <div className="flex flex-col md:flex-row gap-8 h-full">
             {/* Tips Left Side */}
             <div className="flex-1 flex flex-col justify-between z-10">
                 <div>
                    <div className="flex justify-between items-start">
                        <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Weekly Tips</h3>
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
                    <p className="text-4xl font-bold text-slate-900 mt-4 tracking-tight">
                      {formatCurrency(stats.tips, settings.currency)}
                    </p>
                 </div>
                 
                 <div className="mt-4">
                    {isEditingGoal ? (
                       <div className="flex gap-2 animate-in fade-in slide-in-from-top-2">
                           <input 
                             type="number" 
                             autoFocus
                             placeholder={`Goal ${currencySymbol}`}
                             className="w-full px-2 py-1 text-sm border rounded"
                             value={newGoalAmount}
                             onChange={e => setNewGoalAmount(e.target.value)}
                           />
                           <button onClick={handleSaveGoal} className="bg-emerald-600 text-white px-3 py-1 rounded text-xs font-bold">Save</button>
                           <button onClick={() => setIsEditingGoal(false)} className="text-slate-400 text-xs hover:text-slate-600">Cancel</button>
                       </div>
                    ) : (
                        currentGoal ? (
                          <div className="space-y-1.5">
                              <div className="flex justify-between text-xs font-medium">
                                  <span className="text-slate-500">Goal: {formatCurrency(currentGoal.goalTips, settings.currency)}</span>
                                  <span className={goalProgress >= 100 ? "text-emerald-600" : "text-blue-600"}>{goalProgress.toFixed(0)}%</span>
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
                        )
                    )}
                 </div>
             </div>

             {/* Chart Right Side */}
             <div className="flex-1 h-32 md:h-auto min-h-[150px]">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={weeklyData}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis dataKey="name" tick={{fontSize: 10, fill: '#64748b'}} axisLine={false} tickLine={false} />
                    <Tooltip 
                      contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                      cursor={{ fill: '#f8fafc' }}
                      formatter={(value: any) => [`${currencySymbol}${Number(value || 0).toFixed(2)}`, 'Tips']}
                    />
                    <Bar dataKey="tips" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
             </div>
         </div>
      </div>
    </div>
  );
}
