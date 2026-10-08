package com.shiv.executor

import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MobileWalletAdapter(private val activity: android.app.Activity) {
    private val walletAdapter = MobileWalletAdapterClient()
    private var authToken: String? = null

    suspend fun connect(): String = withContext(Dispatchers.IO) {
        val result = walletAdapter.authorize(
            Uri.parse("https://shiv.executor"),
            Uri.parse("favicon.ico"),
            "Shiv Executor",
            activity
        ).get()
        authToken = result.authToken
        result.accounts.first().address
    }

    suspend fun signAndSend(base64Tx: String): String = withContext(Dispatchers.IO) {
        val txBytes = android.util.Base64.decode(base64Tx, android.util.Base64.DEFAULT)
        val result = walletAdapter.transact(activity) { authResult ->
            signAndSendTransactions(arrayOf(txBytes))
        }.get()
        result.signatures.first().toString()
    }
}