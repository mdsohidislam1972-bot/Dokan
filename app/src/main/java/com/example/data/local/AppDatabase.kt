package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BankAccountDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.CustomerPaymentDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.OtherIncomeDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.SaleOrderDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.SupplierPaymentDao
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

@Database(
    entities = [
        Product::class,
        SaleOrder::class,
        Category::class,
        PurchaseRecord::class,
        Customer::class,
        Supplier::class,
        SupplierPayment::class,
        CustomerPayment::class,
        Expense::class,
        BankAccount::class,
        OtherIncome::class,
        AppReminder::class,
        StaffMember::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleOrderDao(): SaleOrderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun supplierPaymentDao(): SupplierPaymentDao
    abstract fun customerPaymentDao(): CustomerPaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun otherIncomeDao(): OtherIncomeDao
    abstract fun reminderDao(): ReminderDao
    abstract fun staffDao(): StaffDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN colorHex TEXT NOT NULL DEFAULT '#10B981'")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE products ADD COLUMN barcode TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE products ADD COLUMN location TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE products ADD COLUMN wholesalePrice REAL NOT NULL DEFAULT 0.0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE products ADD COLUMN mrp REAL NOT NULL DEFAULT 0.0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE customers ADD COLUMN category TEXT NOT NULL DEFAULT 'Regular'")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE suppliers ADD COLUMN category TEXT NOT NULL DEFAULT 'Supplier'")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hardware_manager_database"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getRepository(context: Context): com.example.data.repository.HardwareRepository {
            val db = getDatabase(context)
            return com.example.data.repository.HardwareRepository(
                productDao = db.productDao(),
                saleOrderDao = db.saleOrderDao(),
                categoryDao = db.categoryDao(),
                purchaseDao = db.purchaseDao(),
                customerDao = db.customerDao(),
                supplierDao = db.supplierDao(),
                supplierPaymentDao = db.supplierPaymentDao(),
                customerPaymentDao = db.customerPaymentDao(),
                expenseDao = db.expenseDao(),
                bankAccountDao = db.bankAccountDao(),
                otherIncomeDao = db.otherIncomeDao(),
                reminderDao = db.reminderDao(),
                staffDao = db.staffDao(),
                context = context.applicationContext
            )
        }
    }
}
