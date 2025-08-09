# Test Suite Fixing Plan

## Overview
The test suite currently contains **36 test files** with **3 disabled files** and multiple compilation/runtime issues. This plan provides a systematic approach to fix all test-related problems and establish a robust, maintainable test infrastructure.

## Current Issues Analysis

### **Issue Categories:**

#### 1. **Mock Framework Inconsistency**
- **Problem**: Mixed usage of Mockito and MockK frameworks across different test files
- **Impact**: Import conflicts, different API usage patterns, inconsistent mocking syntax
- **Files Affected**: ~25+ test files
- **Examples**:
  ```kotlin
  // Some tests use Mockito
  @Mock private lateinit var mockRepository: TrackerRepository
  
  // Others use MockK
  @MockK private lateinit var repository: TrackerRepository
  ```

#### 2. **Disabled Test Files with Compilation Errors**
- **Problem**: 3 test files disabled due to data model mismatches and suspend function issues
- **Files**:
  - `DataExportUseCaseTest.kt.disabled` - Suspend function mocking issues
  - `EnhancedWeeklyInsightsUseCaseTest.kt.disabled` - Data model constructor mismatches
  - `EnhancedWeeklyInsightsCardTest.kt.disabled` - UI testing dependency issues

#### 3. **Data Model Constructor Mismatches**
- **Problem**: Test files reference old constructor signatures for domain models
- **Impact**: Compilation failures due to missing/changed parameters
- **Common Issues**:
  - `WellnessScore` constructor changes (missing `breaksScore`, `sleepHygieneScore`, `level`, `calculatedAt`)
  - `Goal` vs `UserGoal` naming conflicts
  - `SessionData` parameter mismatches

#### 4. **Coroutine Testing Setup Issues**
- **Problem**: Inconsistent use of `runTest`, `coEvery`, `coVerify` across test files
- **Impact**: Tests may not properly handle suspend functions or coroutine contexts

#### 5. **Missing Test Dependencies**
- **Problem**: Some tests missing proper test dependencies for UI testing, coroutines, or specific frameworks
- **Impact**: Compilation or runtime failures

## **Fixing Strategy**

### **Phase 1: Standardize Mock Framework (Priority: HIGH)**

#### **1.1 Choose Primary Mock Framework**
**Decision: Standardize on MockK**
- **Rationale**: Better Kotlin support, more intuitive syntax, better coroutine support
- **Migration**: Convert all Mockito usage to MockK

#### **1.2 Update Dependencies**
```kotlin
// Remove from build.gradle
testImplementation 'org.mockito:mockito-core:5.18.0'
testImplementation "org.mockito:mockito-inline:5.2.0"
testImplementation "org.mockito.kotlin:mockito-kotlin:6.0.0"

// Keep only MockK
testImplementation "io.mockk:mockk:1.13.14"
testImplementation "io.mockk:mockk-android:1.13.14"
```

#### **1.3 Migration Pattern**
```kotlin
// OLD (Mockito)
@Mock private lateinit var mockRepository: TrackerRepository
MockitoAnnotations.openMocks(this)
whenever(mockRepository.getUser()).thenReturn(user)
verify(mockRepository).insertUser(user)

// NEW (MockK)
@MockK private lateinit var mockRepository: TrackerRepository
MockKAnnotations.init(this, relaxUnitFun = true)
every { mockRepository.getUser() } returns user
verify { mockRepository.insertUser(user) }

// Suspend functions
coEvery { mockRepository.suspendFunction() } returns result
coVerify { mockRepository.suspendFunction() }
```

### **Phase 2: Fix Data Model Issues (Priority: HIGH)**

#### **2.1 Update WellnessScore Usage**
```kotlin
// OLD
WellnessScore(
    date = startOfDay,
    totalScore = 85,
    timeLimitScore = 90,
    focusSessionScore = 80
)

// NEW (check current constructor)
WellnessScore(
    id = 1,
    score = 85,
    calculatedAt = System.currentTimeMillis(),
    dayStartMillis = startOfDay,
    // Add any missing required parameters
)
```

#### **2.2 Fix Goal/UserGoal References**
- Verify which model is currently used in the codebase
- Update all test references consistently

#### **2.3 Update All Constructor Calls**
- Review each domain model's current constructor
- Update test files to match current signatures

### **Phase 3: Re-enable Disabled Tests (Priority: HIGH)**

#### **3.1 Fix DataExportUseCaseTest**
```kotlin
// Fix suspend function mocking
// OLD
every { mockDao.getAllAppUsageEventsForExport() } returns data

// NEW  
coEvery { mockDao.getAllAppUsageEventsForExport() } returns data
```

#### **3.2 Fix Enhanced Weekly Insights Tests**
- Update data model constructors
- Fix UI testing dependencies
- Ensure proper coroutine testing setup

### **Phase 4: Standardize Test Structure (Priority: MEDIUM)**

#### **4.1 Standard Test Template**
```kotlin
@RunWith(RobolectricTestRunner::class) // If needed for Android components
class ExampleUseCaseTest {

    @MockK
    private lateinit var repository: TrackerRepository
    
    @MockK
    private lateinit var logger: AppLogger
    
    private lateinit var useCase: ExampleUseCase
    
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        
        // Standard logger setup
        every { logger.i(any(), any()) } just runs
        every { logger.e(any(), any(), any()) } just runs
        
        useCase = ExampleUseCase(repository, logger)
    }
    
    @Test
    fun `test description in backticks`() = runTest {
        // Given
        val expected = createTestData()
        coEvery { repository.getData() } returns expected
        
        // When  
        val result = useCase.execute()
        
        // Then
        assertEquals(expected, result)
        coVerify { repository.getData() }
    }
}
```

### **Phase 5: Improve Test Coverage (Priority: MEDIUM)**

#### **5.1 Add Missing Test Categories**
- **Error Scenarios**: Network failures, database errors, invalid input
- **Edge Cases**: Empty data, boundary values, concurrent access
- **Integration Points**: Cross-feature interactions

#### **5.2 Add Performance Assertions**
```kotlin
@Test
fun `operation should complete within performance threshold`() = runTest {
    val executionTime = measureTimeMillis {
        useCase.performExpensiveOperation()
    }
    assertTrue("Operation took $executionTime ms", executionTime < 1000)
}
```

### **Phase 6: Test Infrastructure Improvements (Priority: LOW)**

#### **6.1 Test Utilities**
```kotlin
// Create TestDataFactory
object TestDataFactory {
    fun createWellnessScore(score: Int = 75) = WellnessScore(
        id = Random.nextLong(),
        score = score,
        calculatedAt = System.currentTimeMillis(),
        dayStartMillis = System.currentTimeMillis()
    )
    
    fun createTimeRestriction(packageName: String = "com.test.app") = TimeRestriction(
        // ... with sensible defaults
    )
}
```

#### **6.2 Base Test Classes**
```kotlin
abstract class BaseUseCaseTest {
    @MockK protected lateinit var logger: AppLogger
    
    @Before
    fun baseSetup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        setupLogger()
    }
    
    protected fun setupLogger() {
        every { logger.i(any(), any()) } just runs
        every { logger.e(any(), any(), any()) } just runs
        every { logger.d(any(), any()) } just runs
        every { logger.w(any(), any(), any()) } just runs
    }
}
```

## **Implementation Plan**

### **Week 1: Foundation Fixes**
1. **Day 1-2**: Standardize mock framework (convert all files)
2. **Day 3-4**: Fix data model constructor issues
3. **Day 5**: Re-enable and fix disabled tests

### **Week 2: Structure & Coverage**
1. **Day 1-2**: Standardize test structure across all files
2. **Day 3-4**: Add missing error scenario tests
3. **Day 5**: Create test utilities and base classes

### **Week 3: Validation & Polish**
1. **Day 1-2**: Run full test suite and fix remaining issues
2. **Day 3-4**: Add performance tests where needed
3. **Day 5**: Documentation and test maintenance guidelines

## **Success Criteria**

### **Must Have:**
- [ ] All 36 test files compile successfully
- [ ] All disabled tests re-enabled and passing
- [ ] Consistent mock framework usage (100% MockK)
- [ ] Zero data model constructor mismatches
- [ ] `./gradlew test` runs without compilation errors

### **Should Have:**
- [ ] 90%+ test success rate
- [ ] Consistent test structure across all files
- [ ] Error scenario coverage for all use cases
- [ ] Performance tests for critical operations

### **Nice to Have:**
- [ ] Test utilities for common operations
- [ ] Base test classes for common patterns
- [ ] Automated test quality checks
- [ ] Test maintenance documentation

## **Risk Mitigation**

1. **Breaking Changes**: Work on feature branch, frequent commits
2. **Time Overrun**: Focus on must-have criteria first
3. **New Issues**: Document and prioritize new findings
4. **Regression**: Maintain working test backup before changes

## **Files to Modify**

### **High Priority (Week 1):**
```
- All 36 test files for mock framework standardization
- DataExportUseCaseTest.kt.disabled (re-enable)
- EnhancedWeeklyInsightsUseCaseTest.kt.disabled (re-enable)  
- EnhancedWeeklyInsightsCardTest.kt.disabled (re-enable)
- app/build.gradle (dependencies)
```

### **Medium Priority (Week 2):**
```
- Create TestDataFactory.kt
- Create BaseUseCaseTest.kt
- Update all test files with standardized structure
```

### **Low Priority (Week 3):**
```
- Add TEST_MAINTENANCE.md documentation
- Create automated test validation scripts
```

## **Expected Outcomes**

After implementing this plan:

1. **Reliable CI/CD**: Tests run consistently without flaky failures
2. **Developer Productivity**: Easy to write and maintain tests
3. **Code Quality**: Better test coverage catches issues early
4. **Documentation**: Clear patterns for future test development
5. **Maintainability**: Standardized structure makes updates easier

This plan transforms the current problematic test suite into a robust, maintainable foundation for ongoing development.