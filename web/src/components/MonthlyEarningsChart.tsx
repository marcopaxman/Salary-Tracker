import { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { CURRENCIES } from '../hooks/useSettings';

interface MonthlyEarningsChartProps {
  data: { name: string; total: number }[];
  currency: string;
}

/** Compute a Y-axis max one tick interval above the highest data value. */
function getYAxisMax(maxValue: number): number {
  if (maxValue <= 0) return 100;

  const roughInterval = maxValue / 4;
  const magnitude = Math.pow(10, Math.floor(Math.log10(roughInterval)));
  const normalized = roughInterval / magnitude;
  const niceUnit = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10;
  const interval = niceUnit * magnitude;

  const currentTop = Math.ceil(maxValue / interval) * interval;
  return currentTop + interval;
}

export default function MonthlyEarningsChart({ data, currency }: MonthlyEarningsChartProps) {
  const currencySymbol = CURRENCIES.find(c => c.code === currency)?.symbol || '€';

  const yAxisMax = useMemo(() => {
    const maxValue = Math.max(0, ...data.map(d => d.total));
    return getYAxisMax(maxValue);
  }, [data]);

  return (
    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
      <h3 className="text-lg font-semibold text-slate-900 mb-4">Monthly Earnings Trend</h3>
      <div className="h-64">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
            <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#64748b' }} axisLine={false} tickLine={false} />
            <YAxis
              tick={{ fontSize: 12, fill: '#64748b' }}
              axisLine={false}
              tickLine={false}
              domain={[0, yAxisMax]}
            />
            <Tooltip
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
              formatter={(value) => [`${currencySymbol}${Number(value || 0).toFixed(2)}`, 'Total Earnings']}
            />
            <Bar dataKey="total" fill="#3b82f6" radius={[4, 4, 0, 0]} minPointSize={4} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
