import { useMemo, useState } from 'react';
import { useEntries } from '../hooks/useEntries';
import { useGoals } from '../hooks/useGoals';
import { useSettings, formatCurrency, CURRENCIES } from '../hooks/useSettings';
import { startOfMonth, endOfMonth, isWithinInterval, parseISO, format } from 'date-fns';
import { Euro, TrendingUp, ChevronRight, Calculator } from 'lucide-react';
import { BarChart, Bar, XAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

export default function Dashboard() {
  const { entries, loading: entriesLoading } = useEntries();
  const { getGoalForMonth, updateGoal } = useGoals();
  const { settings } = useSettings();
  
  const [isEditingGoal, setIsEditingGoal] = useState(false);
  const [newGoalAmount, setNewGoalAmount] = useState('');

  const currentMonthDate = new Date();
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
      hours: acc.hours + (entry.hoursWorked || 0)
    }), { turnover: 0, tips: 0, hours: 0 });
  }, [entries]);

  // Chart Data Preparation
  const weeklyData = useMemo(() => {
     // Get last 7 days from today? Or just last 7 days generally? 
     // Let's do last 7 days ending today.
     const days = [];
     for (let i = 6; i >= 0; i--) {
         const d = new Date();
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
  }, [entries]);

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

  const estimatedCommission = stats.turnover * (currentGoal?.commissionPercent || 0.01);
  const currencySymbol = CURRENCIES.find(c => c.code === settings.currency)?.symbol || '€';

  return (
    <div className="space-y-8 pb-24 md:pb-0">
      <div className="flex justify-between items-end">
        <div>
           <h2 className="text-3xl font-bold tracking-tight text-slate-900">Dashboard</h2>
           <p className="text-slate-500 mt-1">Overview for {format(currentMonthDate, 'MMMM yyyy')}</p>
        </div>
        <div className="text-right">
             <p className="text-sm font-medium text-slate-500">Estimated Commission (1%)</p>
             <div className="flex items-center justify-end gap-2 text-emerald-600">
               <Calculator size={18} />
               <span className="text-xl font-bold">{formatCurrency(estimatedCommission, settings.currency)}</span>
             </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Turnover Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group">
            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
               <Euro size={80} className="text-blue-600" />
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

        {/* Tips & Goals Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden group md:col-span-2">
           <div className="flex flex-col md:flex-row gap-8 h-full">
               {/* Tips Left Side */}
               <div className="flex-1 flex flex-col justify-between z-10">
                   <div>
                      <div className="flex justify-between items-start">
                          <h3 className="text-slate-500 font-medium text-sm uppercase tracking-wider">Total Tips</h3>
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
    </div>
  );
}
