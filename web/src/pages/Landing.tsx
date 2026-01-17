import { Link } from 'react-router-dom';
import { Wallet, TrendingUp, Calendar, DollarSign, BarChart3, Settings } from 'lucide-react';

export default function Landing() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-slate-100">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white/80 backdrop-blur-md border-b border-slate-200 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-blue-600 rounded-lg">
              <Wallet size={24} className="text-white" />
            </div>
            <span className="text-xl font-bold text-slate-900">Waiter Wallet</span>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="px-4 py-2 text-slate-700 hover:text-blue-600 font-medium transition-colors"
            >
              Sign In
            </Link>
            <Link
              to="/register"
              className="px-6 py-2 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 transition-colors shadow-sm"
            >
              Get Started
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="pt-32 pb-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-blue-100 text-blue-700 rounded-full text-sm font-medium mb-8">
            <TrendingUp size={16} />
            <span>Track Your Earnings Effortlessly</span>
          </div>
          
          <h1 className="text-5xl md:text-6xl font-bold text-slate-900 mb-6 leading-tight">
            The Smart Way to<br />
            <span className="bg-gradient-to-r from-blue-600 to-blue-400 bg-clip-text text-transparent">
              Track Your Income
            </span>
          </h1>
          
          <p className="text-xl text-slate-600 mb-10 max-w-2xl mx-auto">
            Designed for waiters and service workers. Track tips, commission, hourly wages, and get insights into your earnings with beautiful visualizations.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="px-8 py-4 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 transition-all shadow-lg hover:shadow-xl transform hover:scale-105"
            >
              Start Tracking Free
            </Link>
            <Link
              to="/login"
              className="px-8 py-4 bg-white text-slate-900 font-semibold rounded-xl hover:bg-slate-50 transition-all border-2 border-slate-200"
            >
              Sign In
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4 sm:px-6 lg:px-8 bg-white/50">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4">
              Everything You Need in One Place
            </h2>
            <p className="text-lg text-slate-600 max-w-2xl mx-auto">
              Comprehensive income tracking with powerful features designed specifically for service workers.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
              <div className="p-3 bg-blue-100 rounded-xl w-fit mb-4">
                <DollarSign size={28} className="text-blue-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Comprehensive Earnings</h3>
              <p className="text-slate-600">
                Track tips (cash & card), hourly wages, and commission all in one dashboard with automatic calculations.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
              <div className="p-3 bg-purple-100 rounded-xl w-fit mb-4">
                <BarChart3 size={28} className="text-purple-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Visual Analytics</h3>
              <p className="text-slate-600">
                Beautiful charts and graphs showing your earnings trends over time to help you understand your income.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
              <div className="p-3 bg-emerald-100 rounded-xl w-fit mb-4">
                <Calendar size={28} className="text-emerald-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Calendar View</h3>
              <p className="text-slate-600">
                See your daily earnings at a glance with an intuitive calendar interface showing tips and wages per day.
              </p>
            </div>

            {/* Feature 4 */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
              <div className="p-3 bg-amber-100 rounded-xl w-fit mb-4">
                <TrendingUp size={28} className="text-amber-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Monthly Goals</h3>
              <p className="text-slate-600">
                Set income goals and track your progress throughout the month with visual progress indicators.
              </p>
            </div>

            {/* Feature 5 */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
              <div className="p-3 bg-rose-100 rounded-xl w-fit mb-4">
                <Settings size={28} className="text-rose-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Customizable Settings</h3>
              <p className="text-slate-600">
                Configure your hourly rate, commission percentage, and preferred currency to match your needs.
              </p>
            </div>

            {/* Feature 6 */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
              <div className="p-3 bg-cyan-100 rounded-xl w-fit mb-4">
                <Wallet size={28} className="text-cyan-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Multi-Job Support</h3>
              <p className="text-slate-600">
                Track earnings from multiple jobs separately and see your total income across all positions.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto bg-gradient-to-r from-blue-600 to-blue-700 rounded-3xl p-12 text-center shadow-2xl">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Ready to Take Control of Your Earnings?
          </h2>
          <p className="text-xl text-blue-100 mb-8">
            Join waiters worldwide who are already tracking their income smarter.
          </p>
          <Link
            to="/register"
            className="inline-block px-8 py-4 bg-white text-blue-600 font-bold rounded-xl hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl transform hover:scale-105"
          >
            Get Started for Free
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-8 px-4 sm:px-6 lg:px-8 border-t border-slate-200 bg-white">
        <div className="max-w-7xl mx-auto text-center text-slate-600">
          <div className="flex items-center justify-center gap-2 mb-2">
            <div className="p-1.5 bg-blue-600 rounded-lg">
              <Wallet size={20} className="text-white" />
            </div>
            <span className="font-bold text-slate-900">Waiter Wallet</span>
          </div>
          <p className="text-sm">
            &copy; {new Date().getFullYear()} Waiter Wallet. Track your earnings with confidence.
          </p>
        </div>
      </footer>
    </div>
  );
}
