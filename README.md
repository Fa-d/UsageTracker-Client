# Screen Time Tracker - Kotlin Multiplatform

## Project Structure

The project has been refactored to use Kotlin Multiplatform (KMP) with Compose Multiplatform (CMP) for cross-platform development.

### Modules

- **`shared/`** - Common multiplatform code
  - Business logic, data models, repositories
  - Shared Compose UI components
  - Platform-specific implementations via `expect`/`actual`

- **`app/`** - Android application
  - Android-specific implementations
  - Integrates with shared module

- **`desktop/`** - Desktop application (Windows, macOS, Linux)
  - Compose Desktop implementation
  - Uses shared business logic and UI

- **`ios/`** - iOS application project
  - Xcode project configuration
  - Will integrate with shared module via Kotlin/Native

### Building

#### Android
```bash
./gradlew app:assembleDebug
```

#### Desktop
```bash
./gradlew desktop:run
./gradlew desktop:packageDistributionForCurrentOS  # Create distributable
```

#### Shared Module Only
```bash
./gradlew shared:build
```

### Platform Capabilities

| Feature | Android | iOS | Desktop | Web |
|---------|---------|-----|---------|-----|
| Core Analytics | ✅ | ✅ | ✅ | ❌ |
| Usage Tracking | ✅ (Full) | 🔶 (Limited) | 🔶 (Basic) | ❌ |
| Content Blocking | ✅ | ❌ | ❌ | ❌ |
| Notifications | ✅ | ✅ | ✅ | ❌ |
| Background Sync | ✅ | 🔶 | 🔶 | ❌ |

### Dependencies

- **Kotlin Multiplatform**: 2.1.20
- **Compose Multiplatform**: 1.7.5
- **Ktor**: Network client
- **SQLDelight**: Database
- **Koin**: Dependency injection
- **kotlinx.datetime**: Date/time handling
- **kotlinx.serialization**: JSON serialization

### Migration Notes

1. **Shared Logic**: ~70% of business logic moved to common module
2. **UI Components**: Core Compose components are now shared
3. **Platform-Specific**: Usage tracking implementations per platform
4. **Data Layer**: Repository pattern with platform-specific drivers

### Next Steps

1. **iOS Integration**: Add KMP framework to iOS project
2. **Desktop Enhancement**: Implement native system monitoring
3. **Web Support**: Add Compose for Web target
4. **Testing**: Add shared unit tests and UI tests