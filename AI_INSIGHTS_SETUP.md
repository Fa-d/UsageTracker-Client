# AI Insights Module Setup Guide

## Overview
The AI Insights feature has been moved to a separate private repository to protect proprietary code while maintaining integration with the main app.

## Setup Instructions

### For Developers with AI Access

1. **Clone the main repository** (if you haven't already):
   ```bash
   git clone https://github.com/YOUR_USERNAME/UsageTracker-Client.git
   cd UsageTracker-Client
   ```

2. **Add the AI Insights submodule**:
   ```bash
   git submodule add https://github.com/YOUR_USERNAME/UsageTracker-AI-Insights.git ai_insights
   git submodule update --init --recursive
   ```

3. **Configure submodule to track main branch**:
   ```bash
   cd ai_insights
   git checkout main
   cd ..
   git add .gitmodules ai_insights
   git commit -m "Add AI insights as submodule"
   ```

### For Developers without AI Access

No additional setup required. The app will build without AI features automatically.

## Build Behavior

- **With AI submodule**: App includes AI insights as a dynamic feature module
- **Without AI submodule**: App builds normally without AI features

## Development Workflow

### Updating AI Insights
```bash
cd ai_insights
git pull origin main
cd ..
git add ai_insights
git commit -m "Update AI insights submodule"
```

### Making Changes to AI Insights
```bash
cd ai_insights
# Make your changes
git add .
git commit -m "Your AI changes"
git push origin main
cd ..
git add ai_insights
git commit -m "Update AI insights submodule reference"
```

## Synchronized Releases

1. **Tag AI insights first**:
   ```bash
   cd ai_insights
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Update main app submodule reference**:
   ```bash
   cd ..
   git add ai_insights
   git commit -m "Update AI insights to v1.0.0"
   git tag v1.0.0
   git push origin v1.0.0
   ```

## CI/CD Configuration

Add to your GitHub Actions workflow:

```yaml
- name: Checkout with submodules
  uses: actions/checkout@v3
  with:
    submodules: recursive
    token: ${{ secrets.GITHUB_TOKEN }}
```

For private submodules, you may need a personal access token with repository access.