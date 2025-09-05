# 📈 Trading Stocks – Dual Provider Quotes

> Android app that displays the latest stock price from **two providers** (Alpha Vantage & Twelve Data) side by side.
> I’m exploring a clean architecture setup where data providers can be swapped freely—without touching the presentation or domain layers—so the app stays modular, testable, and easy to evolve.

---

## 🔹 About / O aplikaci

**EN:** Android app built with **Jetpack Compose**, **Kotlin Coroutines**, **Koin DI**, and a shared **Retrofit + OkHttp + Moshi** network layer. Type a ticker (e.g., `AAPL`) in the Home screen to open the Stocks screen with two independent quotes. Twelve Data also displays company name, previous close, and change.

---

## 🧭 Architecture

- **Modules**
  - `core:network` – shared OkHttp/Retrofit/Moshi, interceptors (HttpLogging, Chucker optional)
  - `domain:marketdata` – `Price`, `MarketDataRepository`, use cases
  - `data:marketdata-alpha` – Alpha Vantage API + repository
  - `data:marketdata-twelvedata` – Twelve Data API + repository
  - `feature:home` – search UI (query & submit)
  - `feature:stocks` – card with two provider rows

- **Data flow**
  - `HomeScreen` → collects query & submit → navigates to `StocksRoute(ticker)`
  - `StocksViewModel` launches two use cases in parallel (Alpha & Twelve)
  - Results are mapped to UI model **`DisplayPrice`** (dates `dd.MM.yyyy`)

- **Per-provider UI state**
  - Each provider has its own UI state (loading / data / error), so one failure doesn’t block the other.

---

## 🔧 Tech Stack

- **Language /** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Async:** Coroutines, StateFlow
- **DI:** Koin
- **Network:** Retrofit, OkHttp, Moshi (+ HttpLoggingInterceptor, Chucker optional)
- **Testing:** JUnit5, kotlinx-coroutines-test, Turbine, MockK

---

## 🚀 Run

1. **API keys   
   Put your keys into `local.properties` or environment variables:

   ```properties
   # local.properties
   API_KEY=your_alpha_vantage_key
   TWELVE_API_KEY=your_twelve_data_key
   ```

2. **Build variants / Varianty buildů**  
   - `dev` / `prod` flavors provide `BASE_URI` & `API_KEY` via `BuildConfig` in each data module.

3. **Gradle sync & run  
   - Sync the project in Android Studio and run the **app** configuration.

---

## 🧩 Dependency Injection (Koin)

Koin modules are loaded in `app`:

```kotlin
startKoin {
    androidContext(this@App)
    modules(
        networkModule,                 // core:network
        marketDataAlphaNetworkModule,  // data:marketdata-alpha
        marketDataAlphaModule,
        twelveNetworkModule,           // data:marketdata-twelvedata
        twelveModule,
        homeModule,                    // feature:home
        stocksFeatureModule            // feature:stocks
    )
}
```

- Provider modules create their own `Retrofit` with shared `OkHttpClient` and `Converter.Factory` from `core:network`.
- API keys and base URLs come from each module’s `BuildConfig` fields.

---

## 🧪 Tests 

- **ViewModel tests** use `kotlinx.coroutines.test` and **Turbine** to assert state emissions.
- **MockK** stubs Android `Log` in JVM tests.
- Example scenarios:
  - both providers succeed → two data rows
  - one fails → one data row + one error row
  - both fail → two error rows

---

## 🗺️ Screens 

- **Home** – gradient header, **TopSearchBar** with query, search & clear actions, placeholder card
- **Stocks** – single **ElevatedCard** with:
  - Alpha Vantage row (price)
  - Twelve Data row (price + company name + previous close + change with ▲/▼ and colors)
  - per-row loading & error states

---

## 📦 Networking Notes

- OkHttp client has timeouts and interceptors:
  - API interceptor (placeholder)
  - HttpLoggingInterceptor (BODY in debug)
  - Chucker (optional)
- Moshi is configured globally; DTOs use Moshi annotations where needed.

---
