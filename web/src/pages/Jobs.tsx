import { useState } from 'react';
import { useJobs } from '../hooks/useJobs';
import type { FirestoreJob } from '../types';
import { Briefcase, Plus, Pencil, Trash2, X, Save, AlertCircle } from 'lucide-react';

export default function Jobs() {
  const { jobs, loading, addJob, updateJob, deleteJob } = useJobs();
  
  const [isEditing, setIsEditing] = useState<string | null>(null);
  const [isAdding, setIsAdding] = useState(false);
  const [editName, setEditName] = useState('');
  const [error, setError] = useState('');

  const handleStartEdit = (job: FirestoreJob) => {
      setIsEditing(job.id);
      setEditName(job.name);
      setError('');
  };

  const handleStartAdd = () => {
      setIsAdding(true);
      setEditName('');
      setError('');
  };

  const handleSave = async (id?: string) => {
      if (!editName.trim()) {
          setError('Job name is required');
          return;
      }

      try {
          if (id) {
              await updateJob(id, { name: editName.trim() });
              setIsEditing(null);
          } else {
              await addJob({ name: editName.trim() });
              setIsAdding(false);
          }
      } catch (e) {
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
           <p className="text-slate-500 mt-1">Manage your workplaces</p>
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
        {isAdding && (
             <div className="bg-white p-6 rounded-2xl shadow-sm border border-blue-200 ring-2 ring-blue-100 flex flex-col justify-between min-h-[140px] animate-in fade-in zoom-in-95 duration-200">
                 <input 
                    type="text" 
                    placeholder="Job Name (e.g. Italian Restaurant)"
                    className="w-full font-bold text-lg text-slate-900 placeholder:text-slate-300 border-b border-slate-200 focus:border-blue-500 outline-none pb-1"
                    autoFocus
                    value={editName}
                    onChange={e => setEditName(e.target.value)}
                 />
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
          <div key={job.id} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between min-h-[140px] hover:shadow-md transition-shadow group relative">
            
            {isEditing === job.id ? (
                 <>
                   <input 
                        type="text" 
                        className="w-full font-bold text-lg text-slate-900 border-b border-slate-200 focus:border-blue-500 outline-none pb-1"
                        value={editName}
                        onChange={e => setEditName(e.target.value)}
                   />
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
                    
                    <h3 className="font-bold text-lg text-slate-900">{job.name}</h3>
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
