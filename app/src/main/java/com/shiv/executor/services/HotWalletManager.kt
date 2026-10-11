package com.shiv.executor.services
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
object HotWalletManager {
    fun hasHotWallet(ctx: Context): Boolean {
        return ctx.getSharedPreferences("shiv_hot",0).contains("hot_pubkey")
    }
    suspend fun getBalanceSOL(ctx: Context): Double = withContext(Dispatchers.IO) { 0.5 }
    fun getHotWalletPubkey(ctx: Context): String? {
        return ctx.getSharedPreferences("shiv_hot",0).getString("hot_pubkey",null)
    }
}
