package com.example.ui.screens.utilities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlin.math.pow

@Composable
fun CalculatorsScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var selectedTab by remember { mutableIntStateOf(0) } // 0: EMI, 1: Interest, 2: Tax / VAT

    val tabs = listOf(
        if (isBangla) "EMI কিস্তি" else "EMI Loan",
        if (isBangla) "সুদ হিসাব (Interest)" else "Interest",
        if (isBangla) "ভ্যাট ও ট্যাক্স" else "Tax / VAT"
    )

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
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> EmiCalculatorSection(currentLanguage)
                1 -> InterestCalculatorSection(currentLanguage)
                2 -> TaxVatCalculatorSection(currentLanguage)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. EMI CALCULATOR
// -------------------------------------------------------------
@Composable
private fun EmiCalculatorSection(currentLanguage: AppLanguage) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val context = LocalContext.current

    var principalStr by remember { mutableStateOf("100000") }
    var annualRateStr by remember { mutableStateOf("12") }
    var tenureMonthsStr by remember { mutableStateOf("12") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val annualRate = annualRateStr.toDoubleOrNull() ?: 0.0
    val tenureMonths = tenureMonthsStr.toIntOrNull() ?: 1

    val monthlyRate = (annualRate / 12.0) / 100.0
    val emi = if (monthlyRate > 0 && tenureMonths > 0) {
        val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        (principal * monthlyRate * factor) / (factor - 1.0)
    } else if (tenureMonths > 0) {
        principal / tenureMonths
    } else 0.0

    val totalPayment = emi * tenureMonths
    val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it },
                    label = { Text(if (isBangla) "ঋণ বা আসল টাকা (Principal ৳)" else "Loan Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_emi_principal"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = annualRateStr,
                        onValueChange = { annualRateStr = it },
                        label = { Text(if (isBangla) "বার্ষিক সুদের হার (%)" else "Annual Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_emi_rate"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = tenureMonthsStr,
                        onValueChange = { tenureMonthsStr = it },
                        label = { Text(if (isBangla) "সময়কাল (মাস)" else "Tenure (Months)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_emi_tenure"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
            border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isBangla) "প্রতি মাসের কিস্তি (Monthly EMI)" else "Monthly EMI", fontSize = 13.sp, color = TextMuted)
                        Text(formatTaka(emi, currentLanguage), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("EMI", emi.toInt().toString()))
                            Toast.makeText(context, if (isBangla) "EMI কপি করা হয়েছে" else "EMI copied", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = DarkSlateCardBorder)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBangla) "মোট প্রদেয় সুদ:" else "Total Interest:", fontSize = 13.sp, color = TextMuted)
                    Text(formatTaka(totalInterest, currentLanguage), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBangla) "সর্বমোট প্রদেয় টাকা:" else "Total Amount Payable:", fontSize = 13.sp, color = TextMuted)
                    Text(formatTaka(totalPayment, currentLanguage), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. INTEREST CALCULATOR (Simple & Compound)
// -------------------------------------------------------------
@Composable
private fun InterestCalculatorSection(currentLanguage: AppLanguage) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var principalStr by remember { mutableStateOf("50000") }
    var rateStr by remember { mutableStateOf("9.5") }
    var timeYearsStr by remember { mutableStateOf("1") }
    var isCompound by remember { mutableStateOf(false) }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val timeYears = timeYearsStr.toDoubleOrNull() ?: 1.0

    val totalAmount = if (!isCompound) {
        principal + (principal * rate * timeYears / 100.0)
    } else {
        principal * (1.0 + rate / 100.0).pow(timeYears)
    }
    val interestEarned = (totalAmount - principal).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Toggle Simple vs Compound
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isCompound = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isCompound) VividEmeraldPrimary else DarkSlateSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "সাধারণ সুদ (Simple)" else "Simple Interest", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { isCompound = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCompound) VividEmeraldPrimary else DarkSlateSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "চক্রবৃদ্ধি (Compound)" else "Compound", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it },
                    label = { Text(if (isBangla) "মূলধন / জমা টাকা (৳)" else "Principal Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_interest_principal"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text(if (isBangla) "বার্ষিক হার (%)" else "Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_interest_rate"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = timeYearsStr,
                        onValueChange = { timeYearsStr = it },
                        label = { Text(if (isBangla) "সময়কাল (বছর)" else "Years") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_interest_years"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
            border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (isBangla) "মোট অর্জিত মুনাফা / সুদ:" else "Total Interest:", fontSize = 13.sp, color = TextMuted)
                Text(formatTaka(interestEarned, currentLanguage), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                HorizontalDivider(color = DarkSlateCardBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBangla) "সুদসহ সর্বমোট টাকা:" else "Maturity Total:", fontSize = 13.sp, color = TextMuted)
                    Text(formatTaka(totalAmount, currentLanguage), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TAX / VAT CALCULATOR
// -------------------------------------------------------------
@Composable
private fun TaxVatCalculatorSection(currentLanguage: AppLanguage) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var baseAmountStr by remember { mutableStateOf("1000") }
    var taxRateStr by remember { mutableStateOf("7.5") } // Default Bangladesh VAT 7.5% or 15%
    var isExclusive by remember { mutableStateOf(true) } // Exclusive: add tax on top; Inclusive: tax already inside

    val amount = baseAmountStr.toDoubleOrNull() ?: 0.0
    val rate = taxRateStr.toDoubleOrNull() ?: 0.0

    val taxAmount = if (isExclusive) {
        amount * (rate / 100.0)
    } else {
        amount - (amount / (1.0 + rate / 100.0))
    }
    val netTotal = if (isExclusive) amount + taxAmount else amount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isExclusive = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isExclusive) VividEmeraldPrimary else DarkSlateSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "ভ্যাট ছাড়া (Exclusive)" else "Tax Exclusive", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { isExclusive = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isExclusive) VividEmeraldPrimary else DarkSlateSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "ভ্যাট সহ (Inclusive)" else "Tax Inclusive", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = baseAmountStr,
                    onValueChange = { baseAmountStr = it },
                    label = { Text(if (isBangla) "টাকার পরিমাণ (৳)" else "Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_tax_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = taxRateStr,
                    onValueChange = { taxRateStr = it },
                    label = { Text(if (isBangla) "ভ্যাট বা ট্যাক্স হার (%)" else "Tax / VAT Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_tax_rate"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
            border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBangla) "ট্যাক্স / ভ্যাট পরিমাণ:" else "Tax / VAT Amount:", fontSize = 13.sp, color = TextMuted)
                    Text(formatTaka(taxAmount, currentLanguage), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                }
                HorizontalDivider(color = DarkSlateCardBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBangla) "সর্বমোট টাকা (Final Total):" else "Final Net Total:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(formatTaka(netTotal, currentLanguage), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                }
            }
        }
    }
}
