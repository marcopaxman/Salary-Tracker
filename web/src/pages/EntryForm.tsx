import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import { useJobs } from '../hooks/useJobs';
import { useEntries, isEntryActive } from '../hooks/useEntries';
import { useSettings, CURRENCIES } from '../hooks/useSettings';
import type { FirestoreDailyEntry } from '../types';
import { ArrowLeft, Save, Calendar, DollarSign, Clock, FileText, Briefcase, Archive, RotateCcw, Trash2 } from 'lucide-react';
import { format } from 'date-fns';
import ConfirmModal from '../components/ConfirmModal';

export default function EntryForm() {
  const { id } = useParams(); // id is the date string (YYYY-MM-DD)
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const { jobs } = useJobs();
  const { settings } = useSettings();
  const { deactivateEntry, reactivateEntry, deleteEntry } = useEntries();
  
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(!!id);
  const [error, setError] = useState('');
  const [isArchived, setIsArchived] = useState(false);
  const [showArchiveModal, setShowArchiveModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // Form State
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [turnover, setTurnover] = useState('');
  const [tipsCash, setTipsCash] = useState('');
  const [tipsCard, setTipsCard] = useState('');
  const [hours, setHours] = useState('');
  const [selectedJob, setSelectedJob] = useState('');
  const [notes, setNotes] = useState('');
  const [createdAt, setCreatedAt] = useState<number>(Date.now());

  useEffect(() => {
    if (id && currentUser) {
      const fetchEntry = async () => {
        try {
          const docRef = doc(db, 'users', currentUser.uid, 'entries', id);
          const docSnap = await getDoc(docRef);
          
          if (docSnap.exists()) {
            const data = docSnap.data() as FirestoreDailyEntry;
            setDate(data.date);
            setTurnover(data.turnover.toString());
            setTipsCash(data.tipsCash?.toString() || '');
            setTipsCard(data.tipsCard?.toString() || '');
            setHours(data.hoursWorked?.toString() || '');
            setSelectedJob(data.jobId || '');
            setNotes(data.notes || '');
            setCreatedAt(data.createdAt);
            setIsArchived(!isEntryActive(data));
          } else {
             setError('Entry not found');
          }
        } catch (err) {
          console.error(err);
          setError('Failed to load entry');
        } finally {
          setInitialLoading(false);
        }
      };
      
      fetchEntry();
    }
  }, [id, currentUser]);

  // If creating new, and jobs load, set default job if only one exists or none selected
  useEffect(() => {
      if (!selectedJob && jobs.length > 0) {
          setSelectedJob(jobs[0].id);
      }
  }, [jobs, selectedJob]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!currentUser) return;

    const entryId = date;

    setLoading(true);
    setError('');

    try {
        if (id && date !== id) {
            const newDocRef = doc(db, 'users', currentUser.uid, 'entries', date);
            const existingSnap = await getDoc(newDocRef);
            if (existingSnap.exists() && isEntryActive(existingSnap.data() as FirestoreDailyEntry)) {
                setError('An entry already exists for this date.');
                setLoading(false);
                return;
            }
        } else if (!id) {
            const docRef = doc(db, 'users', currentUser.uid, 'entries', date);
            const existingSnap = await getDoc(docRef);
            if (existingSnap.exists() && isEntryActive(existingSnap.data() as FirestoreDailyEntry)) {
                setError('An entry already exists for this date.');
                setLoading(false);
                return;
            }
        }

        const entryData: Record<string, unknown> = {
            id: entryId,
            date: date,
            turnover: parseFloat(turnover) || 0,
            tipsCash: parseFloat(tipsCash) || 0,
            tipsCard: parseFloat(tipsCard) || 0,
            hoursWorked: parseFloat(hours) || 0,
            jobId: selectedJob || null,
            notes: notes || '',
            active: true,
            createdAt: id ? createdAt : Date.now(),
            updatedAt: Date.now()
        };

        Object.keys(entryData).forEach(key => entryData[key] === undefined && delete entryData[key]);

        await setDoc(doc(db, 'users', currentUser.uid, 'entries', entryId), entryData, { merge: true });

        if (id && date !== id) {
            await deleteEntry(id);
        }

        navigate('/dashboard/entries');
    } catch (err) {
        console.error(err);
        setError('Failed to save entry');
    } finally {
        setLoading(false);
    }
  }

  async function handleDeactivate() {
    if (!id) return;
    setLoading(true);
    try {
        await deactivateEntry(id);
        navigate('/dashboard/entries');
    } catch (err) {
        console.error(err);
        setError('Failed to deactivate entry');
        setLoading(false);
    }
  }

  async function handlePermanentDelete() {
    if (!id) return;
    setLoading(true);
    try {
        await deleteEntry(id);
        navigate('/dashboard/entries');
    } catch (err) {
        console.error(err);
        setError('Failed to delete entry');
        setLoading(false);
    }
  }

  async function handleReactivate() {
    if (!id) return;
    setLoading(true);
    try {
        await reactivateEntry(id);
        setIsArchived(false);
    } catch (err) {
        console.error(err);
        setError('Failed to reactivate entry');
    } finally {
        setLoading(false);
    }
  }

  if (initialLoading) {
      return (
          <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
      );
  }

  // Helper to safely parse date for display
  const getDisplayDate = (dateStr: string) => {
      try {
          return format(new Date(dateStr), 'MMM d, yyyy');
      } catch (e) {
          return dateStr;
      }
  };

  // Get currency symbol from settings
  const currencySymbol = CURRENCIES.find(c => c.code === settings.currency)?.symbol || '€';

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button 
           onClick={() => navigate('/dashboard/entries')}
           className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500 hover:text-slate-900"
        >
            <ArrowLeft size={24} />
        </button>
        <div>
            <h1 className="text-2xl font-bold text-slate-900">
                {id ? 'Edit Entry' : 'New Entry'}
            </h1>
            <p className="text-slate-500 text-sm">
                {id ? `Editing details for ${getDisplayDate(date)}` : 'Record your earnings for a shift'}
            </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 md:p-8 space-y-8 animate-in slide-in-from-bottom-4 duration-500">
        
        {isArchived && id && (
            <div className="bg-amber-50 text-amber-800 p-4 rounded-xl text-sm font-medium border border-amber-100 flex items-center justify-between gap-4">
                <span>This entry is archived and not included in dashboard calculations.</span>
                <button
                    type="button"
                    onClick={handleReactivate}
                    disabled={loading}
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-100 hover:bg-amber-200 text-amber-900 rounded-lg text-xs font-semibold transition-colors shrink-0 disabled:opacity-50"
                >
                    <RotateCcw size={14} />
                    Reactivate
                </button>
            </div>
        )}

        {error && (
            <div className="bg-red-50 text-red-600 p-4 rounded-xl text-sm font-medium border border-red-100">
                {error}
            </div>
        )}

        {/* Date Section */}
        <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider flex items-center gap-2">
                <Calendar size={16} /> Date & Job
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label className="block text-sm font-medium text-slate-600 mb-1.5">Date</label>
                    <input 
                        type="date"
                        required
                        className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium text-slate-900"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                    />
                </div>
                <div>
                     <label className="block text-sm font-medium text-slate-600 mb-1.5">Job / Role</label>
                     <div className="relative">
                        <Briefcase className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <select
                            className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium text-slate-900 appearance-none"
                            value={selectedJob}
                            onChange={(e) => setSelectedJob(e.target.value)}
                        >
                            <option value="">Select a job...</option>
                            {jobs.map(job => (
                                <option key={job.id} value={job.id}>{job.name}</option>
                            ))}
                        </select>
                     </div>
                </div>
            </div>
        </div>

        <hr className="border-slate-100" />

        {/* Financials Section */}
        <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider flex items-center gap-2">
                <DollarSign size={16} /> Earnings
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                 <div>
                    <label className="block text-sm font-medium text-slate-600 mb-1.5">Total Turnover ({currencySymbol})</label>
                    <input 
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="0.00"
                        className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium text-slate-900"
                        value={turnover}
                        onChange={(e) => setTurnover(e.target.value)}
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-slate-600 mb-1.5">Cash Tips ({currencySymbol})</label>
                    <input 
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="0.00"
                        className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500/20 focus:border-green-500 outline-none transition-all font-medium text-slate-900"
                        value={tipsCash}
                        onChange={(e) => setTipsCash(e.target.value)}
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-slate-600 mb-1.5">Card Tips ({currencySymbol})</label>
                    <input 
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="0.00"
                        className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium text-slate-900"
                        value={tipsCard}
                        onChange={(e) => setTipsCard(e.target.value)}
                    />
                </div>
            </div>
        </div>

        <hr className="border-slate-100" />

        {/* Details Section */}
        <div className="space-y-4">
             <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider flex items-center gap-2">
                <FileText size={16} /> Details
            </h3>
            <div className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-slate-600 mb-1.5 flex items-center gap-2">
                        <Clock size={16} className="text-slate-400" /> Hours Worked
                    </label>
                    <input 
                        type="number"
                        step="0.5"
                        min="0"
                        placeholder="e.g. 5.5"
                        className="w-full md:w-1/3 px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium text-slate-900"
                        value={hours}
                        onChange={(e) => setHours(e.target.value)}
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-slate-600 mb-1.5">Notes</label>
                    <textarea 
                        rows={3}
                        placeholder="Any special notes about this shift..."
                        className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium text-slate-900 resize-none"
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                    />
                </div>
            </div>
        </div>

        <div className="pt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            {id && !isArchived ? (
                <button
                    type="button"
                    onClick={() => setShowArchiveModal(true)}
                    disabled={loading}
                    className="flex items-center justify-center gap-2 w-full sm:w-auto px-4 py-2.5 text-amber-700 font-medium hover:bg-amber-50 border border-amber-200 rounded-xl transition-colors disabled:opacity-50 order-3 sm:order-1"
                >
                    <Archive size={18} />
                    <span>Archive</span>
                </button>
            ) : id && isArchived ? (
                <button
                    type="button"
                    onClick={() => setShowDeleteModal(true)}
                    disabled={loading}
                    className="flex items-center justify-center gap-2 w-full sm:w-auto px-4 py-2.5 text-red-600 font-medium hover:bg-red-50 border border-red-200 rounded-xl transition-colors disabled:opacity-50 order-3 sm:order-1"
                >
                    <Trash2 size={18} />
                    <span>Delete Permanently</span>
                </button>
            ) : (
                <div className="hidden sm:block" />
            )}
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-4 w-full sm:w-auto order-1 sm:order-2">
                <button
                    type="submit"
                    disabled={loading}
                    className="flex items-center justify-center gap-2 w-full sm:w-auto bg-blue-600 hover:bg-blue-700 text-white px-8 py-2.5 rounded-xl font-bold shadow-lg shadow-blue-600/20 transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed order-1"
                >
                    <Save size={20} />
                    <span>{loading ? 'Saving...' : 'Save Entry'}</span>
                </button>
                <button
                    type="button"
                    onClick={() => navigate('/dashboard/entries')}
                    className="w-full sm:w-auto px-6 py-2.5 text-slate-600 font-medium hover:bg-slate-100 rounded-xl transition-colors order-2"
                >
                    Cancel
                </button>
            </div>
        </div>

      </form>

      <ConfirmModal
        open={showArchiveModal}
        title="Archive this entry?"
        message="This entry will be moved to your archived list and excluded from dashboard calculations. You can reactivate it later."
        confirmLabel="Archive Entry"
        variant="warning"
        loading={loading}
        onConfirm={handleDeactivate}
        onCancel={() => setShowArchiveModal(false)}
      />

      <ConfirmModal
        open={showDeleteModal}
        title="Permanently delete this entry?"
        message={`This will permanently remove the entry for ${getDisplayDate(date)}. This action cannot be undone.`}
        confirmLabel="Delete Permanently"
        variant="danger"
        loading={loading}
        onConfirm={handlePermanentDelete}
        onCancel={() => setShowDeleteModal(false)}
      />
    </div>
  );
}
