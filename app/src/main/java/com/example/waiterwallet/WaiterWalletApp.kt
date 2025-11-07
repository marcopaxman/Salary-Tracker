package com.example.waiterwallet

import android.app.Application
import com.example.waiterwallet.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WaiterWalletApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        // Expose app for simple ViewModel factories
        com.example.waiterwallet.ui.viewmodel.WaiterWalletAppHolder.app = this
    }
}
