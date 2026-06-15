import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { CURRENCIES } from '../hooks/useSettings';

interface WeeklyTipsChartProps {
  data: { name: string; tips: number }[];
  currency: string;
}

export default function WeeklyTipsChart({ data, currency }: WeeklyTipsChartProps) {
  const currencySymbol = CURRENCIES.find(c => c.code === currency)?.symbol || '€';

  return (
    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
      <h3 className="text-lg font-semibold text-slate-900 mb-1">Daily Tips This Week</h3>
      <p className="text-sm text-slate-500 mb-4">Tips earned over the last 7 days</p>
      <div className="h-56">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
            <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#64748b' }} axisLine={false} tickLine={false} />
            <YAxis hide domain={[0, 'auto']} />
            <Tooltip
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
              formatter={(value) => [`${currencySymbol}${Number(value || 0).toFixed(2)}`, 'Tips']}
            />
            <Line type="monotone" dataKey="tips" stroke="#3b82f6" strokeWidth={3} dot={{ fill: '#3b82f6', r: 4 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
