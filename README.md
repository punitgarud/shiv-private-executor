### 1. `punitgarud/Shiv` - Public Dashboard (Web)

Create / replace `README.md` in that repo:
# SHIV - Solana Executor Dashboard

Live dashboard for SHIV 24/7 Executor - Shows top performing modules, SOL price, and trading signals.

**Live Site:** https://punitgarud.github.io/Shiv/

## What is SHIV?

SHIV is a modular Solana on-chain executor system. This repo is the **public frontend** that displays:
- Top 4 performing trading modules
- Real-time SOL price
- Scores, Sharpe, WinRate, PnL signals
- Entry/Current price, price change

It feeds data to the private executor app via `data.json` and `api/top4.json`

## Architecture

GitHub Pages (this repo)
  ├── http://index.html        # AMOLED black dashboard
  ├── http://data.json         # Main feed: { solPriceUsd, top4: [...] }
  ├── api/top4.json     # Backup feed
  └── http://README.md
         |
         v
Shiv-Private-Executor (Android App)
  └── http://PerformanceService.kt -> fetches http://data.json

## Data Format

`data.json` / `api/top4.json`:
```json
{
  "solPriceUsd": 152.5,
  "top4": [
    {
      "id": "JUP",
      "name": "Jupiter Scan",
      "score": 95.5,
      "mint": "So11111111111111111111111111111111111111112",
      "signal": "BUY",
      "entryPrice": 0.0012,
      "currentPrice": 0.0015,
      "priceChangePct": 25.0,
      "sharpe": 2.1,
      "winRate": 78.5,
      "maxDrawdown": -2.3,
      "winStreak": 5
    }
  ]
}
## Modules Explained
Code | Name | Logic
JUP | Jupiter Scan | Scans Jupiter routes for best price
MEV | MEV Shield 1% | Blocks trades with >1% MEV impact
RISK | Risk Guard -0.05 | Stops if daily loss > 0.05 SOL
SHARPE | Sharpe >1.5 | Only trades if Sharpe > 1.5
FEE | Fee Lowest | Picks lowest fee route among 3 Jupiter quotes
AUTO | Auto Sell | Auto-sells on signal flip
## How to Update

1. Edit `data.json` manually or via script
2. Commit & push to `main`
3. GitHub Pages auto-deploys in 1-2 min
4. Private executor picks it up in next fetch cycle

You can automate updates with GitHub Action that runs a python script to calculate scores and push `data.json`.

## Setup Locally
git clone https://github.com/punitgarud/Shiv.git
cd Shiv
python -m http.server 8000
# open http://localhost:8000
No build needed - pure static HTML/JSON.

## Security

This repo contains *NO private keys*. Only public signals. Private keys stay only in the Android private executor (never pushed).

---
Built for SHIV 24/7 Executor System - AMOLED Black Edition

### 2. `punitgarud/shiv-private-executor` - Android Private Executor

Replace `README.md` in private repo:

```markdown
# SHIV Private Executor - Android 24/7

Private Android app that runs SHIV trading modules 24/7 in background as a Foreground Service. AMOLED Black UI.

**This repo is PRIVATE - Never share APK publicly.**

## Features

- **24/7 Foreground Service** with notification (survives app kill)
- **6 Selectable Modules:** JUP, MEV, RISK, SHARPE, FEE, AUTO
- **Jupiter Integration:** Fetches 3 quotes, picks lowest fee (score = impact*10000 - outAmount)
- **Performance Feed:** Fetches top4 from `https://punitgarud.github.io/Shiv/data.json`
- **Risk Management:** Daily loss guard, balance tracking, trade logs
- **AMOLED Black UI:** Pure black background for battery saving
- **Auto-Build:** GitHub Actions builds Debug APK on every push

## Architecture

MainActivity (UI)
  ├── StatusManager (SharedPreferences storage)
  │     ├── isRunning, getTrades, getPnl, getBalance, getLogs
  │     ├── getModules / setModules
  │     └── addDailyLoss, resetDailyLoss
  └── ShivAutoService (Foreground Service)
        ├── PerformanceService -> fetchTop4() from GitHub Pages
        ├── JupiterService -> getBestLowestFeeQuote() + getSwapTransaction()
        ├── SolanaService -> Send signed tx (needs private key)
        └── RiskManager -> Checks MEV, Sharpe, Daily Loss

Services:
- app/src/main/java/com/shiv/executor/services/
    - http://JupiterService.kt
    - http://PerformanceService.kt
    - http://SolanaService.kt (add your key handling here)
    - http://ShivAutoService.kt

## Setup

### Prerequisites
- Android Studio or Termux + GitHub Actions
- Solana wallet private key (for real trading)

### 1. Clone

```bash
git clone https://github.com/punitgarud/shiv-private-executor.git
cd shiv-private-executor
### 2. Configure Private Key (IMPORTANT)

*Never commit private key to git!*

Add to `local.properties` (gitignored):
SOLANA_PRIVATE_KEY=your_base58_private_key_here
RPC_URL=https://api.mainnet-beta.solana.com
Or set as GitHub Secret for Actions build.

In `SolanaService.kt`, load it:
val privateKey = BuildConfig.SOLANA_PRIVATE_KEY // or from SecureStorage
### 3. Build

*Option A: GitHub Actions (recommended for Termux)*
Push to main -> Actions -> Build Debug APK -> Download artifact
git add .
git commit -m "fix"
git push origin main
*Option B: Local*
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
### 4. Install on Phone

- Enable "Install unknown apps"
- Install APK
- Grant Notification Permission (required for 24/7 service)
- Select Modules -> SAVE MODULES
- Click START 24/7 EXECUTOR

App will show ● RUNNING 24/7, Bal, PnL, Trades, Logs.

## StatusManager - Fixed API

This was the cause of earlier build failures. Ensure `StatusManager.kt` has:
fun isRunning(c: Context): Boolean
fun getTrades(c: Context): Int
fun getPnl(c: Context): Double
fun getBalance(c: Context): Double
fun getLogs(c: Context): String
fun getModules(c: Context): String
fun setModules(c: Context, mods: String)
fun setRunning(c: Context, r: Boolean)
fun setBalance(c: Context, b: Double)
fun appendLog(c: Context, msg: String)
fun addDailyLoss(c: Context, loss: Double)
## Common Build Fixes

*Error: Unresolved reference 'isRunning' etc.*
-> Update http://StatusManager.kt to include missing methods (see above)

*Error: 'fun parse' is deprecated*
-> In http://JupiterService.kt:
// Old: MediaType.parse("application/json")
// New:
import okhttp3.MediaType.Companion.toMediaType
RequestBody.create("application/json".toMediaType(), bodyJson.toString())
*Error: Unresolved reference 'WSOL'*
-> Use `JupiterService.WSOL_MINT` not `WSOL`

## Security Checklist

- [ ] `local.properties` in `.gitignore`
- [ ] Private key NOT in git history (`git log -p | grep -i private`)
- [ ] Repo set to Private on GitHub
- [ ] APK not shared publicly
- [ ] RPC uses private QuickNode/Helius URL (not public)

## Roadmap

- [ ] Add Biometric lock for START/STOP
- [ ] Add auto-restart on boot (BOOT_COMPLETED receiver)
- [ ] Add Telegram alerts for trades
- [ ] Add SQLite for trade history instead of SharedPrefs

## Disclaimer

This is for educational purposes. Crypto trading is risky. You can lose all funds. Use at your own risk. Test with small amounts (0.05 SOL) first.

---
SHIV Executor - Built for 24/7 AMOLED

Want me to push these directly to both repos for you? Just run:

```bash
cd ~/shiv-private-executor
# paste the second README
nano README.md
git add README.md && git commit -m "docs: complete readme" && git push
I can also generate the fixed `StatusManager.kt` file now so your next APK build passes - want that?
