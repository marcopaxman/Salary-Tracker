import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { LayoutDashboard, List, Briefcase, Settings, LogOut, Wallet } from 'lucide-react';
import { auth } from '../lib/firebase';

export default function Layout() {
  const location = useLocation();
  const navigate = useNavigate();

  const navigation = [
    { name: 'Dashboard', href: '/', icon: LayoutDashboard },
    { name: 'Entries', href: '/entries', icon: List },
    { name: 'Jobs', href: '/jobs', icon: Briefcase },
    { name: 'Settings', href: '/settings', icon: Settings },
  ];

  async function handleLogout() {
    try {
      await auth.signOut();
      navigate('/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  }

  return (
    <div className="flex h-screen bg-gray-50 font-sans">
      {/* Sidebar - Sleek Dark Design */}
      <div className="hidden md:flex flex-col w-72 bg-slate-900 border-r border-slate-800 text-slate-300">
        <div className="p-8 pb-4">
          <div className="flex items-center gap-3 text-white mb-2">
            <div className="p-2 bg-blue-600 rounded-lg">
               <Wallet size={24} className="text-white" />
            </div>
            <h1 className="text-xl font-bold tracking-tight">Waiter Wallet</h1>
          </div>
          <p className="text-xs text-slate-500 font-medium ml-1">Salary & Tip Tracker</p>
        </div>
        
        <nav className="flex-1 px-4 py-6 space-y-1">
          {navigation.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.href;
            return (
              <Link
                key={item.name}
                to={item.href}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 group ${
                  isActive 
                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/50' 
                    : 'hover:bg-slate-800 hover:text-white'
                }`}
              >
                <Icon size={20} className={isActive ? 'text-white' : 'text-slate-400 group-hover:text-white transition-colors'} />
                <span className="font-medium text-sm">{item.name}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-slate-800 mt-auto">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-4 py-3 w-full text-slate-400 hover:bg-red-500/10 hover:text-red-400 rounded-lg transition-all duration-200"
          >
            <LogOut size={20} />
            <span className="font-medium text-sm">Sign Out</span>
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden bg-gray-50">
        {/* Mobile Header */}
        <header className="md:hidden bg-white border-b border-gray-200 p-4 flex justify-between items-center sticky top-0 z-20 shadow-sm">
           <div className="flex items-center gap-2">
             <div className="p-1.5 bg-blue-600 rounded">
                <Wallet size={20} className="text-white" />
             </div>
             <h1 className="text-lg font-bold text-slate-900">Waiter Wallet</h1>
           </div>
           <button onClick={handleLogout} className="text-slate-500 hover:text-slate-700">
             <LogOut size={24} />
           </button>
        </header>

        <main className="flex-1 overflow-y-auto p-4 md:p-8 scroll-smooth">
          <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
            <Outlet />
          </div>
        </main>
      </div>
      
      {/* Mobile Bottom Nav - Glassmorphism effect */}
      <div className="md:hidden fixed bottom-6 left-4 right-4 bg-slate-900/90 backdrop-blur-lg border border-slate-800 shadow-2xl rounded-2xl flex justify-around p-2 z-30">
        {navigation.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.href;
            return (
              <Link
                key={item.name}
                to={item.href}
                className={`flex flex-col items-center justify-center p-3 rounded-xl transition-all ${
                  isActive ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <Icon size={20} />
              </Link>
            );
          })}
      </div>
    </div>
  );
}
