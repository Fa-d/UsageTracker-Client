```mermaid
erDiagram
    ScreenUnlockEvent {
        long id PK
        long timestamp
    }

    AppUsageEvent {
        long id PK
        string packageName
        string eventName
        long timestamp
    }

    AppSessionEvent {
        long id PK
        string packageName
        long startTimeMillis
        long endTimeMillis
        long durationMillis
    }

    DailyAppSummary {
        long dateMillis PK
        string packageName PK
        long totalDurationMillis
        int openCount
    }

    DailyScreenUnlockSummary {
        long dateMillis PK
        int unlockCount
    }

    LimitedApp {
        string packageName PK
        long timeLimitMillis
    }

    AppSessionEvent ||--o{ DailyAppSummary : "aggregates"
    ScreenUnlockEvent ||--o{ DailyScreenUnlockSummary : "aggregates"
    LimitedApp ||--o{ AppSessionEvent : "limits"
    LimitedApp ||--o{ AppUsageEvent : "limits"
```

**Note:** The relationships shown above are conceptual based on how data is processed and aggregated within the application. Room (the ORM used) does not explicitly define foreign key constraints in the entity classes themselves for these relationships, but rather manages them through queries and data processing logic.