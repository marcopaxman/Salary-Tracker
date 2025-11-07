package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.data.Job
import com.example.waiterwallet.data.JobDao
import kotlinx.coroutines.launch

class JobsViewModel(private val jobDao: JobDao) : ViewModel() {
    
    val allJobs = jobDao.observeJobs()
    
    fun addJob(name: String) {
        viewModelScope.launch {
            jobDao.upsert(Job(name = name))
        }
    }
    
    fun updateJob(job: Job) {
        viewModelScope.launch {
            jobDao.upsert(job)
        }
    }
    
    fun deleteJob(job: Job) {
        viewModelScope.launch {
            jobDao.delete(job)
        }
    }
    
    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            @Suppress("UNCHECKED_CAST")
            return JobsViewModel(app.database.jobDao()) as T
        }
    }
}
