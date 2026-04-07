package com.halan.twittercounter.data.remote

import okhttp3.Request
import java.net.URLEncoder
import java.util.TreeMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.util.UUID

object OAuthHelper {

    fun sign(
        request: Request,
        consumerKey: String,
        consumerSecret: String,
        accessToken: String,
        accessTokenSecret: String,
    ): Request {
        val method = request.method
        val url = request.url.toString().substringBefore("?")
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = UUID.randomUUID().toString().replace("-", "")

        val oauthParams = TreeMap<String, String>().apply {
            put("oauth_consumer_key", consumerKey)
            put("oauth_nonce", nonce)
            put("oauth_signature_method", "HMAC-SHA1")
            put("oauth_timestamp", timestamp)
            put("oauth_token", accessToken)
            put("oauth_version", "1.0")
        }

        val paramString = oauthParams.entries.joinToString("&") {
            "${encode(it.key)}=${encode(it.value)}"
        }

        val baseString = "${encode(method)}&${encode(url)}&${encode(paramString)}"
        val signingKey = "${encode(consumerSecret)}&${encode(accessTokenSecret)}"
        val signature = hmacSha1(baseString, signingKey)

        oauthParams["oauth_signature"] = signature

        val authHeader = "OAuth " + oauthParams.entries.joinToString(", ") {
            "${encode(it.key)}=\"${encode(it.value)}\""
        }

        return request.newBuilder()
            .addHeader("Authorization", authHeader)
            .build()
    }

    private fun hmacSha1(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA1"))
        return Base64.encodeToString(mac.doFinal(data.toByteArray()), Base64.NO_WRAP)
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}