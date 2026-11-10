package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AppSettings
import com.example.data.local.ViewMode
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerPayment
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.PurchaseRecord
import com.example.data.local.entity.SaleOrder
import com.example.data.local.entity.Supplier
import com.example.data.local.entity.SupplierPayment
import com.example.data.repository.HardwareRepository
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSummary(
    val exportDateFormatted: String,
    val productsCount: Int,
    val salesCount: Int,
    val customersCount: Int,
    val customerPaymentsCount: Int,
    val suppliersCount: Int,
    val supplierPaymentsCount: Int,
    val expensesCount: Int,
    val categoriesCount: Int,
    val purchasesCount: Int,
    val language: String,
    val viewMode: String
)

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val summary: BackupSummary? = null
)

class BackupManager(private val repository: HardwareRepository) {

    suspend fun generateBackupJson(context: Context): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(now))

        root.put("format_version", 2)
        root.put("app_name", "Hardware Manager")
        root.put("export_timestamp", now)
        root.put("export_date_formatted", formattedDate)

        // Settings
        val settingsObj = JSONObject()
        settingsObj.put("language", com.example.ui.localization.LanguageController.currentLanguage.code)
        settingsObj.put("view_mode", AppSettings.viewMode.name)
        root.put("settings", settingsObj)

        // 1. Categories
        val categories = repository.getAllCategoriesSync()
        val categoriesArray = JSONArray()
        for (cat in categories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("nameEn", cat.nameEn)
            obj.put("nameBn", cat.nameBn)
            obj.put("emoji", cat.emoji)
            obj.put("sortOrder", cat.sortOrder)
            obj.put("isVisible", cat.isVisible)
            obj.put("colorHex", cat.colorHex)
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)

        // 2. Products
        val products = repository.getAllProductsSync()
        val productsArray = JSONArray()
        for (p in products) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("nameBn", p.nameBn)
            obj.put("nameEn", p.nameEn)
            obj.put("category", p.category)
            obj.put("description", p.description)
            obj.put("photoKey", p.photoKey)
            obj.put("buyPrice", p.buyPrice)
            obj.put("sellPrice", p.sellPrice)
            obj.put("stockQuantity", p.stockQuantity)
            obj.put("lowStockThreshold", p.lowStockThreshold)
            obj.put("unitBn", p.unitBn)
            obj.put("unitEn", p.unitEn)
            obj.put("isDeleted", p.isDeleted)
            productsArray.put(obj)
        }
        root.put("products", productsArray)

        // 3. Customers
        val customers = repository.getAllCustomersSync()
        val customersArray = JSONArray()
        for (c in customers) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("address", c.address)
            obj.put("note", c.note)
            obj.put("totalBaki", c.totalBaki)
            if (c.dueDate != null) obj.put("dueDate", c.dueDate)
            if (c.lastPaymentDate != null) obj.put("lastPaymentDate", c.lastPaymentDate)
            obj.put("lastTransactionTime", c.lastTransactionTime)
            customersArray.put(obj)
        }
        root.put("customers", customersArray)

        // 4. Customer Payments
        val custPayments = repository.getAllCustomerPaymentsSync()
        val custPaymentsArray = JSONArray()
        for (cp in custPayments) {
            val obj = JSONObject()
            obj.put("id", cp.id)
            obj.put("customerId", cp.customerId)
            obj.put("amount", cp.amount)
            obj.put("note", cp.note)
            obj.put("timestamp", cp.timestamp)
            custPaymentsArray.put(obj)
        }
        root.put("customer_payments", custPaymentsArray)

        // 5. Suppliers
        val suppliers = repository.getAllSuppliersSync()
        val suppliersArray = JSONArray()
        for (s in suppliers) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("company", s.company)
            obj.put("phone", s.phone)
            obj.put("address", s.address)
            obj.put("suppliedCategories", s.suppliedCategories)
            obj.put("note", s.note)
            obj.put("amountOwed", s.amountOwed)
            obj.put("createdAt", s.createdAt)
            suppliersArray.put(obj)
        }
        root.put("suppliers", suppliersArray)

        // 6. Supplier Payments
        val supPayments = repository.getAllSupplierPaymentsSync()
        val supPaymentsArray = JSONArray()
        for (sp in supPayments) {
            val obj = JSONObject()
            obj.put("id", sp.id)
            obj.put("supplierId", sp.supplierId)
            obj.put("amount", sp.amount)
            obj.put("paymentMethod", sp.paymentMethod)
            obj.put("note", sp.note)
            obj.put("timestamp", sp.timestamp)
            supPaymentsArray.put(obj)
        }
        root.put("supplier_payments", supPaymentsArray)

        // 7. Purchases
        val purchases = repository.getAllPurchasesSync()
        val purchasesArray = JSONArray()
        for (pr in purchases) {
            val obj = JSONObject()
            obj.put("id", pr.id)
            obj.put("productId", pr.productId)
            obj.put("productName", pr.productName)
            obj.put("quantity", pr.quantity)
            obj.put("unitBuyingPrice", pr.unitBuyingPrice)
            obj.put("totalCost", pr.totalCost)
            obj.put("timestamp", pr.timestamp)
            obj.put("supplierName", pr.supplierName)
            obj.put("note", pr.note)
            if (pr.supplierId != null) obj.put("supplierId", pr.supplierId)
            obj.put("paidAmount", pr.paidAmount)
            obj.put("dueAmount", pr.dueAmount)
            if (pr.billPhotoUri != null) obj.put("billPhotoUri", pr.billPhotoUri)
            purchasesArray.put(obj)
        }
        root.put("purchases", purchasesArray)

        // 8. Sales
        val sales = repository.getAllSalesSync()
        val salesArray = JSONArray()
        for (so in sales) {
            val obj = JSONObject()
            obj.put("id", so.id)
            obj.put("invoiceNumber", so.invoiceNumber)
            obj.put("timestamp", so.timestamp)
            if (so.customerId != null) obj.put("customerId", so.customerId)
            obj.put("customerName", so.customerName)
            obj.put("customerPhone", so.customerPhone)
            obj.put("totalAmount", so.totalAmount)
            obj.put("paidAmount", so.paidAmount)
            obj.put("dueAmount", so.dueAmount)
            obj.put("totalCost", so.totalCost)
            obj.put("totalItemsCount", so.totalItemsCount)
            obj.put("summaryItems", so.summaryItems)
            if (so.dueDate != null) obj.put("dueDate", so.dueDate)
            obj.put("isCredit", so.isCredit)
            salesArray.put(obj)
        }
        root.put("sales", salesArray)

        // 9. Expenses
        val expenses = repository.getAllExpensesSync()
        val expensesArray = JSONArray()
        for (ex in expenses) {
            val obj = JSONObject()
            obj.put("id", ex.id)
            obj.put("title", ex.title)
            obj.put("amount", ex.amount)
            obj.put("category", ex.category)
            obj.put("timestamp", ex.timestamp)
            obj.put("note", ex.note)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        root.toString(2)
    }

    suspend fun inspectBackupJson(jsonString: String): BackupSummary = withContext(Dispatchers.Default) {
        val root = JSONObject(jsonString)
        val formattedDate = root.optString("export_date_formatted", "Unknown Date")
        val settingsObj = root.optJSONObject("settings")
        val lang = settingsObj?.optString("language", "bn") ?: "bn"
        val mode = settingsObj?.optString("view_mode", "DETAILED") ?: "DETAILED"

        val productsCount = root.optJSONArray("products")?.length() ?: 0
        val salesCount = root.optJSONArray("sales")?.length() ?: 0
        val customersCount = root.optJSONArray("customers")?.length() ?: 0
        val customerPaymentsCount = root.optJSONArray("customer_payments")?.length() ?: 0
        val suppliersCount = root.optJSONArray("suppliers")?.length() ?: 0
        val supplierPaymentsCount = root.optJSONArray("supplier_payments")?.length() ?: 0
        val expensesCount = root.optJSONArray("expenses")?.length() ?: 0
        val categoriesCount = root.optJSONArray("categories")?.length() ?: 0
        val purchasesCount = root.optJSONArray("purchases")?.length() ?: 0

        BackupSummary(
            exportDateFormatted = formattedDate,
            productsCount = productsCount,
            salesCount = salesCount,
            customersCount = customersCount,
            customerPaymentsCount = customerPaymentsCount,
            suppliersCount = suppliersCount,
            supplierPaymentsCount = supplierPaymentsCount,
            expensesCount = expensesCount,
            categoriesCount = categoriesCount,
            purchasesCount = purchasesCount,
            language = lang,
            viewMode = mode
        )
    }

    suspend fun restoreFromJson(jsonString: String, context: Context): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val summary = inspectBackupJson(jsonString)

            // Clear all existing data atomically
            repository.clearAllData()

            // 1. Restore Categories
            val categoriesArray = root.optJSONArray("categories")
            if (categoriesArray != null && categoriesArray.length() > 0) {
                val list = mutableListOf<Category>()
                for (i in 0 until categoriesArray.length()) {
                    val obj = categoriesArray.getJSONObject(i)
                    list.add(
                        Category(
                            id = obj.optLong("id", 0L),
                            nameEn = obj.getString("nameEn"),
                            nameBn = obj.optString("nameBn", obj.getString("nameEn")),
                            emoji = obj.optString("emoji", "📦"),
                            sortOrder = obj.optInt("sortOrder", i + 1),
                            isVisible = obj.optBoolean("isVisible", true),
                            colorHex = obj.optString("colorHex", "#10B981")
                        )
                    )
                }
                repository.insertCategories(list)
            }

            // 2. Restore Products
            val productsArray = root.optJSONArray("products")
            if (productsArray != null && productsArray.length() > 0) {
                val list = mutableListOf<Product>()
                for (i in 0 until productsArray.length()) {
                    val obj = productsArray.getJSONObject(i)
                    list.add(
                        Product(
                            id = obj.optLong("id", 0L),
                            nameBn = obj.optString("nameBn", ""),
                            nameEn = obj.optString("nameEn", ""),
                            category = obj.optString("category", "Other"),
                            description = obj.optString("description", ""),
                            photoKey = obj.optString("photoKey", ""),
                            buyPrice = obj.optDouble("buyPrice", 0.0),
                            sellPrice = obj.optDouble("sellPrice", 0.0),
                            stockQuantity = obj.optInt("stockQuantity", 0),
                            lowStockThreshold = obj.optInt("lowStockThreshold", 5),
                            unitBn = obj.optString("unitBn", "টি"),
                            unitEn = obj.optString("unitEn", "pcs"),
                            isDeleted = obj.optBoolean("isDeleted", false)
                        )
                    )
                }
                repository.insertProducts(list)
            }

            // 3. Restore Customers
            val customersArray = root.optJSONArray("customers")
            if (customersArray != null && customersArray.length() > 0) {
                val list = mutableListOf<Customer>()
                for (i in 0 until customersArray.length()) {
                    val obj = customersArray.getJSONObject(i)
                    list.add(
                        Customer(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            address = obj.optString("address", ""),
                            note = obj.optString("note", ""),
                            totalBaki = obj.optDouble("totalBaki", 0.0),
                            dueDate = if (obj.has("dueDate")) obj.getLong("dueDate") else null,
                            lastPaymentDate = if (obj.has("lastPaymentDate")) obj.getLong("lastPaymentDate") else null,
                            lastTransactionTime = obj.optLong("lastTransactionTime", System.currentTimeMillis())
                        )
                    )
                }
                repository.insertCustomers(list)
            }

            // 4. Restore Customer Payments
            val custPaymentsArray = root.optJSONArray("customer_payments")
            if (custPaymentsArray != null && custPaymentsArray.length() > 0) {
                val list = mutableListOf<CustomerPayment>()
                for (i in 0 until custPaymentsArray.length()) {
                    val obj = custPaymentsArray.getJSONObject(i)
                    list.add(
                        CustomerPayment(
                            id = obj.optLong("id", 0L),
                            customerId = obj.getLong("customerId"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                repository.insertCustomerPayments(list)
            }

            // 5. Restore Suppliers
            val suppliersArray = root.optJSONArray("suppliers")
            if (suppliersArray != null && suppliersArray.length() > 0) {
                val list = mutableListOf<Supplier>()
                for (i in 0 until suppliersArray.length()) {
                    val obj = suppliersArray.getJSONObject(i)
                    list.add(
                        Supplier(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            company = obj.optString("company", ""),
                            phone = obj.optString("phone", ""),
                            address = obj.optString("address", ""),
                            suppliedCategories = obj.optString("suppliedCategories", ""),
                            note = obj.optString("note", ""),
                            amountOwed = obj.optDouble("amountOwed", 0.0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                repository.insertSuppliers(list)
            }

            // 6. Restore Supplier Payments
            val supPaymentsArray = root.optJSONArray("supplier_payments")
            if (supPaymentsArray != null && supPaymentsArray.length() > 0) {
                val list = mutableListOf<SupplierPayment>()
                for (i in 0 until supPaymentsArray.length()) {
                    val obj = supPaymentsArray.getJSONObject(i)
                    list.add(
                        SupplierPayment(
                            id = obj.optLong("id", 0L),
                            supplierId = obj.getLong("supplierId"),
                            amount = obj.getDouble("amount"),
                            paymentMethod = obj.optString("paymentMethod", "Cash"),
                            note = obj.optString("note", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                repository.insertSupplierPayments(list)
            }

            // 7. Restore Purchases
            val purchasesArray = root.optJSONArray("purchases")
            if (purchasesArray != null && purchasesArray.length() > 0) {
                val list = mutableListOf<PurchaseRecord>()
                for (i in 0 until purchasesArray.length()) {
                    val obj = purchasesArray.getJSONObject(i)
                    list.add(
                        PurchaseRecord(
                            id = obj.optLong("id", 0L),
                            productId = obj.getLong("productId"),
                            productName = obj.optString("productName", ""),
                            quantity = obj.optInt("quantity", 0),
                            unitBuyingPrice = obj.optDouble("unitBuyingPrice", 0.0),
                            totalCost = obj.optDouble("totalCost", 0.0),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            supplierName = obj.optString("supplierName", ""),
                            note = obj.optString("note", ""),
                            supplierId = if (obj.has("supplierId")) obj.getLong("supplierId") else null,
                            paidAmount = obj.optDouble("paidAmount", 0.0),
                            dueAmount = obj.optDouble("dueAmount", 0.0),
                            billPhotoUri = if (obj.has("billPhotoUri")) obj.getString("billPhotoUri") else null
                        )
                    )
                }
                repository.insertPurchases(list)
            }

            // 8. Restore Sales
            val salesArray = root.optJSONArray("sales")
            if (salesArray != null && salesArray.length() > 0) {
                val list = mutableListOf<SaleOrder>()
                for (i in 0 until salesArray.length()) {
                    val obj = salesArray.getJSONObject(i)
                    list.add(
                        SaleOrder(
                            id = obj.optLong("id", 0L),
                            invoiceNumber = obj.getString("invoiceNumber"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            customerId = if (obj.has("customerId")) obj.getLong("customerId") else null,
                            customerName = obj.optString("customerName", "Walk-in"),
                            customerPhone = obj.optString("customerPhone", ""),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            paidAmount = obj.optDouble("paidAmount", 0.0),
                            dueAmount = obj.optDouble("dueAmount", 0.0),
                            totalCost = obj.optDouble("totalCost", 0.0),
                            totalItemsCount = obj.optInt("totalItemsCount", 0),
                            summaryItems = obj.optString("summaryItems", ""),
                            dueDate = if (obj.has("dueDate")) obj.getLong("dueDate") else null,
                            isCredit = obj.optBoolean("isCredit", false)
                        )
                    )
                }
                repository.insertSales(list)
            }

            // 9. Restore Expenses
            val expensesArray = root.optJSONArray("expenses")
            if (expensesArray != null && expensesArray.length() > 0) {
                val list = mutableListOf<Expense>()
                for (i in 0 until expensesArray.length()) {
                    val obj = expensesArray.getJSONObject(i)
                    list.add(
                        Expense(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            category = obj.optString("category", "Shop Maintenance"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                }
                repository.insertExpenses(list)
            }

            // Restore Settings
            if (summary.language == "en") {
                AppSettings.setLanguage(context, AppLanguage.ENGLISH)
            } else {
                AppSettings.setLanguage(context, AppLanguage.BANGLA)
            }

            val restoredMode = try {
                ViewMode.valueOf(summary.viewMode)
            } catch (e: Exception) {
                ViewMode.DETAILED
            }
            AppSettings.setViewMode(restoredMode, context)

            RestoreResult(
                success = true,
                message = "রিস্টোর সফল হয়েছে! (Restore successful)",
                summary = summary
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                message = "রিস্টোর ব্যর্থ হয়েছে: ${e.localizedMessage ?: "Unknown error"}",
                summary = null
            )
        }
    }

    suspend fun saveBackupToLocalFile(context: Context, jsonString: String): File = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) {
            backupsDir.mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(backupsDir, "HardwareManager_Backup_$timestamp.json")
        file.writeText(jsonString)
        file
    }

    suspend fun createGoogleDriveShareIntent(context: Context, jsonString: String): Intent = withContext(Dispatchers.IO) {
        val cacheBackupsDir = File(context.cacheDir, "backups")
        if (!cacheBackupsDir.exists()) {
            cacheBackupsDir.mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(cacheBackupsDir, "HardwareManager_Backup_$timestamp.json")
        file.writeText(jsonString)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Hardware Manager Store Backup ($timestamp)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Hardware Manager Store Backup file. Save this to Google Drive or safe cloud storage."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(intent, "Back up to Google Drive / Share")
    }

    suspend fun listLocalBackups(context: Context): List<File> = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (backupsDir.exists()) {
            backupsDir.listFiles { f -> f.name.endsWith(".json") }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
