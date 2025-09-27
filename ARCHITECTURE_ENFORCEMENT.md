# 🏗️ Architecture Enforcement Guide

This project implements a comprehensive Clean Architecture enforcement system that validates compliance at every stage of development.

## 🚀 Quick Start

### Initial Setup (Run Once)
```bash
# Install git hooks
./gradlew installGitHooks

# Validate current architecture
./gradlew validateArchitecture

# Generate architecture dashboard
./gradlew generateArchitectureHealthDashboard
```

### Daily Development Workflow
```bash
# Before committing (automatic via pre-commit hook)
git add .
git commit -m "feat: implement feature"
# → Automatically formats code

# Before pushing (automatic via pre-push hook)
git push origin feature/branch
# → Validates architecture

# Manual validation anytime
./gradlew validateArchitecture
```

## 📊 Architecture Dashboard

Generate a comprehensive architecture health report:
```bash
./gradlew generateArchitectureHealthDashboard
```

This creates:
- `build/reports/architecture/dashboard.html` - Interactive HTML dashboard
- `build/reports/architecture/detailed-report.md` - Detailed markdown report
- `build/reports/architecture/metrics.json` - Metrics for automation

## 🔒 Enforcement Layers

### Layer 1: Local Development
- **Pre-commit Hook**: Automatic code formatting with ktlint
- **Pre-push Hook**: Architecture validation before pushing

### Layer 2: Pull Requests
- **GitHub Actions**: Comprehensive validation suite
- **Danger**: Automated PR feedback and warnings

### Layer 3: Build-time Validation
- **ArchUnit Tests**: JUnit-based architecture tests
- **Custom Detekt Rules**: Clean architecture dependency rules

## 🎯 Architecture Rules

### Layer Dependencies
- **Features** → Domain + Data + Core
- **Data** → Domain + Core
- **Domain** → Core only
- **Core** → External libraries

### Naming Conventions
- Use cases must end with `UseCase`
- Repository interfaces in `domain.repository` package
- Repository implementations in `data.repository` package

### Domain Layer Purity
- No Android framework dependencies
- No data layer dependencies
- Pure Kotlin/Java only

### Feature Independence
- Features cannot depend on other features
- Core modules cannot depend on features
- Data modules cannot depend on features

## 🛠️ Available Tasks

| Task | Description |
|------|-------------|
| `validateArchitecture` | Run complete architecture validation |
| `installGitHooks` | Install pre-commit and pre-push hooks |
| `generateArchitectureHealthDashboard` | Generate comprehensive dashboard |
| `detekt` | Run Detekt with custom architecture rules |
| `test --tests "*Architecture*"` | Run ArchUnit architecture tests |

## 📈 Monitoring

### Success Metrics
- Pre-push success rate: >95%
- CI/CD pass rate: >90%
- Architecture violations: 0
- Build time impact: <10%

### Check Compliance
```bash
# Quick architecture check
./gradlew validateArchitecture

# Detailed analysis
./gradlew generateArchitectureHealthDashboard
```

## 🔧 Customization

### Adding Custom Rules
1. Create new rule in `architecture-rules/src/main/kotlin/rules/`
2. Register in `CleanArchRuleSetProvider`
3. Add configuration to `config/detekt/detekt.yml`

### Adjusting Severity
Edit `config/detekt/detekt.yml`:
```yaml
clean-architecture:
  CleanArchitectureViolation:
    active: true
    severity: error  # error | warning | info
```

## 🚨 Troubleshooting

### Architecture Validation Fails
1. Check the detailed error messages
2. Review `build/reports/detekt/detekt.html`
3. Run ArchUnit tests for specific violations

### Git Hooks Not Working
```bash
# Reinstall hooks
./gradlew installGitHooks

# Check hook permissions
ls -la .git/hooks/
```

### CI/CD Issues
1. Check GitHub Actions logs
2. Verify SARIF reports are generated
3. Review Danger feedback in PR

## 📋 Best Practices

### Development
1. Run `./gradlew validateArchitecture` before pushing
2. Follow the established module structure
3. Keep domain layer pure
4. Maintain feature independence

### Code Reviews
1. Check Danger feedback
2. Verify architecture tests pass
3. Review dependency changes carefully

### Maintenance
1. Regular dashboard reviews
2. Update enforcement rules as needed
3. Monitor compliance metrics

---

*This enforcement system ensures your project maintains clean architecture principles throughout its lifecycle. For questions or issues, refer to the detailed architecture documentation.*