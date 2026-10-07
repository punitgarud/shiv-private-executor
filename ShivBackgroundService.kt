class ShivBackgroundService : Service() {
    // Runs every 2 mins
    fun checkSignals() {
        val json = fetch("https://raw.githubusercontent.com/YOURNAME/SHIV/main/active_modules.json")
        val signal = json.active_signal
        if (signal?.signal == "BUY" && RiskManager.canTrade(signal, getBalance())) {
            executeBuy(signal)
        }
        // Auto SELL logic: checks all open positions
        openPositions.forEach { pos ->
            if (pos.pnlPercent >= 60 || pos.pnlPercent <= -20) {
                executeSell(pos) // Auto book profit or stop loss
            }
        }
    }
}
