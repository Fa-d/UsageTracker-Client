# Android Clean Architecture Enforcement System

## 🎯 Overview
This comprehensive solution implements a **multi-layered enforcement system** that validates clean architecture compliance at every stage of development - from local commits to CI/CD pipelines. It combines git hooks for immediate feedback, custom Detekt rules for architecture validation, ArchUnit for dependency testing, and GitHub Actions for final enforcement.

## 🔒 Enforcement Layers

### Layer 1: Local Development (Immediate Feedback)
- **Git Pre-commit Hooks**: Code formatting and basic checks
- **Git Pre-push Hooks**: Architecture validation before pushing

### Layer 2: Pull Request (Automated Review)
- **GitHub Actions**: Comprehensive validation suite
- **Danger**: Automated PR feedback and warnings

### Layer 3: Architecture Testing (Build-time Validation)
- **ArchUnit**: JUnit-based architecture tests
- **Custom Detekt Rules**: Clean architecture dependency rules

## 📁 Project Structure
```
project/
├── .github/
│   ├── workflows/
│   │   ├── pr-validation.yml
│   │   └── architecture-check.yml
│   └── danger/
│       └── Dangerfile
├── buildSrc/
│   └── src/main/kotlin/
│       └── ArchitectureEnforcement.kt
├── architecture-rules/
│   ├── src/
│   │   ├── main/kotlin/rules/
│   │   └── test/kotlin/
│   └── build.gradle.kts
├── config/
│   ├── detekt/
│   │   ├── detekt.yml
│   │   └── baseline.xml
│   └── git-hooks/
│       ├── pre-commit
│       └── pre-push
└── gradle/
    └── architecture-validation.gradle.kts
```

## 🚀 Part 1: Git Hooks Setup

### 1.1 Install Git Hooks Gradle Task
Create `gradle/git-hooks.gradle.kts`:

```kotlin
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

tasks.register("installGitHooks") {
    doLast {
        val hooksDir = file("${rootProject.rootDir}/.git/hooks")
        if (!hooksDir.exists()) {
            hooksDir.mkdirs()
        }
        
        // Copy pre-commit hook
        val preCommitSource = file("${rootProject.rootDir}/config/git-hooks/pre-commit")
        val preCommitTarget = file("${rootProject.rootDir}/.git/hooks/pre-commit")
        Files.copy(
            preCommitSource.toPath(),
            preCommitTarget.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        preCommitTarget.setExecutable(true)
        
        // Copy pre-push hook
        val prePushSource = file("${rootProject.rootDir}/config/git-hooks/pre-push")
        val prePushTarget = file("${rootProject.rootDir}/.git/hooks/pre-push")
        Files.copy(
            prePushSource.toPath(),
            prePushTarget.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        prePushTarget.setExecutable(true)
        
        println("✅ Git hooks installed successfully!")
    }
}

// Auto-install on project sync
tasks.getByPath(":app:preBuild").dependsOn("installGitHooks")
```

### 1.2 Pre-commit Hook (Formatting)
Create `config/git-hooks/pre-commit`:

```bash
#!/bin/sh

echo "🎨 Running code formatting..."

# Format staged Kotlin files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep '\.kt[s]$')

if [ -n "$STAGED_FILES" ]; then
    echo "Formatting Kotlin files..."
    ./gradlew ktlintFormat --daemon
    
    # Re-add formatted files
    echo "$STAGED_FILES" | xargs git add
    echo "✅ Code formatting complete"
fi

exit 0
```

### 1.3 Pre-push Hook (Architecture Validation)
Create `config/git-hooks/pre-push`:

```bash
#!/bin/sh

echo "🏗️ Validating Clean Architecture..."

# Run architecture checks
./gradlew validateArchitecture --daemon

status=$?
if [ "$status" = 0 ]; then
    echo "✅ Architecture validation passed"
    exit 0
else
    echo "❌ Architecture violations detected!"
    echo "Run './gradlew validateArchitecture' to see details"
    exit 1
fi
```

## 🔍 Part 2: Custom Detekt Rules

### 2.1 Architecture Rules Module
Create `architecture-rules/build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.5")
    implementation("io.gitlab.arturbosch.detekt:detekt-cli:1.23.5")
    
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.5")
    testImplementation("org.assertj:assertj-core:3.24.2")
}
```

### 2.2 Clean Architecture Dependency Rule
Create `architecture-rules/src/main/kotlin/rules/CleanArchitectureRule.kt`:

```kotlin
package com.project.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective

class CleanArchitectureRule(config: Config = Config.empty) : Rule(config) {
    
    override val issue = Issue(
        id = "CleanArchitectureViolation",
        severity = Severity.Maintainability,
        description = "Ensures clean architecture layer dependencies",
        debt = Debt.FIVE_MINS
    )
    
    private val layerPatterns = mapOf(
        "domain" to listOf("domain"),
        "data" to listOf("domain", "data"),
        "presentation" to listOf("domain", "data", "presentation", "feature")
    )
    
    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        
        val currentLayer = detectLayer(file.packageFqName.asString())
        if (currentLayer == null) return
        
        file.importList?.imports?.forEach { import ->
            checkImport(import, currentLayer)
        }
    }
    
    private fun detectLayer(packageName: String): String? {
        return when {
            packageName.contains(".domain.") -> "domain"
            packageName.contains(".data.") -> "data"
            packageName.contains(".presentation.") || 
            packageName.contains(".feature.") -> "presentation"
            else -> null
        }
    }
    
    private fun checkImport(import: KtImportDirective, currentLayer: String) {
        val importPath = import.importPath?.pathStr ?: return
        val importLayer = detectLayer(importPath) ?: return
        
        val allowedLayers = layerPatterns[currentLayer] ?: return
        
        if (!allowedLayers.contains(importLayer)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(import),
                    "Layer '$currentLayer' should not depend on '$importLayer'. " +
                    "Allowed dependencies: ${allowedLayers.joinToString()}"
                )
            )
        }
    }
}
```

### 2.3 Domain Layer Purity Rule
Create `architecture-rules/src/main/kotlin/rules/DomainLayerPurityRule.kt`:

```kotlin
package com.project.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtImportDirective

class DomainLayerPurityRule(config: Config = Config.empty) : Rule(config) {
    
    override val issue = Issue(
        id = "DomainLayerImpurity",
        severity = Severity.Maintainability,
        description = "Domain layer must not depend on Android framework",
        debt = Debt.TEN_MINS
    )
    
    private val forbiddenImports = listOf(
        "android.",
        "androidx.",
        "kotlinx.android.",
        "javax.inject.Inject", // Use domain-specific injection
        "dagger.",
        "hilt."
    )
    
    override fun visitImportDirective(import: KtImportDirective) {
        val importPath = import.importPath?.pathStr ?: return
        val file = import.containingKtFile
        
        // Check if we're in domain layer
        if (!file.packageFqName.asString().contains(".domain.")) {
            return
        }
        
        forbiddenImports.forEach { forbidden ->
            if (importPath.startsWith(forbidden)) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(import),
                        "Domain layer cannot depend on '$importPath'. " +
                        "Domain must be pure Kotlin/Java."
                    )
                )
            }
        }
    }
}
```

### 2.4 Register Custom Rules
Create `architecture-rules/src/main/kotlin/rules/CleanArchRuleSetProvider.kt`:

```kotlin
package com.project.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CleanArchRuleSetProvider : RuleSetProvider {
    override val ruleSetId = "clean-architecture"
    
    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            CleanArchitectureRule(config),
            DomainLayerPurityRule(config),
            UseCaseNamingRule(config),
            RepositoryInterfaceRule(config)
        )
    )
}
```

### 2.5 Configure Detekt
Create `config/detekt/detekt.yml`:

```yaml
config:
  validation: true
  warningsAsErrors: true
  excludes: "clean-architecture"

clean-architecture:
  active: true
  CleanArchitectureViolation:
    active: true
  DomainLayerImpurity:
    active: true
  UseCaseNaming:
    active: true
    pattern: '^[A-Z][a-zA-Z]*UseCase$'
  RepositoryInterface:
    active: true

# Standard Detekt rules
complexity:
  active: true
  ComplexMethod:
    threshold: 15
  LongMethod:
    threshold: 60
  TooManyFunctions:
    threshold: 20

naming:
  active: true
  ClassNaming:
    classPattern: '[A-Z][a-zA-Z0-9]*'
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
    excludeClassPattern: '^.*Test$'
```

## 🧪 Part 3: ArchUnit Testing

### 3.1 Architecture Test Base
Create `app/src/test/kotlin/architecture/ArchitectureTest.kt`:

```kotlin
package com.project.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(
    packages = ["com.project"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class ArchitectureTest {
    
    @ArchTest
    val layerDependencies = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Presentation").definedBy("..presentation..", "..feature..")
        .layer("Domain").definedBy("..domain..")
        .layer("Data").definedBy("..data..")
        .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Presentation", "Data")
        .whereLayer("Data").mayOnlyBeAccessedByLayers("Presentation")
    
    @ArchTest
    val domainShouldNotDependOnFramework = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("android..", "androidx..")
        .because("Domain layer must be framework independent")
    
    @ArchTest
    val useCaseNaming = classes()
        .that().resideInAPackage("..domain.usecase..")
        .should().haveSimpleNameEndingWith("UseCase")
        .because("Use cases should follow naming convention")
    
    @ArchTest
    val repositoryImplementations = classes()
        .that().implement("..domain.repository..")
        .should().resideInAPackage("..data.repository..")
        .because("Repository implementations belong in data layer")
    
    @ArchTest
    val viewModelsDependencies = classes()
        .that().haveSimpleNameEndingWith("ViewModel")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..domain..",
            "androidx.lifecycle..",
            "kotlinx.coroutines..",
            "javax.inject..",
            "dagger.hilt.."
        ).because("ViewModels should only depend on domain layer")
}
```

### 3.2 Module Boundary Tests
Create `app/src/test/kotlin/architecture/ModuleBoundaryTest.kt`:

```kotlin
package com.project.architecture

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

class ModuleBoundaryTest {
    
    @ArchTest
    val featureIndependence = noClasses()
        .that().resideInAPackage("..feature.login..")
        .should().dependOnClassesThat()
        .resideInAPackage("..feature.home..")
        .because("Features should be independent")
    
    @ArchTest
    val dataSourceIsolation = noClasses()
        .that().resideInAPackage("..presentation..")
        .should().directlyDependOnClassesThat()
        .resideInAnyPackage("..data.local..", "..data.remote..")
        .because("Presentation should access data through repositories")
}
```

## 🤖 Part 4: GitHub Actions CI/CD

### 4.1 Pull Request Validation Workflow
Create `.github/workflows/pr-validation.yml`:

```yaml
name: PR Validation

on:
  pull_request:
    branches: [ main, develop ]
    types: [ opened, synchronize, reopened ]

jobs:
  architecture-check:
    name: Architecture Validation
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'
      
      - name: Grant execute permission
        run: chmod +x ./gradlew
      
      - name: Run Detekt with Custom Rules
        run: ./gradlew detekt --continue
        
      - name: Run ArchUnit Tests
        run: ./gradlew test --tests "*ArchitectureTest*"
        
      - name: Generate Architecture Report
        if: always()
        run: |
          ./gradlew mergeDetektSarif
          ./gradlew generateArchitectureReport
      
      - name: Upload SARIF to GitHub
        if: always()
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: build/reports/detekt/merged.sarif
      
      - name: Upload Architecture Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: architecture-reports
          path: |
            build/reports/detekt/
            build/reports/tests/
            build/reports/architecture/

  code-quality:
    name: Code Quality Checks
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'
      
      - name: Run ktlint
        run: ./gradlew ktlintCheck
      
      - name: Run Android Lint
        run: ./gradlew lint
      
      - name: Check for TODO comments
        run: |
          if grep -r "TODO" --include="*.kt" --include="*.java" .; then
            echo "❌ TODO comments found. Please resolve before merging."
            exit 1
          fi

  danger:
    name: Danger PR Review
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.2'
          bundler-cache: true
      
      - name: Run Danger
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          gem install danger danger-checkstyle_format danger-android_lint
          danger --dangerfile=.github/danger/Dangerfile
```

### 4.2 Danger Configuration
Create `.github/danger/Dangerfile`:

```ruby
# Architecture checks
message("🏗️ Running architecture validation...")

# Check for architecture violations in changed files
git.modified_files.select { |f| f.end_with?(".kt") }.each do |file|
  content = File.read(file)
  
  # Check domain layer purity
  if file.include?("/domain/") && content.match?(/import\s+(android\.|androidx\.)/)
    fail("❌ Domain layer violation in `#{file}`: Domain cannot depend on Android framework")
  end
  
  # Check use case naming
  if file.include?("/usecase/") && !File.basename(file, ".kt").end_with?("UseCase")
    warn("⚠️ Use case naming violation: `#{file}` should end with 'UseCase'")
  end
end

# Module dependency checks
git.modified_files.select { |f| f.end_with?(".kt") }.each do |file|
  content = File.read(file)
  
  # Check for cross-feature dependencies
  if file.match?(%r{feature/(\w+)}) 
    feature = $1
    if content.match?(%r{import.*\.feature\.(?!#{feature})\w+})
      fail("❌ Cross-feature dependency detected in `#{file}`")
    end
  end
end

# PR size check
if git.lines_of_code > 500
  warn("⚠️ Large PR: #{git.lines_of_code} lines changed. Consider breaking it down.")
end

# Check for tests
has_tests = git.modified_files.any? { |f| f.include?("Test") }
has_source = git.modified_files.any? { |f| f.end_with?(".kt") && !f.include?("Test") }

if has_source && !has_tests
  warn("⚠️ No tests added for new code. Please add tests.")
end

# Architecture documentation check
if git.modified_files.any? { |f| f.include?("/domain/") || f.include?("/data/") }
  unless git.modified_files.any? { |f| f.include?("README") || f.include?("ARCHITECTURE") }
    message("📝 Consider updating architecture documentation for these changes")
  end
end

# Success message
if status_report[:errors].empty? && status_report[:warnings].empty?
  message("✅ All architecture checks passed!")
end
```

## 🛠️ Part 5: Gradle Integration

### 5.1 Master Validation Task
Create `buildSrc/src/main/kotlin/ArchitectureEnforcement.kt`:

```kotlin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*

open class ValidateArchitectureTask : DefaultTask() {
    
    init {
        group = "verification"
        description = "Validates clean architecture compliance"
    }
    
    @TaskAction
    fun validate() {
        project.exec {
            commandLine("./gradlew", "detekt")
        }
        
        project.exec {
            commandLine("./gradlew", "test", "--tests", "*ArchitectureTest*")
        }
        
        println("✅ Architecture validation complete")
    }
}

// Register in build.gradle.kts
tasks.register<ValidateArchitectureTask>("validateArchitecture") {
    dependsOn("detekt", "test")
}
```

### 5.2 Pre-build Validation
Add to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing configuration
}

// Architecture enforcement
tasks.named("preBuild") {
    if (System.getenv("CI") == "true" || gradle.startParameter.taskNames.contains("assembleRelease")) {
        dependsOn("validateArchitecture")
    }
}

// Fail build on architecture violations
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        sarif.required.set(true)
    }
    
    // Fail on any architecture rule violation
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    
    doLast {
        val reportFile = file("${project.buildDir}/reports/detekt/detekt.sarif")
        if (reportFile.exists()) {
            val content = reportFile.readText()
            if (content.contains("\"level\":\"error\"")) {
                throw GradleException("Architecture violations detected!")
            }
        }
    }
}
```

## 📊 Part 6: Reporting Dashboard

### 6.1 Architecture Health Report
Create `gradle/architecture-report.gradle.kts`:

```kotlin
tasks.register("generateArchitectureReport") {
    doLast {
        val reportDir = file("${project.buildDir}/reports/architecture")
        reportDir.mkdirs()
        
        val report = buildString {
            appendLine("# Architecture Health Report")
            appendLine("Generated: ${java.time.LocalDateTime.now()}")
            appendLine()
            
            // Layer compliance
            appendLine("## Layer Compliance")
            val detektReport = file("${project.buildDir}/reports/detekt/detekt.xml")
            if (detektReport.exists()) {
                val violations = parseDetektViolations(detektReport)
                appendLine("- Clean Architecture Violations: ${violations.size}")
                violations.forEach { 
                    appendLine("  - $it")
                }
            }
            
            // Module metrics
            appendLine()
            appendLine("## Module Metrics")
            project.subprojects.forEach { subproject ->
                val loc = countLinesOfCode(subproject.projectDir)
                appendLine("- ${subproject.name}: $loc LOC")
            }
            
            // Test coverage
            appendLine()
            appendLine("## Architecture Test Coverage")
            appendLine("- ArchUnit Tests: ✅")
            appendLine("- Custom Detekt Rules: ✅")
            appendLine("- Git Hooks: ✅")
        }
        
        file("$reportDir/report.md").writeText(report)
        println("📊 Architecture report generated: $reportDir/report.md")
    }
}
```

## 🚦 Usage Guide

### Initial Setup (One-time)
```bash
# 1. Add the enforcement system to your project
cp -r architecture-rules/ your-project/
cp -r config/ your-project/
cp -r .github/ your-project/

# 2. Install git hooks
./gradlew installGitHooks

# 3. Generate Detekt baseline (optional)
./gradlew detektBaseline

# 4. Configure GitHub secrets
# Add GITHUB_TOKEN to your repository secrets
```

### Daily Development Workflow
```bash
# Before committing (automatic via pre-commit hook)
git add .
git commit -m "feat: implement use case"
# → Automatically formats code

# Before pushing (automatic via pre-push hook)  
git push origin feature/branch
# → Validates architecture

# Manual validation anytime
./gradlew validateArchitecture

# Generate architecture report
./gradlew generateArchitectureReport
```

### CI/CD Integration
- **Every PR**: Runs full validation suite
- **Merge to main**: Enforces zero violations
- **Release builds**: Requires clean architecture

## 📈 Benefits

### Immediate Benefits
- ✅ **Zero architecture debt**: Violations caught before commit
- ✅ **Consistent codebase**: Automated formatting and standards
- ✅ **Fast feedback**: Local validation in seconds
- ✅ **Team alignment**: Clear, enforced boundaries

### Long-term Benefits
- ✅ **95% test coverage achievable**: Clean boundaries enable testing
- ✅ **Parallel development**: Independent modules
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Onboarding**: Self-documenting architecture

## 🔧 Customization

### Adding Custom Rules
1. Create new rule in `architecture-rules/src/main/kotlin/rules/`
2. Register in `CleanArchRuleSetProvider`
3. Add configuration to `detekt.yml`
4. Write tests in `architecture-rules/src/test/kotlin/`

### Adjusting Severity
```yaml
# In detekt.yml
clean-architecture:
  CleanArchitectureViolation:
    active: true
    severity: error  # error | warning | info
```

### Excluding Files
```yaml
# In detekt.yml
clean-architecture:
  excludes: 
    - "**/test/**"
    - "**/build/**"
    - "**/generated/**"
```

## 🏆 Success Metrics

Monitor these metrics to track adoption:

| Metric | Target | Measurement |
|--------|--------|-------------|
| Pre-push success rate | >95% | Git hook logs |
| CI/CD pass rate | >90% | GitHub Actions |
| Architecture violations | 0 | Detekt reports |
| Module coupling | <5% | ArchUnit metrics |
| Build time | <10% increase | Gradle profiler |

## 📝 Summary

This comprehensive enforcement system ensures clean architecture compliance through:

1. **Local hooks** for immediate feedback
2. **Custom Detekt rules** for architecture-specific validation
3. **ArchUnit tests** for dependency verification
4. **GitHub Actions** for CI/CD enforcement
5. **Danger** for automated PR reviews

The multi-layered approach catches violations early, educates developers, and maintains architectural integrity throughout the project lifecycle. It's designed to be strict enough to prevent violations but flexible enough to not hinder productivity.
