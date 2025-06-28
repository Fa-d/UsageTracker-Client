```mermaid
graph TD
    subgraph UI Layer
        A[MainActivity] --> B(DashboardScreen)
        A --> C(LimiterConfigScreen)
        B --> D[DashboardViewModel]
        C --> E[LimiterConfigViewModel]
    end

    subgraph Domain Layer
        F[Use Cases]
        G[TrackerRepository Interface]
        D --> F
        E --> F
        F --> G
    end

    subgraph Data Layer
        H[TrackerRepositoryImpl]
        I[Room Database]
        J[DAOs]
        K[Entities]
        G --> H
        H --> I
        I --> J
        J --> K
    end

    subgraph Background Processes
        L[AppUsageTrackingService]
        M[DailyAggregationWorker]
        N[BootCompletedReceiver]
        O[ScreenStateReceiver]
        P[ScreenUnlockReceiver]
    end

    subgraph Dependency Injection (Hilt)
        Q[DatabaseModule]
        R[DomainModule]
        S[ServiceModule]
    end

    subgraph Utilities
        T[PermissionUtils]
    end

    F --> H
    L --> F
    L --> H
    M --> F
    M --> H

    N --> L
    O --> L
    P --> F

    Q --> I
    Q --> J
    R --> F
    S --> L

    A --> T
    L --> T
```