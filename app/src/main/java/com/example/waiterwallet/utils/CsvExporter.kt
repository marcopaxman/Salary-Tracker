package com.example.waiterwallet.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.waiterwallet.data.DailyEntry
import com.example.waiterwallet.data.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter

object CsvExporter {
    
    suspend fun exportToCSV(
        context: Context,
        entries: List<DailyEntry>,
        jobs: List<Job>,
        month: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            // Create CSV file in cache directory
            val fileName = "waiter_wallet_${month.replace("/", "-")}.csv"
            val file = File(context.cacheDir, fileName)
            
            // Write CSV content
            file.bufferedWriter().use { writer ->
                // Header
                writer.write("Date,Turnover,Cash Tips,Card Tips,Total Tips,Job,Notes\n")
                
                // Data rows
                entries.forEach { entry ->
                    val jobName = jobs.find { it.id == entry.jobId }?.name ?: ""
                    val dateStr = entry.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val turnover = "%.2f".format(entry.turnover)
                    val cashTips = "%.2f".format(entry.tipsCash ?: 0.0)
                    val cardTips = "%.2f".format(entry.tipsCard ?: 0.0)
                    val totalTips = "%.2f".format(entry.totalTips)
                    val notes = entry.notes?.replace("\"", "\"\"") ?: "" // Escape quotes
                    
                    writer.write("$dateStr,$turnover,$cashTips,$cardTips,$totalTips,\"$jobName\",\"$notes\"\n")
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun shareCSV(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Waiter Wallet Earnings Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share CSV"))
    }
}
