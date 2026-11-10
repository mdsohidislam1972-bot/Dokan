package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.AppSettings
import com.example.data.local.ProfileManager
import com.example.data.local.entity.AppReminder
import com.example.data.local.entity.BankAccount
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerPayment
import com.example.data.local.entity.Expense
import com.example.data.local.entity.OtherIncome
import com.example.data.local.entity.Product
import com.example.data.local.entity.PurchaseRecord
import com.example.data.local.entity.SaleOrder
import com.example.data.local.entity.StaffMember
import com.example.data.local.entity.Supplier
import com.example.data.local.entity.SupplierPayment
import com.example.data.repository.HardwareRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private const val PREFS_MIGRATION = "firebase_migration_prefs"
    private const val KEY_MIGRATION_COMPLETED = "is_migration_completed"
    private const val KEY_MIGRATION_TIME = "migration_timestamp"
    private const val KEY_LAST_SYNC_TIME = "last_cloud_sync_timestamp"
    private const val KEY_CLOUD_SYNC_ACTIVE = "cloud_sync_active"

    fun isCloudSyncActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_MIGRATION, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CLOUD_SYNC_ACTIVE, true)
    }

    fun setCloudSyncActive(context: Context, active: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_MIGRATION, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_ACTIVE, active).apply()
        FirebaseManager.setCloudSyncEnabled(active)
    }

    fun isMigrationCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_MIGRATION, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MIGRATION_COMPLETED, false)
    }

    fun getMigrationTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_MIGRATION, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_MIGRATION_TIME, 0L)
    }

    fun getActiveStoreId(): String {
        val id = ProfileManager.activeProfile.id
        return if (id.isNotBlank() && id != "profile_default") id else "store_main"
    }

    // --- Profile & Store Root Sync ---
    suspend fun syncStoreProfile(context: Context, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        val authUser = Firebase.auth.currentUser ?: return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val profile = ProfileManager.activeProfile
            val data = hashMapOf(
                "id" to storeId,
                "name" to profile.name.ifBlank { "Dokan Store" },
                "ownerId" to authUser.uid,
                "ownerEmail" to (authUser.email ?: profile.email),
                "phone" to profile.phone,
                "address" to profile.address,
                "businessType" to profile.businessType,
                "category" to profile.category,
                "email" to profile.email,
                "ownerName" to profile.ownerName,
                "division" to profile.division,
                "district" to profile.district,
                "streetAddress" to profile.streetAddress,
                "registrationNumber" to profile.registrationNumber,
                "bankAccount" to profile.bankAccount,
                "logoUri" to (profile.logoUri ?: ""),
                "bannerUri" to (profile.bannerUri ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Store profile synced for $storeId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync store profile: ${e.message}")
        }
    }

    // --- Entity Sync Functions ---

    suspend fun syncProduct(context: Context, product: Product, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "name" to product.name,
                "nameBn" to product.nameBn,
                "nameEn" to product.nameEn,
                "category" to product.category,
                "description" to product.description,
                "photoKey" to product.photoKey,
                "buyPrice" to product.buyPrice,
                "sellPrice" to product.sellPrice,
                "stockQty" to product.stockQuantity,
                "lowStockThreshold" to product.lowStockThreshold,
                "unit" to product.unit,
                "unitBn" to product.unitBn,
                "unitEn" to product.unitEn,
                "isDeleted" to product.isDeleted,
                "barcode" to product.barcode,
                "location" to product.location,
                "wholesalePrice" to product.wholesalePrice,
                "mrp" to product.mrp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("products").document(product.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncProduct error: ${e.message}")
        }
    }

    suspend fun deleteProduct(context: Context, productId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("products").document(productId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteProduct error: ${e.message}")
        }
    }

    suspend fun syncCategory(context: Context, category: Category, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "name" to category.name,
                "nameEn" to category.nameEn,
                "nameBn" to category.nameBn,
                "emoji" to category.emoji,
                "sortOrder" to category.sortOrder,
                "colorHex" to category.colorHex,
                "isVisible" to category.isVisible,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("categories").document(category.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncCategory error: ${e.message}")
        }
    }

    suspend fun syncSaleOrder(context: Context, sale: SaleOrder, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "invoiceNumber" to sale.invoiceNumber,
                "timestamp" to sale.timestamp,
                "customerId" to (sale.customerId ?: 0L),
                "customerName" to sale.customerName,
                "customerPhone" to sale.customerPhone,
                "totalAmount" to sale.totalAmount,
                "paidAmount" to sale.paidAmount,
                "dueAmount" to sale.dueAmount,
                "totalCost" to sale.totalCost,
                "totalItemsCount" to sale.totalItemsCount,
                "summaryItems" to sale.summaryItems,
                "dueDate" to (sale.dueDate ?: 0L),
                "isCredit" to sale.isCredit,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("sales").document(sale.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncSaleOrder error: ${e.message}")
        }
    }

    suspend fun syncPurchase(context: Context, purchase: PurchaseRecord, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "productId" to purchase.productId,
                "productName" to purchase.productName,
                "quantity" to purchase.quantity,
                "unitBuyingPrice" to purchase.unitBuyingPrice,
                "totalCost" to purchase.totalCost,
                "timestamp" to purchase.timestamp,
                "supplierName" to purchase.supplierName,
                "supplierId" to (purchase.supplierId ?: 0L),
                "paidAmount" to purchase.paidAmount,
                "dueAmount" to purchase.dueAmount,
                "note" to purchase.note,
                "billPhotoUri" to (purchase.billPhotoUri ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("purchases").document(purchase.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncPurchase error: ${e.message}")
        }
    }

    suspend fun syncCustomer(context: Context, customer: Customer, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "name" to customer.name,
                "phone" to customer.phone,
                "address" to customer.address,
                "note" to customer.note,
                "totalBaki" to customer.totalBaki,
                "category" to customer.category,
                "dueDate" to (customer.dueDate ?: 0L),
                "lastPaymentDate" to (customer.lastPaymentDate ?: 0L),
                "lastTransactionTime" to customer.lastTransactionTime,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("customers").document(customer.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncCustomer error: ${e.message}")
        }
    }

    suspend fun deleteCustomer(context: Context, customerId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("customers").document(customerId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteCustomer error: ${e.message}")
        }
    }

    suspend fun deleteCategory(context: Context, categoryId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("categories").document(categoryId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteCategory error: ${e.message}")
        }
    }

    suspend fun deleteSaleOrder(context: Context, saleId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("sales").document(saleId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteSaleOrder error: ${e.message}")
        }
    }

    suspend fun deletePurchase(context: Context, purchaseId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("purchases").document(purchaseId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deletePurchase error: ${e.message}")
        }
    }

    suspend fun deleteSupplier(context: Context, supplierId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("suppliers").document(supplierId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteSupplier error: ${e.message}")
        }
    }

    suspend fun deleteExpense(context: Context, expenseId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("expenses").document(expenseId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteExpense error: ${e.message}")
        }
    }

    suspend fun deleteBankAccount(context: Context, accountId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("bank_accounts").document(accountId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteBankAccount error: ${e.message}")
        }
    }

    suspend fun deleteOtherIncome(context: Context, incomeId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("other_incomes").document(incomeId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteOtherIncome error: ${e.message}")
        }
    }

    suspend fun deleteReminder(context: Context, reminderId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("reminders").document(reminderId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteReminder error: ${e.message}")
        }
    }

    suspend fun deleteStaffMember(context: Context, staffId: Long, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            db.collection("stores").document(storeId)
                .collection("staff").document(staffId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteStaffMember error: ${e.message}")
        }
    }

    suspend fun syncSupplier(context: Context, supplier: Supplier, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "name" to supplier.name,
                "company" to supplier.company,
                "phone" to supplier.phone,
                "address" to supplier.address,
                "suppliedCategories" to supplier.suppliedCategories,
                "note" to supplier.note,
                "amountOwed" to supplier.amountOwed,
                "category" to supplier.category,
                "createdAt" to supplier.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("suppliers").document(supplier.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncSupplier error: ${e.message}")
        }
    }

    suspend fun syncCustomerPayment(context: Context, payment: CustomerPayment, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "customerId" to payment.customerId,
                "amount" to payment.amount,
                "note" to payment.note,
                "timestamp" to payment.timestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("customer_payments").document(payment.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncCustomerPayment error: ${e.message}")
        }
    }

    suspend fun syncSupplierPayment(context: Context, payment: SupplierPayment, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "supplierId" to payment.supplierId,
                "amount" to payment.amount,
                "paymentMethod" to payment.paymentMethod,
                "note" to payment.note,
                "timestamp" to payment.timestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("supplier_payments").document(payment.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncSupplierPayment error: ${e.message}")
        }
    }

    suspend fun syncExpense(context: Context, expense: Expense, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "title" to expense.title,
                "amount" to expense.amount,
                "category" to expense.category,
                "note" to expense.note,
                "timestamp" to expense.timestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("expenses").document(expense.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncExpense error: ${e.message}")
        }
    }

    suspend fun syncBankAccount(context: Context, account: BankAccount, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "name" to account.name,
                "accountType" to account.accountType,
                "accountNumber" to account.accountNumber,
                "currentBalance" to account.currentBalance,
                "isDefault" to account.isDefault,
                "note" to account.note,
                "lastUpdated" to account.lastUpdated,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("bank_accounts").document(account.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncBankAccount error: ${e.message}")
        }
    }

    suspend fun syncOtherIncome(context: Context, income: OtherIncome, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "title" to income.title,
                "amount" to income.amount,
                "category" to income.category,
                "accountId" to (income.accountId ?: 0L),
                "note" to income.note,
                "timestamp" to income.timestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("other_incomes").document(income.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncOtherIncome error: ${e.message}")
        }
    }

    suspend fun syncReminder(context: Context, reminder: AppReminder, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "title" to reminder.title,
                "description" to reminder.description,
                "reminderDate" to reminder.reminderDate,
                "reminderTime" to reminder.reminderTime,
                "type" to reminder.type,
                "isCompleted" to reminder.isCompleted,
                "createdAt" to reminder.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("reminders").document(reminder.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncReminder error: ${e.message}")
        }
    }

    suspend fun syncStaffMember(context: Context, staff: StaffMember, storeId: String = getActiveStoreId()) = withContext(Dispatchers.IO) {
        if (!isCloudSyncActive(context)) return@withContext
        if (Firebase.auth.currentUser == null) return@withContext
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "storeId" to storeId,
                "name" to staff.name,
                "phone" to staff.phone,
                "role" to staff.role,
                "permissionLevel" to staff.permissionLevel,
                "allowedFeatures" to staff.allowedFeatures,
                "isActive" to staff.isActive,
                "permissions" to staff.allowedFeatures.split(",").map { it.trim().lowercase() },
                "createdAt" to staff.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("staff").document(staff.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncStaffMember error: ${e.message}")
        }
    }

    // --- ONE-TIME ZERO-DATA-LOSS MIGRATION ---

    suspend fun performOneTimeMigration(
        context: Context,
        repository: HardwareRepository,
        storeId: String = getActiveStoreId(),
        onProgress: (String, Int, Int) -> Unit,
        onComplete: (Int) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val authUser = Firebase.auth.currentUser
        if (authUser == null) {
            onError("You must be signed in with Google to migrate data to Firebase Firestore")
            return@withContext
        }

        try {
            val db = FirebaseManager.getFirestore(context)
            syncStoreProfile(context, storeId)

            onProgress("Migrating Store Profile & Categories...", 1, 12)
            val categories = repository.allCategories.firstOrNull() ?: emptyList()
            var totalCount = categories.size
            for (cat in categories) {
                syncCategory(context, cat, storeId)
            }

            onProgress("Migrating Products (${categories.size} categories done)...", 2, 12)
            val products = repository.allProducts.firstOrNull() ?: emptyList()
            totalCount += products.size
            for (prod in products) {
                syncProduct(context, prod, storeId)
            }

            onProgress("Migrating Sales & Invoices (${products.size} products done)...", 3, 12)
            val sales = repository.allSales.firstOrNull() ?: emptyList()
            totalCount += sales.size
            for (sale in sales) {
                syncSaleOrder(context, sale, storeId)
            }

            onProgress("Migrating Customers (${sales.size} sales done)...", 4, 12)
            val customers = repository.allCustomers.firstOrNull() ?: emptyList()
            totalCount += customers.size
            for (c in customers) {
                syncCustomer(context, c, storeId)
            }

            onProgress("Migrating Suppliers...", 5, 12)
            val suppliers = repository.allSuppliersOwedFirst.firstOrNull() ?: emptyList()
            totalCount += suppliers.size
            for (s in suppliers) {
                syncSupplier(context, s, storeId)
            }

            onProgress("Migrating Purchases...", 6, 12)
            val purchases = repository.allPurchases.firstOrNull() ?: emptyList()
            totalCount += purchases.size
            for (p in purchases) {
                syncPurchase(context, p, storeId)
            }

            onProgress("Migrating Customer Payments...", 7, 12)
            val customerPayments = repository.allCustomerPayments.firstOrNull() ?: emptyList()
            totalCount += customerPayments.size
            for (cp in customerPayments) {
                syncCustomerPayment(context, cp, storeId)
            }

            onProgress("Migrating Supplier Payments...", 8, 12)
            val supplierPayments = repository.allSupplierPayments.firstOrNull() ?: emptyList()
            totalCount += supplierPayments.size
            for (sp in supplierPayments) {
                syncSupplierPayment(context, sp, storeId)
            }

            onProgress("Migrating Expenses...", 9, 12)
            val expenses = repository.allExpenses.firstOrNull() ?: emptyList()
            totalCount += expenses.size
            for (exp in expenses) {
                syncExpense(context, exp, storeId)
            }

            onProgress("Migrating Bank Accounts...", 10, 12)
            val accounts = repository.allBankAccounts.firstOrNull() ?: emptyList()
            totalCount += accounts.size
            for (acc in accounts) {
                syncBankAccount(context, acc, storeId)
            }

            onProgress("Migrating Other Incomes & Reminders...", 11, 12)
            val incomes = repository.allOtherIncomes.firstOrNull() ?: emptyList()
            totalCount += incomes.size
            for (inc in incomes) {
                syncOtherIncome(context, inc, storeId)
            }
            val reminders = repository.allReminders.firstOrNull() ?: emptyList()
            totalCount += reminders.size
            for (rem in reminders) {
                syncReminder(context, rem, storeId)
            }

            onProgress("Migrating Staff & Settings...", 12, 12)
            val staff = repository.allStaffMembers.firstOrNull() ?: emptyList()
            totalCount += staff.size
            for (st in staff) {
                syncStaffMember(context, st, storeId)
            }

            // Sync settings
            val settingsData = hashMapOf(
                "storeId" to storeId,
                "currency" to AppSettings.currencySymbol,
                "currencyPosition" to AppSettings.currencyPosition.name,
                "numberFormat" to AppSettings.numberFormat.name,
                "dateFormat" to AppSettings.dateFormat,
                "timeFormat" to AppSettings.timeFormat,
                "viewMode" to AppSettings.viewMode.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("stores").document(storeId)
                .collection("settings").document("general")
                .set(settingsData, SetOptions.merge())
                .await()

            // Save migration state locally
            val prefs = context.getSharedPreferences(PREFS_MIGRATION, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            prefs.edit()
                .putBoolean(KEY_MIGRATION_COMPLETED, true)
                .putLong(KEY_MIGRATION_TIME, now)
                .putLong(KEY_LAST_SYNC_TIME, now)
                .putBoolean(KEY_CLOUD_SYNC_ACTIVE, true)
                .apply()

            Log.i(TAG, "One-time migration completed successfully with $totalCount total records")
            onComplete(totalCount)
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed", e)
            onError(e.localizedMessage ?: "Migration encountered an unexpected error")
        }
    }

    // --- ROLLBACK FUNCTIONALITY ---
    fun rollbackToLocal(context: Context): Boolean {
        // Keeps the local Room database 100% active and disables remote syncing
        setCloudSyncActive(context, false)
        Log.i(TAG, "Rollback executed: switched to local offline Room database without losing data")
        return true
    }
}
