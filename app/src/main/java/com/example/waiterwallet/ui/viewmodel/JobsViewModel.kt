package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.data.Job
import com.example.waiterwallet.data.UnifiedRepository
import com.example.waiterwallet.data.UnifiedRepositoryFactory
import kotlinx.coroutines.launch

class JobsViewModel(private val unifiedRepo: UnifiedRepository) : ViewModel() {
    
    val allJobs = unifiedRepo.observeJobs()
    
    fun addJob(name: String) {
        viewModelScope.launch {
            unifiedRepo.upsertJob(Job(name = name))
        }
    }
    
    fun updateJob(job: Job) {
        viewModelScope.launch {
            unifiedRepo.upsertJob(job)
        }
    }
    
    fun deleteJob(job: Job) {
        viewModelScope.launch {
            unifiedRepo.deleteJob(job)
        }
    }
    
    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val unifiedRepo = UnifiedRepositoryFactory.getInstance(app)
            @Suppress("UNCHECKED_CAST")
            return JobsViewModel(unifiedRepo) as T
        }
    }
}
