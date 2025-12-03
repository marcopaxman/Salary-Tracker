package com.example.waiterwallet.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.waiterwallet.data.DailyEntry
import com.example.waiterwallet.data.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object PdfExporter {
    
    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 20f
    
    suspend fun exportToPDF(
        context: Context,
        entries: List<DailyEntry>,
        jobs: List<Job>,
        month: String,
        hourlyRate: Double = 0.0
    ): File? = withContext(Dispatchers.IO) {
        try {
            val fileName = "waiter_wallet_${month.replace("/", "-")}.pdf"
            val file = File(context.cacheDir, fileName)
            
            val document = PdfDocument()
            var pageNumber = 1
            var currentY = MARGIN + 30f
            
            // Create first page
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            
            // Paints
            val titlePaint = Paint().apply {
                textSize = 24f
                isFakeBoldText = true
                color = android.graphics.Color.BLACK
            }
            
            val headerPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
                color = android.graphics.Color.DKGRAY
            }
            
            val textPaint = Paint().apply {
                textSize = 11f
                color = android.graphics.Color.BLACK
            }
            
            val linePaint = Paint().apply {
                color = android.graphics.Color.LTGRAY
                strokeWidth = 1f
            }
            
            // Title
            canvas.drawText("Waiter Wallet - Monthly Report", MARGIN, currentY, titlePaint)
            currentY += 25f
            canvas.drawText(month, MARGIN, currentY, headerPaint)
            currentY += 40f
            
            // Calculate totals
            val totalTurnover = entries.sumOf { it.turnover }
            val totalTipsCash = entries.sumOf { it.tipsCash ?: 0.0 }
            val totalTipsCard = entries.sumOf { it.tipsCard ?: 0.0 }
            val totalTips = totalTipsCash + totalTipsCard
            val totalHours = entries.sumOf { it.hoursWorked ?: 0.0 }
            val totalHourlyWages = totalHours * hourlyRate
            
            // Summary section
            canvas.drawText("SUMMARY", MARGIN, currentY, headerPaint)
            currentY += LINE_HEIGHT
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += LINE_HEIGHT
            
            canvas.drawText("Total Entries: ${entries.size}", MARGIN, currentY, textPaint)
            currentY += LINE_HEIGHT
            canvas.drawText("Total Turnover: R${"%.2f".format(totalTurnover)}", MARGIN, currentY, textPaint)
            currentY += LINE_HEIGHT
            canvas.drawText("Total Cash Tips: R${"%.2f".format(totalTipsCash)}", MARGIN, currentY, textPaint)
            currentY += LINE_HEIGHT
            canvas.drawText("Total Card Tips: R${"%.2f".format(totalTipsCard)}", MARGIN, currentY, textPaint)
            currentY += LINE_HEIGHT
            canvas.drawText("Total Tips: R${"%.2f".format(totalTips)}", MARGIN, currentY, textPaint)
            currentY += LINE_HEIGHT
            
            if (totalHours > 0 && hourlyRate > 0) {
                canvas.drawText("Total Hours Worked: ${"%.1f".format(totalHours)}", MARGIN, currentY, textPaint)
                currentY += LINE_HEIGHT
                canvas.drawText("Hourly Wages (@ R${"%.2f".format(hourlyRate)}/hr): R${"%.2f".format(totalHourlyWages)}", MARGIN, currentY, textPaint)
                currentY += LINE_HEIGHT
            }
            
            currentY += 30f
            
            // Entries table header
            canvas.drawText("DAILY ENTRIES", MARGIN, currentY, headerPaint)
            currentY += LINE_HEIGHT
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += LINE_HEIGHT
            
            // Table headers
            val col1 = MARGIN
            val col2 = MARGIN + 80f
            val col3 = MARGIN + 160f
            val col4 = MARGIN + 240f
            val col5 = MARGIN + 320f
            val col6 = MARGIN + 400f
            
            canvas.drawText("Date", col1, currentY, headerPaint)
            canvas.drawText("Turnover", col2, currentY, headerPaint)
            canvas.drawText("Cash Tips", col3, currentY, headerPaint)
            canvas.drawText("Card Tips", col4, currentY, headerPaint)
            canvas.drawText("Total Tips", col5, currentY, headerPaint)
            canvas.drawText("Job", col6, currentY, headerPaint)
            currentY += 5f
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += LINE_HEIGHT
            
            // Entries
            for (entry in entries.sortedBy { it.date }) {
                // Check if we need a new page
                if (currentY > PAGE_HEIGHT - MARGIN - 50f) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = MARGIN + 30f
                    
                    // Repeat table headers on new page
                    canvas.drawText("Date", col1, currentY, headerPaint)
                    canvas.drawText("Turnover", col2, currentY, headerPaint)
                    canvas.drawText("Cash Tips", col3, currentY, headerPaint)
                    canvas.drawText("Card Tips", col4, currentY, headerPaint)
                    canvas.drawText("Total Tips", col5, currentY, headerPaint)
                    canvas.drawText("Job", col6, currentY, headerPaint)
                    currentY += 5f
                    canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                    currentY += LINE_HEIGHT
                }
                
                val dateStr = entry.date.format(DateTimeFormatter.ofPattern("dd MMM"))
                val jobName = jobs.find { it.id == entry.jobId }?.name ?: "-"
                
                canvas.drawText(dateStr, col1, currentY, textPaint)
                canvas.drawText("R${"%.2f".format(entry.turnover)}", col2, currentY, textPaint)
                canvas.drawText("R${"%.2f".format(entry.tipsCash ?: 0.0)}", col3, currentY, textPaint)
                canvas.drawText("R${"%.2f".format(entry.tipsCard ?: 0.0)}", col4, currentY, textPaint)
                canvas.drawText("R${"%.2f".format(entry.totalTips)}", col5, currentY, textPaint)
                canvas.drawText(jobName.take(15), col6, currentY, textPaint)
                
                currentY += LINE_HEIGHT
            }
            
            // Finish last page
            document.finishPage(page)
            
            // Write to file
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun sharePDF(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Waiter Wallet Monthly Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share PDF Report"))
    }
}
