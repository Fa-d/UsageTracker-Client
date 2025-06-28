```mermaid
graph TD
    A[Start AppUsageTrackingService] --> B{Service Created?}
    B -- Yes --> C[onCreate]
    C --> D[Initialize CoroutineScope]
    C --> E[Create Notification Channels]
    C --> F[Load Limited App Settings -from DB]

    F --> G[repository.getAllLimitedAppsOnce]
    G -- Reads LimitedApp data --> DB[(AppDatabase)]

    D --> H[onStartCommand]
    H --> I[Start Foreground Service]
    H --> J{Intent Action?}

    J -- ACTION_HANDLE_SCREEN_OFF --> K[handleScreenOff]
    K --> L[finalizeCurrentSession]

    J -- ACTION_RELOAD_LIMIT_SETTINGS --> F

    J -- Other Default --> M[Start App Usage Polling Loop]
    M --> N{Loop Active?}
    N -- Yes --> O[pollAppUsage]

    O --> P[Query UsageStatsManager for Events]
    P --> Q{New Foreground App?}

    Q -- Yes --> R[finalizeCurrentSession -old app]
    R --> S[recordAppSessionUseCase]
    S -- Inserts AppSessionEvent --> DB

    Q -- Yes --> T[startNewSession - new app]
    T --> U{App is Limited?}
    U -- Yes --> V[Set currentLimitedAppDetails]

    O --> W{Usage Limit Exceeded?}
    W -- Yes 1x --> X[Show Warning Notification]
    W -- Yes 3x --> Y[Execute Dissuasion Action]

    L --> S

    Z[Service Destroyed] --> AA[onDestroy]
    AA --> L
    AA --> BB[Cancel CoroutineScope]

    style DB fill:#f9f,stroke:#333,stroke-width:2px
```

**Explanation:**
This diagram illustrates the main flow of the `AppUsageTrackingService`.
- The service starts and initializes, loading app limit settings from the database.
- It then enters a continuous polling loop to monitor foreground app changes.
- When an app changes or the screen turns off, the `finalizeCurrentSession` function is called to save the previous app's session data to the database.
- If the currently used app is a limited app, the service checks its continuous usage against the set limits and triggers notifications or dissuasion actions as needed.
- The service interacts with the `AppDatabase` primarily through the `TrackerRepository` to `getAllLimitedAppsOnce()` and `recordAppSessionUseCase()`.