package com.example.ui.screens.utilities

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.data.local.entity.Supplier
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun DataImportScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Import Parties, 1: Import Items

    val samplePartiesCsv = """
Karim Store, 01812345678, Dhanmondi Dhaka, 2500, Customer
Jamal Enterprise, 01711223344, Mirpur Dhaka, 0, Customer
Meghna Hardware Supplier, 01922334455, Chawkbazar, 12000, Supplier
Bismillah Sanitary, 01633445566, Nawabpur Dhaka, 0, Supplier
""".trimIndent()

    val sampleItemsCsv = """
1-inch PVC Pipe, Pipes & Fittings, 180, 240, 50, pcs
LED Bulb 12W, Lights & Bulbs, 120, 160, 100, pcs
Stainless Steel Screw Pack, Nuts Bolts & Screws, 45, 70, 200, packet
Heavy Duty Hammer, Hand Tools, 320, 450, 15, pcs
Royal Luxury Paint 1L, Paint & Chemicals, 450, 580, 25, can
""".trimIndent()

    var partiesInput by remember { mutableStateOf("") }
    var itemsInput by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSlateSurface,
            contentColor = VividEmeraldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = VividEmeraldAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "পার্টি ইমপোর্ট (গ্রাহক ও মহাজন)" else "Import Parties", fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "পণ্য ইমপোর্ট (স্টক আইটেম)" else "Import Items", fontSize = 13.sp)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                // PARTIES IMPORT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isBangla) "পার্টি ডাটা ফরম্যাট (CSV বা টেক্সট):" else "Parties CSV Format:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Name, Phone, Address, Opening Baki (৳), Type (Customer/Supplier)",
                            fontSize = 12.sp,
                            color = VividEmeraldAccent
                        )

                        OutlinedTextField(
                            value = partiesInput,
                            onValueChange = { partiesInput = it },
                            placeholder = { Text(if (isBangla) "এখানে পার্টি তালিকা পেস্ট করুন..." else "Paste parties CSV lines here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("input_import_parties"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividEmeraldPrimary,
                                unfocusedBorderColor = DarkSlateCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { partiesInput = samplePartiesCsv },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isBangla) "নমুনা ডেটা বসান" else "Load Sample", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (partiesInput.isBlank()) {
                                        Toast.makeText(context, if (isBangla) "অনুগ্রহ করে ডাটা পেস্ট করুন" else "Please paste data first", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isImporting = true
                                    scope.launch {
                                        val lines = partiesInput.lines().filter { it.isNotBlank() }
                                        var count = 0
                                        lines.forEach { line ->
                                            val parts = line.split(",").map { it.trim() }
                                            if (parts.isNotEmpty()) {
                                                val name = parts.getOrNull(0) ?: ""
                                                val phone = parts.getOrNull(1) ?: ""
                                                val addr = parts.getOrNull(2) ?: ""
                                                val baki = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                                                val type = parts.getOrNull(4) ?: "Customer"

                                                if (type.equals("Supplier", ignoreCase = true)) {
                                                    repository.insertSupplier(
                                                        Supplier(
                                                            name = name,
                                                            phone = phone,
                                                            address = addr,
                                                            amountOwed = baki
                                                        )
                                                    )
                                                } else {
                                                    repository.insertCustomer(
                                                        Customer(
                                                            name = name,
                                                            phone = phone,
                                                            address = addr,
                                                            totalBaki = baki,
                                                            lastTransactionTime = System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                                count++
                                            }
                                        }
                                        isImporting = false
                                        partiesInput = ""
                                        Toast.makeText(context, if (isBangla) "$count টি পার্টি সফলভাবে ইমপোর্ট হয়েছে!" else "$count parties imported successfully!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isImporting,
                                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("btn_import_parties_submit")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "ইমপোর্ট করুন" else "Import Parties", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // ITEMS IMPORT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isBangla) "পণ্য ডাটা ফরম্যাট (CSV বা টেক্সট):" else "Products CSV Format:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Item Name, Category, Buy Price (৳), Sell Price (৳), Stock, Unit",
                            fontSize = 12.sp,
                            color = VividEmeraldAccent
                        )

                        OutlinedTextField(
                            value = itemsInput,
                            onValueChange = { itemsInput = it },
                            placeholder = { Text(if (isBangla) "এখানে পণ্য তালিকা পেস্ট করুন..." else "Paste products CSV lines here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("input_import_items"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividEmeraldPrimary,
                                unfocusedBorderColor = DarkSlateCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { itemsInput = sampleItemsCsv },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isBangla) "নমুনা ডেটা বসান" else "Load Sample", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (itemsInput.isBlank()) {
                                        Toast.makeText(context, if (isBangla) "অনুগ্রহ করে ডাটা পেস্ট করুন" else "Please paste data first", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isImporting = true
                                    scope.launch {
                                        val lines = itemsInput.lines().filter { it.isNotBlank() }
                                        var count = 0
                                        lines.forEach { line ->
                                            val parts = line.split(",").map { it.trim() }
                                            if (parts.isNotEmpty()) {
                                                val name = parts.getOrNull(0) ?: ""
                                                val cat = parts.getOrNull(1) ?: "Other"
                                                val buy = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                                                val sell = parts.getOrNull(3)?.toDoubleOrNull() ?: (buy * 1.25)
                                                val stock = parts.getOrNull(4)?.toIntOrNull() ?: 10
                                                val unit = parts.getOrNull(5) ?: "pcs"

                                                repository.insertProduct(
                                                    Product(
                                                        nameBn = name,
                                                        nameEn = name,
                                                        category = cat,
                                                        buyPrice = buy,
                                                        sellPrice = sell,
                                                        stockQuantity = stock,
                                                        unitBn = if (unit == "pcs") "টি" else unit,
                                                        unitEn = unit
                                                    )
                                                )
                                                count++
                                            }
                                        }
                                        isImporting = false
                                        itemsInput = ""
                                        Toast.makeText(context, if (isBangla) "$count টি পণ্য সফলভাবে ইমপোর্ট হয়েছে!" else "$count products imported successfully!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isImporting,
                                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("btn_import_items_submit")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "ইমপোর্ট করুন" else "Import Items", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
