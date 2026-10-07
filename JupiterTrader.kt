class JupiterTrader(private val wallet: MobileWalletAdapter) {
    suspend fun executeBuy(signal: Signal): Boolean {
        // 1. Quote
        val quote = getJupiterQuote(SOL_MINT, signal.token, signal.amount)
        if (quote.priceImpact > RiskManager.MAX_SLIPPAGE_PCT) return false
        
        // 2. Swap
        val tx = wallet.signAndSend(quote.transaction)
        // 3. Immediately set TP/SL monitoring in background
        ShivBackgroundService.monitorPosition(signal, quote.entryPrice)
        return true
    }
}
