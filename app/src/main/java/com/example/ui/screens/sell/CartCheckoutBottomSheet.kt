package com.example.ui.screens.sell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Customer
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutBottomSheet(
    viewModel: SellViewModel,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var discountText by remember { mutableStateOf("") }
    var isCreditSale by remember { mutableStateOf(false) }
    var paidNowText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var customerDropdownOpen by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.total }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val netTotal = (subtotal - discount).coerceAtLeast(0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSlateSurface,
        modifier = modifier.testTag("sheet_cart_checkout")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = VividEmeraldAccent)
                    Text(
                        text = if (isBangla) "শপিং কার্ট ও চেকআউট" else "Shopping Cart & Checkout",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )
                }

                TextButton(onClick = { viewModel.clearCart() }) {
                    Text(if (isBangla) "কার্ট খালি করুন" else "Clear", color = LowStockRed, fontSize = 12.sp)
                }
            }

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "কার্ট ফাঁকা! ক্যাটালগ থেকে পণ্য যোগ করুন।" else "Cart is empty! Add products from list.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems) { item ->
                        Surface(
                            color = DarkSlateBackground,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.localizedName(isBangla),
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${formatTaka(item.unitPrice, currentLanguage)} × ${if (isBangla) toBengaliNumerals(item.quantity) else item.quantity} = ${formatTaka(item.total, currentLanguage)}",
                                        style = AppTypographyScale.hintText,
                                        color = VividEmeraldAccent
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(item.product.id, -1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary)
                                    }

                                    Text(
                                        text = if (isBangla) toBengaliNumerals(item.quantity) else item.quantity.toString(),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(item.product.id, 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary)
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeFromCart(item.product.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = LowStockRed)
                                    }
                                }
                            }
                        }
                    }
                }

                // Customer selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBangla) "ক্রেতা: ${selectedCustomer?.name ?: "ক্যাশ গ্রাহক"}" else "Buyer: ${selectedCustomer?.name ?: "Walk-in"}",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    TextButton(onClick = { customerDropdownOpen = true }) {
                        Text(if (isBangla) "পরিবর্তন" else "Select Party", color = VividEmeraldAccent, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = customerDropdownOpen,
                        onDismissRequest = { customerDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBangla) "ক্যাশ গ্রাহক (Walk-in)" else "Walk-in Cash") },
                            onClick = {
                                selectedCustomer = null
                                customerDropdownOpen = false
                            }
                        )
                        allCustomers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.name} (${c.phone})") },
                                onClick = {
                                    selectedCustomer = c
                                    customerDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Discount & Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text(if (isBangla) "ছাড় (টাকা)" else "Discount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = DarkSlateBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(if (isBangla) "মোট প্রদেয়" else "Net Total", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(formatTaka(netTotal, currentLanguage), fontWeight = FontWeight.Bold, color = VividEmeraldAccent, fontSize = 16.sp)
                        }
                    }
                }

                // Credit Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "বাকি বিক্রি (Credit Sale)" else "Sell on Credit (Baki)",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = isCreditSale,
                        onCheckedChange = { isCreditSale = it }
                    )
                }

                if (isCreditSale) {
                    OutlinedTextField(
                        value = paidNowText,
                        onValueChange = { paidNowText = it },
                        label = { Text(if (isBangla) "নগদ জমা (Paid Now)" else "Amount Paid Now") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        val paid = if (isCreditSale) (paidNowText.toDoubleOrNull() ?: 0.0) else netTotal
                        viewModel.confirmCartSale(
                            isBangla = isBangla,
                            customer = selectedCustomer,
                            rawDiscount = discount,
                            isCredit = isCreditSale,
                            paidNow = paid,
                            notes = notesText,
                            onSuccess = { onDismiss() }
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_checkout"),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(
                        text = if (isBangla) "বিক্রি সম্পন্ন করুন (${formatTaka(netTotal, currentLanguage)})" else "Complete Checkout (${formatTaka(netTotal, currentLanguage)})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
