package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.data.local.entity.Supplier
import com.example.ui.localization.AppLanguage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateMonthlyPdfReport(
        context: Context,
        monthName: String,
        sales: List<SaleOrder>,
        products: List<Product>,
        customers: List<Customer>,
        suppliers: List<Supplier>,
        language: AppLanguage
    ): File? {
        return try {
            val isBangla = language == AppLanguage.BANGLA
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val sanitizedMonth = monthName.replace(" ", "_")
            val pdfFile = File(reportsDir, "Dokan_Report_${sanitizedMonth}.pdf")

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Dark Slate
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(5, 150, 105) // Vivid Emerald
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 11f
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                strokeWidth = 1f
            }

            // Header
            var yPos = 50f
            canvas.drawText("Dokan - Retail POS & Business Report", 40f, yPos, titlePaint)
            yPos += 24f
            canvas.drawText(if (isBangla) "মাসিক সারসংক্ষেপ: $monthName" else "Monthly Summary: $monthName", 40f, yPos, subtitlePaint)
            yPos += 18f

            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Generated: $dateStr", 40f, yPos, bodyPaint)
            yPos += 20f

            canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
            yPos += 25f

            // Key Statistics
            val totalSalesAmount = sales.sumOf { it.totalAmount }
            val totalPaidAmount = sales.sumOf { it.paidAmount }
            val totalDueAmount = sales.sumOf { it.dueAmount }

            canvas.drawText("Total Sales: $totalSalesAmount BDT", 40f, yPos, boldPaint)
            yPos += 18f
            canvas.drawText("Total Paid: $totalPaidAmount BDT", 40f, yPos, bodyPaint)
            yPos += 18f
            canvas.drawText("Total Due / Baki: $totalDueAmount BDT", 40f, yPos, bodyPaint)
            yPos += 18f
            canvas.drawText("Total Orders: ${sales.size}", 40f, yPos, bodyPaint)
            yPos += 18f
            canvas.drawText("Active Products: ${products.size} | Customers: ${customers.size} | Suppliers: ${suppliers.size}", 40f, yPos, bodyPaint)
            yPos += 25f

            canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
            yPos += 25f

            // Table of Recent Orders
            canvas.drawText("Order Transactions (${sales.take(15).size} of ${sales.size}):", 40f, yPos, boldPaint)
            yPos += 20f

            canvas.drawText("Invoice", 40f, yPos, boldPaint)
            canvas.drawText("Customer", 130f, yPos, boldPaint)
            canvas.drawText("Total", 320f, yPos, boldPaint)
            canvas.drawText("Paid", 410f, yPos, boldPaint)
            canvas.drawText("Due", 490f, yPos, boldPaint)
            yPos += 8f
            canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
            yPos += 16f

            for (order in sales.take(18)) {
                if (yPos > 790f) break
                val invoiceText = order.invoiceNumber.takeLast(10)
                val customerText = order.customerName.ifBlank { "Walk-in" }.take(18)
                val totalText = "${order.totalAmount.toInt()}"
                val paidText = "${order.paidAmount.toInt()}"
                val dueText = "${order.dueAmount.toInt()}"

                canvas.drawText(invoiceText, 40f, yPos, bodyPaint)
                canvas.drawText(customerText, 130f, yPos, bodyPaint)
                canvas.drawText(totalText, 320f, yPos, bodyPaint)
                canvas.drawText(paidText, 410f, yPos, bodyPaint)
                canvas.drawText(dueText, 490f, yPos, bodyPaint)
                yPos += 18f
            }

            document.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateMonthlyCsvReport(
        context: Context,
        monthName: String,
        sales: List<SaleOrder>,
        language: AppLanguage
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val sanitizedMonth = monthName.replace(" ", "_")
            val csvFile = File(reportsDir, "Dokan_Report_${sanitizedMonth}.csv")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            csvFile.bufferedWriter().use { writer ->
                writer.write("Invoice No,Customer Name,Customer Phone,Total (BDT),Discount (BDT),Paid (BDT),Due (BDT),Payment Type,Date,Status\n")
                for (sale in sales) {
                    val dateFormatted = dateFormat.format(Date(sale.timestamp))
                    val paymentType = if (sale.isCredit) "Baki/Credit" else "Cash"
                    val statusText = if (sale.dueAmount > 0) "Due" else "Paid in Full"
                    val line = listOf(
                        "\"${sale.invoiceNumber}\"",
                        "\"${sale.customerName.replace("\"", "\"\"")}\"",
                        "\"${sale.customerPhone}\"",
                        sale.totalAmount.toString(),
                        sale.discount.toString(),
                        sale.paidAmount.toString(),
                        sale.dueAmount.toString(),
                        "\"$paymentType\"",
                        "\"$dateFormatted\"",
                        "\"$statusText\""
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }
            csvFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareReportFile(
        context: Context,
        file: File,
        mimeType: String,
        title: String
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Here is the business report from Dokan Store.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Report via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
