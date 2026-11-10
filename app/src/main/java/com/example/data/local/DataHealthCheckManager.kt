package com.example.data.local

import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.data.local.entity.Supplier
import com.example.data.repository.HardwareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HealthIssue(
    val id: String,
    val type: HealthIssueType,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val affectedItemName: String,
    val recommendedFixEn: String,
    val recommendedFixBn: String,
    val fixAction: suspend (HardwareRepository) -> Unit
)

enum class HealthIssueType {
    NEGATIVE_STOCK,
    MISSING_PRICE,
    NEGATIVE_MARGIN,
    BALANCE_MISMATCH
}

object DataHealthCheckManager {

    /**
     * Scans database products, customers, suppliers, and sales to detect math errors,
     * negative stock, missing prices, and mismatched numbers.
     */
    suspend fun runHealthCheck(
        repository: HardwareRepository,
        products: List<Product>,
        customers: List<Customer>,
        suppliers: List<Supplier>,
        sales: List<SaleOrder>
    ): List<HealthIssue> = withContext(Dispatchers.Default) {
        val issues = mutableListOf<HealthIssue>()

        // 1. Check for Negative Stock
        products.filter { it.stockQuantity < 0 }.forEach { p ->
            issues.add(
                HealthIssue(
                    id = "neg_stock_${p.id}",
                    type = HealthIssueType.NEGATIVE_STOCK,
                    titleEn = "Negative Stock Detected",
                    titleBn = "নেগেটিভ (ঘাটতি) স্টক পাওয়া গেছে",
                    descriptionEn = "Product '${p.name}' currently has negative quantity (${p.stockQuantity} ${p.unit}).",
                    descriptionBn = "'${p.name}' পণ্যের স্টক মাইনাসে (${p.stockQuantity} ${p.unit}) রয়েছে।",
                    affectedItemName = p.name,
                    recommendedFixEn = "Reset stock to 0",
                    recommendedFixBn = "স্টক ০ তে রিসেট করুন",
                    fixAction = { repo ->
                        repo.updateProduct(p.copy(stockQuantity = 0))
                    }
                )
            )
        }

        // 2. Check for Missing Prices (Buy or Sell price <= 0)
        products.filter { it.buyPrice <= 0.0 || it.sellPrice <= 0.0 }.forEach { p ->
            val isBuyMissing = p.buyPrice <= 0.0
            issues.add(
                HealthIssue(
                    id = "missing_price_${p.id}",
                    type = HealthIssueType.MISSING_PRICE,
                    titleEn = "Missing or Zero Price",
                    titleBn = "মূল্য শূন্য বা নির্ধারিত নেই",
                    descriptionEn = "Product '${p.name}' has invalid price (Buy: ৳${p.buyPrice}, Sell: ৳${p.sellPrice}).",
                    descriptionBn = "'${p.name}' পণ্যের ক্রয়মূল্য বা বিক্রয়মূল্য সঠিকভাবে দেওয়া নেই।",
                    affectedItemName = p.name,
                    recommendedFixEn = if (isBuyMissing) "Set default buy price to ৳10" else "Set default sell price to ৳15",
                    recommendedFixBn = if (isBuyMissing) "ক্রয়মূল্য ১০ টাকা নির্ধারণ করুন" else "বিক্রয়মূল্য ১৫ টাকা নির্ধারণ করুন",
                    fixAction = { repo ->
                        val newBuy = if (p.buyPrice <= 0.0) (if (p.sellPrice > 0) p.sellPrice * 0.8 else 10.0) else p.buyPrice
                        val newSell = if (p.sellPrice <= 0.0) newBuy * 1.2 else p.sellPrice
                        repo.updateProduct(p.copy(buyPrice = newBuy, sellPrice = newSell))
                    }
                )
            )
        }

        // 3. Check for Negative Profit Margin (Buy Price > Sell Price)
        products.filter { it.buyPrice > it.sellPrice && it.sellPrice > 0 }.forEach { p ->
            val suggestedSell = (p.buyPrice * 1.15).toInt().toDouble()
            issues.add(
                HealthIssue(
                    id = "neg_margin_${p.id}",
                    type = HealthIssueType.NEGATIVE_MARGIN,
                    titleEn = "Loss-Making Price Margin",
                    titleBn = "ক্রয়মূল্যের চেয়ে কম বিক্রয়মূল্য",
                    descriptionEn = "Product '${p.name}' buy price (৳${p.buyPrice}) exceeds selling price (৳${p.sellPrice}), causing a loss on every sale.",
                    descriptionBn = "'${p.name}' পণ্যের বিক্রয়মূল্য ক্রয়মূল্যের চেয়ে কম, এতে প্রতি বিক্রয়ে লোকসান হচ্ছে।",
                    affectedItemName = p.name,
                    recommendedFixEn = "Set sell price to ৳$suggestedSell (15% margin)",
                    recommendedFixBn = "বিক্রয়মূল্য ৳$suggestedSell নির্ধারণ করুন (১৫% লাভ)",
                    fixAction = { repo ->
                        repo.updateProduct(p.copy(sellPrice = suggestedSell))
                    }
                )
            )
        }

        // 4. Check for Customer Ledger Math / Mismatched Baki
        customers.filter { it.totalBaki < 0 }.forEach { cust ->
            issues.add(
                HealthIssue(
                    id = "neg_cust_baki_${cust.id}",
                    type = HealthIssueType.BALANCE_MISMATCH,
                    titleEn = "Negative Customer Baki",
                    titleBn = "কাস্টমার বাকি নেগেটিভ হয়েছে",
                    descriptionEn = "Customer '${cust.name}' has negative due balance (৳${cust.totalBaki.toLong()}).",
                    descriptionBn = "কাস্টমার '${cust.name}'-এর বাকির হিসাব ঋণাত্মক (৳${cust.totalBaki.toLong()}) হয়ে আছে।",
                    affectedItemName = cust.name,
                    recommendedFixEn = "Recalibrate balance to ৳0",
                    recommendedFixBn = "বাকির খাতা ০ তে সমন্বয় করুন",
                    fixAction = { repo ->
                        repo.updateCustomer(cust.copy(totalBaki = 0.0))
                    }
                )
            )
        }

        // 5. Check for Supplier Amount Owed Mismatch
        suppliers.filter { it.amountOwed < 0 }.forEach { supp ->
            issues.add(
                HealthIssue(
                    id = "neg_supp_owed_${supp.id}",
                    type = HealthIssueType.BALANCE_MISMATCH,
                    titleEn = "Negative Supplier Due",
                    titleBn = "মহাজন দেনা নেগেটিভ হয়েছে",
                    descriptionEn = "Supplier '${supp.name}' has negative amount owed (৳${supp.amountOwed.toLong()}).",
                    descriptionBn = "মহাজন '${supp.name}'-এর দেনা হিসাব ঋণাত্মক (৳${supp.amountOwed.toLong()}) হয়ে আছে।",
                    affectedItemName = supp.name,
                    recommendedFixEn = "Recalibrate balance to ৳0",
                    recommendedFixBn = "দেনা হিসাব ০ তে সমন্বয় করুন",
                    fixAction = { repo ->
                        repo.updateSupplier(supp.copy(amountOwed = 0.0))
                    }
                )
            )
        }

        issues
    }
}
