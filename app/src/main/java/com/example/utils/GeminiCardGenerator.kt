package com.example.utils

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiCardGenerator {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .build()

    data class GeneratedCard(
        val titleBn: String,
        val titleEn: String,
        val messageBn: String,
        val messageEn: String
    )

    suspend fun generateCard(
        shopName: String,
        occasion: String,
        customPrompt: String,
        isBangla: Boolean
    ): GeneratedCard = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackCard(shopName, occasion, customPrompt, isBangla)
        }

        try {
            val systemPrompt = """
                You are a creative business marketing copywriter for Dokan store '$shopName'.
                Occasion / Category: $occasion
                Custom prompt / special instructions: $customPrompt
                Generate an engaging, warm promotional or greeting message for store customers.
                Respond ONLY with a JSON object in this format:
                {
                   "titleBn": "...",
                   "titleEn": "...",
                   "messageBn": "...",
                   "messageEn": "..."
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val partObj = JSONObject().apply {
                        put("text", systemPrompt)
                    }
                    val parts = JSONArray().apply { put(partObj) }
                    val contentObj = JSONObject().apply { put("parts", parts) }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string().orEmpty()
                val json = JSONObject(responseString)
                val text = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cardJson = JSONObject(text.trim())
                GeneratedCard(
                    titleBn = cardJson.optString("titleBn", if (isBangla) "AI বিশেষ শুভেচ্ছা কার্ড" else "AI Custom Greeting"),
                    titleEn = cardJson.optString("titleEn", "AI Custom Greeting"),
                    messageBn = cardJson.optString("messageBn", fallbackBn(shopName, occasion, customPrompt)),
                    messageEn = cardJson.optString("messageEn", "Warmest greetings from $shopName! $customPrompt")
                )
            } else {
                fallbackCard(shopName, occasion, customPrompt, isBangla)
            }
        } catch (e: Throwable) {
            fallbackCard(shopName, occasion, customPrompt, isBangla)
        }
    }

    private fun fallbackCard(
        shopName: String,
        occasion: String,
        prompt: String,
        isBangla: Boolean
    ): GeneratedCard {
        val msgBn = fallbackBn(shopName, occasion, prompt)
        val msgEn = "Warmest greetings from $shopName! Wishing you happiness and prosperity. ${prompt.ifBlank { "Thank you for being our valued customer." }}"
        return GeneratedCard(
            titleBn = if (isBangla) "AI বিশেষ শুভেচ্ছা কার্ড" else "AI Custom Greeting",
            titleEn = "AI Custom Greeting",
            messageBn = msgBn,
            messageEn = msgEn
        )
    }

    private fun fallbackBn(shopName: String, occasion: String, prompt: String): String {
        return when (occasion) {
            "Sale" -> "গ্রাহকদের ভালোবাসায় $shopName-এ চলছে বিশেষ মূল্যছাড় উৎসব! ${prompt.ifBlank { "সেরা মূল্যে সেরা পণ্য সংগ্রহ করুন আজই।" }}"
            "Festival" -> "পবিত্র উৎসবের আনন্দে মুখরিত হোক আপনার গৃহকোণ। $shopName-এর পক্ষ থেকে আপনাকে ও আপনার পরিবারকে আন্তরিক মোবারকবাদ।"
            "Morning" -> "একটি নতুন দিন, অফুরন্ত সম্ভাবনা! আজকের দিনটি সাফল্য ও আনন্দে ভরে উঠুক। $shopName-এর সাথে থাকার জন্য ধন্যবাদ।"
            else -> "প্রিয় গ্রাহক, আপনার অবিরাম সমর্থন আমাদের পথচলার শক্তি। ${prompt.ifBlank { "আপনার প্রতিটি মুহূর্ত আনন্দময় হোক।" }}"
        }
    }
}
