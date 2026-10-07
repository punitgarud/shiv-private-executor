package com.shiv.executor
data class Quote(val priceImpact: Double, val transaction: String, val entryPrice: Double)
object SOL_MINT
suspend fun getJupiterQuote(from: Any, token: String, amount: Double): Quote = Quote(2.0, "tx", 1.0)
class JupiterTrader(private val wallet: MobileWalletAdapter) {
    suspend fun executeBuy(signal: Signal): Boolean {
        val quote = getJupiterQuote(SOL_MINT, signal.token, signal.amount)
        if (quote.priceImpact > RiskManager.MAX_SLIPPAGE_PCT) return false
        wallet.signAndSend(quote.transaction)
        ShivBackgroundService.monitorPosition(signal, quote.entryPrice)
        return true
    }
}