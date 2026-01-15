import { useState } from 'react';
import { useJobs, type FirestoreJob } from '../hooks/useJobs';
import { Briefcase, Plus, Pencil, Trash2, X, Save, AlertCircle } from 'lucide-react';
import { formatCurrency, useSettings } from '../hooks/useSettings';

export default function Jobs() {
  const { jobs, loading, addJob, updateJob, deleteJob } = useJobs();
  const { settings } = useSettings();
  
  const [isEditing, setIsEditing] = useState<string | null>(null);
  const [isAdding, setIsAdding] = useState(false);
  const [editForm, setEditForm] = useState({ name: '', hourlyRate: '' });
  const [error, setError] = useState('');

  const handleStartEdit = (job: FirestoreJob) => {
      setIsEditing(job.id);
      setEditForm({ name: job.name, hourlyRate: job.hourlyRate.toString() });
      setError('');
  };

  const handleStartAdd = () => {
      setIsAdding(true);
      setEditForm({ name: '', hourlyRate: '' });
      setError('');
  };

  const handleSave = async (id?: string) => {
      if (!editForm.name) {
          setError('Job name is required');
          return;
      }
      
      const rate = parseFloat(editForm.hourlyRate);
      if (isNaN(rate)) {
          setError('Invalid hourly rate');
          return;
      }

      try {
          if (id) {
              await updateJob(id, { name: editForm.name, hourlyRate: rate });
              setIsEditing(null);
          } else {
              await addJob({ name: editForm.name, hourlyRate: rate });
              setIsAdding(false);
          }
      } catch (e: any) {
          console.error(e);
          setError('Failed to save job');
      }
  };

  const handleDelete = async (id: string) => {
      if (window.confirm('Are you sure you want to delete this job?')) {
          try {
              await deleteJob(id);
          } catch (e) {
              console.error(e);
              alert('Failed to delete job');
          }
      }
  };

  if (loading) {
     return <div className="p-8 text-center text-slate-400">Loading jobs...</div>;
  }

  return (
    <div className="space-y-8 pb-24 md:pb-0">
      <div className="flex justify-between items-center">
        <div>
           <h2 className="text-3xl font-bold tracking-tight text-slate-900">Jobs</h2>
           <p className="text-slate-500 mt-1">Manage your workplaces and rates</p>
        </div>
        <button 
            onClick={handleStartAdd}
            disabled={isAdding}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
            <Plus size={20} />
            <span className="hidden sm:inline">Add Job</span>
        </button>
      </div>

      {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg flex items-center gap-2 text-sm">
              <AlertCircle size={16} /> {error}
          </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Add New Job Card */}
        {isAdding && (
             <div className="bg-white p-6 rounded-2xl shadow-sm border border-blue-200 ring-2 ring-blue-100 flex flex-col justify-between h-48 animate-in fade-in zoom-in-95 duration-200">
                 <div className="space-y-3">
                     <input 
                        type="text" 
                        placeholder="Job Name (e.g. Italian Restaurant)"
                        className="w-full font-bold text-lg text-slate-900 placeholder:text-slate-300 border-b border-slate-200 focus:border-blue-500 outline-none pb-1"
                        autoFocus
                        value={editForm.name}
                        onChange={e => setEditForm(prev => ({...prev, name: e.target.value}))}
                     />
                     <div className="flex items-center gap-2">
                         <span className="text-slate-400 text-sm">Rate: {settings.currency === 'EUR' ? '€' : settings.currency === 'USD' ? '$' : 'R'}</span>
                         <input 
                            type="number" 
                            placeholder="0.00"
                            className="w-24 text-slate-600 font-medium border-b border-slate-200 focus:border-blue-500 outline-none pb-1"
                            value={editForm.hourlyRate}
                            onChange={e => setEditForm(prev => ({...prev, hourlyRate: e.target.value}))}
                         />
                         <span className="text-slate-400 text-sm">/hr</span>
                     </div>
                 </div>
                 <div className="flex justify-end gap-2 mt-4">
                     <button onClick={() => setIsAdding(false)} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors">
                         <X size={20} />
                     </button>
                     <button onClick={() => handleSave()} className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                         <Save size={20} />
                     </button>
                 </div>
             </div>
        )}

        {jobs.map((job) => (
          <div key={job.id} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between h-48 hover:shadow-md transition-shadow group relative">
            
            {isEditing === job.id ? (
                // Edit Mode
                 <>
                   <div className="space-y-3">
                        <input 
                            type="text" 
                            className="w-full font-bold text-lg text-slate-900 border-b border-slate-200 focus:border-blue-500 outline-none pb-1"
                            value={editForm.name}
                            onChange={e => setEditForm(prev => ({...prev, name: e.target.value}))}
                        />
                         <div className="flex items-center gap-2">
                             <span className="text-slate-400 text-sm">Rate:</span>
                             <input 
                                type="number" 
                                className="w-24 text-slate-600 font-medium border-b border-slate-200 focus:border-blue-500 outline-none pb-1"
                                value={editForm.hourlyRate}
                                onChange={e => setEditForm(prev => ({...prev, hourlyRate: e.target.value}))}
                             />
                             <span className="text-slate-400 text-sm">/hr</span>
                         </div>
                   </div>
                   <div className="flex justify-end gap-2 mt-4">
                        <button onClick={() => setIsEditing(null)} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors">
                            <X size={20} />
                        </button>
                        <button onClick={() => handleSave(job.id)} className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors">
                            <Save size={20} />
                        </button>
                   </div>
                 </>
            ) : (
                // View Mode
                <>
                    <div className="flex justify-between items-start">
                        <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
                            <Briefcase size={24} />
                        </div>
                        <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                             <button onClick={() => handleStartEdit(job)} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all">
                                 <Pencil size={16} />
                             </button>
                             <button onClick={() => handleDelete(job.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all">
                                 <Trash2 size={16} />
                             </button>
                        </div>
                    </div>
                    
                    <div>
                        <h3 className="font-bold text-lg text-slate-900">{job.name}</h3>
                        <p className="text-slate-500 font-medium mt-1">
                            {formatCurrency(job.hourlyRate, settings.currency)} / hr
                        </p>
                    </div>
                </>
            )}
          </div>
        ))}

        {jobs.length === 0 && !isAdding && (
            <div className="col-span-full py-12 text-center bg-slate-50 rounded-2xl border border-dashed border-slate-200">
                <Briefcase className="mx-auto text-slate-300 mb-3" size={48} />
                <p className="text-slate-500 font-medium">No jobs added yet</p>
                <p className="text-sm text-slate-400">Click "Add Job" to get started</p>
            </div>
        )}
      </div>
    </div>
  );
}
