import { User, Shield, Wallet, LogOut, ChevronRight, Check } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useSettings, CURRENCIES } from '../hooks/useSettings';
import { useNavigate } from 'react-router-dom';
import { auth } from '../lib/firebase';
import { updatePassword } from 'firebase/auth';
import { useState } from 'react';

export default function Settings() {
  const { currentUser } = useAuth();
  const { settings, updateSettings, loading } = useSettings();
  const navigate = useNavigate();
  
  const [showCurrencySelector, setShowCurrencySelector] = useState(false);
  const [showPasswordInput, setShowPasswordInput] = useState(false);
  const [newPassword, setNewPassword] = useState('');

  async function handleLogout() {
    try {
      await auth.signOut();
      navigate('/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  }

  async function handleChangePassword() {
      if (!newPassword || newPassword.length < 6) {
          alert('Password must be at least 6 characters'); // Using alert for simplicity as per "remove notifications"
          return;
      }
      if (currentUser) {
          try {
              await updatePassword(currentUser, newPassword);
              alert('Password updated successfully!'); // Using alert for simplicity
              setNewPassword('');
              setShowPasswordInput(false);
          } catch (e: any) {
              console.error(e);
              alert('Error updating password: ' + e.message); // Using alert for simplicity
          }
      }
  }

  const selectedCurrency = CURRENCIES.find(c => c.code === settings.currency) || CURRENCIES[0];

  if (loading) {
      return <div className="p-8 text-center text-slate-400">Loading settings...</div>;
  }

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div>
        <h2 className="text-3xl font-bold tracking-tight text-slate-900">Settings</h2>
        <p className="text-slate-500 mt-1">Manage your account and preferences</p>
      </div>

      <div className="space-y-6">
        
        {/* Account Section */}
        <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider ml-1">Account</h3>
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden divide-y divide-slate-100">
                <div className="flex items-center justify-between p-4 bg-slate-50/50">
                    <div className="flex items-center gap-4">
                        <div className="p-2 bg-slate-100 text-slate-600 rounded-lg">
                            <User size={20} />
                        </div>
                        <div>
                            <p className="font-semibold text-slate-900">Profile Information</p>
                            <p className="text-sm text-slate-500">{currentUser?.email}</p>
                        </div>
                    </div>
                </div>
                
                <div className="flex flex-col">
                    <button 
                        onClick={() => setShowPasswordInput(!showPasswordInput)}
                        className="w-full flex items-center justify-between p-4 hover:bg-slate-50 transition-colors text-left group"
                    >
                        <div className="flex items-center gap-4">
                            <div className="p-2 bg-slate-100 text-slate-600 rounded-lg group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors">
                                <Shield size={20} />
                            </div>
                            <div>
                                <p className="font-semibold text-slate-900">Security</p>
                                <p className="text-sm text-slate-500">Change Password</p>
                            </div>
                        </div>
                        <ChevronRight size={20} className={`text-slate-300 group-hover:text-slate-500 transition-transform ${showPasswordInput ? 'rotate-90' : ''}`} />
                    </button>
                    
                    {showPasswordInput && (
                        <div className="p-4 bg-slate-50 border-t border-slate-100 animate-in slide-in-from-top-2 space-y-3">
                             <div className="flex gap-2">
                                 <input 
                                    type="password"
                                    placeholder="New Password"
                                    className="flex-1 px-4 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                                    value={newPassword}
                                    onChange={e => setNewPassword(e.target.value)}
                                 />
                                 <button 
                                    onClick={handleChangePassword}
                                    className="bg-blue-600 text-white px-4 py-2 rounded-xl font-medium hover:bg-blue-700 transition-colors"
                                 >
                                     Update
                                 </button>
                             </div>
                             <p className="text-xs text-slate-400">Note: Updating password may require recent login.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>

        {/* Preferences Section */}
        <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider ml-1">Preferences</h3>
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden divide-y divide-slate-100">
                
                {/* Currency Selector */}
                <div className="flex flex-col">
                    <button 
                        onClick={() => setShowCurrencySelector(!showCurrencySelector)}
                        className="w-full flex items-center justify-between p-4 hover:bg-slate-50 transition-colors text-left group"
                    >
                        <div className="flex items-center gap-4">
                            <div className="p-2 bg-slate-100 text-slate-600 rounded-lg group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors">
                                <Wallet size={20} />
                            </div>
                            <div>
                                <p className="font-semibold text-slate-900">Currency</p>
                                <p className="text-sm text-slate-500">{selectedCurrency.name} ({selectedCurrency.symbol})</p>
                            </div>
                        </div>
                        <ChevronRight size={20} className={`text-slate-300 group-hover:text-slate-500 transition-transform ${showCurrencySelector ? 'rotate-90' : ''}`} />
                    </button>
                    
                    {showCurrencySelector && (
                        <div className="bg-slate-50 border-t border-slate-100 animate-in slide-in-from-top-2">
                            {CURRENCIES.map(c => (
                                <button
                                    key={c.code}
                                    onClick={() => {
                                        updateSettings({ currency: c.code });
                                        setShowCurrencySelector(false);
                                    }}
                                    className="w-full flex items-center justify-between px-12 py-3 hover:bg-slate-100 text-sm text-slate-700 font-medium"
                                >
                                    <span>{c.name} ({c.symbol})</span>
                                    {settings.currency === c.code && <Check size={16} className="text-blue-600" />}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>

        {/* Work Configuration Section */}
        <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider ml-1">Work Configuration</h3>
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden divide-y divide-slate-100">
                
                {/* Hourly Wage */}
                <div className="p-4 flex items-center justify-between">
                    <div>
                        <p className="font-semibold text-slate-900">Hourly Wage</p>
                        <p className="text-sm text-slate-500">Your default hourly rate</p>
                    </div>
                    <div className="flex items-center gap-2">
                        <span className="text-slate-500 font-medium">{selectedCurrency.symbol}</span>
                        <input 
                            type="number"
                            className="w-24 px-3 py-2 border border-slate-200 rounded-lg text-right focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                            value={settings.hourlyRate || ''}
                            onChange={(e) => updateSettings({ hourlyRate: parseFloat(e.target.value) || 0 })}
                            placeholder="0.00"
                        />
                    </div>
                </div>

                {/* Commission Percentage */}
                <div className="p-4 flex items-center justify-between">
                    <div>
                        <p className="font-semibold text-slate-900">Commission</p>
                        <p className="text-sm text-slate-500">Default commission percentage</p>
                    </div>
                    <div className="flex items-center gap-2">
                        <input 
                            type="number"
                            className="w-24 px-3 py-2 border border-slate-200 rounded-lg text-right focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                            value={(settings.commissionPercent * 100) || ''}
                            onChange={(e) => updateSettings({ commissionPercent: (parseFloat(e.target.value) || 0) / 100 })}
                            placeholder="1"
                        />
                        <span className="text-slate-500 font-medium">%</span>
                    </div>
                </div>

            </div>
        </div>
        
        <div className="pt-4">
             <button 
                onClick={handleLogout}
                className="w-full bg-white border border-red-100 text-red-600 font-semibold p-4 rounded-2xl hover:bg-red-50 transition-colors flex items-center justify-center gap-2"
             >
                 <LogOut size={20} />
                 Sign Out
             </button>
        </div>
      </div>
    </div>
  );
}
