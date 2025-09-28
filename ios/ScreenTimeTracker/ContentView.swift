import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("Dashboard")
                }

            AnalyticsView()
                .tabItem {
                    Image(systemName: "chart.bar.fill")
                    Text("Analytics")
                }

            WellnessView()
                .tabItem {
                    Image(systemName: "heart.fill")
                    Text("Wellness")
                }

            GoalsView()
                .tabItem {
                    Image(systemName: "star.fill")
                    Text("Goals")
                }

            SettingsView()
                .tabItem {
                    Image(systemName: "gear")
                    Text("Settings")
                }
        }
        .accentColor(.blue)
    }
}

// MARK: - Dashboard View
struct DashboardView: View {
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    Text("Screen Time Dashboard")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .padding(.top)

                    // Overview Cards
                    VStack(spacing: 16) {
                        DashboardCard(
                            title: "Today's Usage",
                            value: "4h 23m",
                            icon: "clock.fill",
                            color: .blue
                        )
                        DashboardCard(
                            title: "App Pickups",
                            value: "18",
                            icon: "iphone",
                            color: .orange
                        )
                        DashboardCard(
                            title: "Wellness Score",
                            value: "72%",
                            icon: "heart.fill",
                            color: .green
                        )
                    }

                    // Quick Actions
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Quick Actions")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        HStack(spacing: 12) {
                            ActionButton(title: "Take Break", icon: "pause.circle.fill", color: .purple)
                            ActionButton(title: "View Goals", icon: "target", color: .blue)
                            ActionButton(title: "Wellness", icon: "heart.fill", color: .pink)
                        }
                        .padding(.horizontal)
                    }

                    // Top Apps Today
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Top Apps Today")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        VStack(spacing: 8) {
                            AppUsageRow(appName: "Social Media", time: "1h 32m", percentage: 0.35, color: .blue)
                            AppUsageRow(appName: "Work Apps", time: "1h 15m", percentage: 0.29, color: .green)
                            AppUsageRow(appName: "Entertainment", time: "58m", percentage: 0.22, color: .orange)
                            AppUsageRow(appName: "Productivity", time: "38m", percentage: 0.14, color: .purple)
                        }
                        .padding(.horizontal)
                    }

                    Spacer()
                }
            }
            .navigationTitle("Dashboard")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct DashboardCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
                .font(.title2)
                .frame(width: 40)

            VStack(alignment: .leading) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.title2)
                    .fontWeight(.semibold)
            }

            Spacer()
        }
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

struct ActionButton: View {
    let title: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
            Text(title)
                .font(.caption)
                .fontWeight(.medium)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
    }
}

struct AppUsageRow: View {
    let appName: String
    let time: String
    let percentage: Double
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Circle()
                    .fill(color)
                    .frame(width: 12, height: 12)
                Text(appName)
                    .fontWeight(.medium)
                Spacer()
                Text(time)
                    .foregroundColor(.secondary)
            }

            GeometryReader { geometry in
                HStack(spacing: 0) {
                    Rectangle()
                        .fill(color)
                        .frame(width: geometry.size.width * percentage)
                    Rectangle()
                        .fill(Color(UIColor.systemGray5))
                }
            }
            .frame(height: 4)
            .cornerRadius(2)
        }
        .padding(.horizontal)
    }
}

// MARK: - Analytics View
struct AnalyticsView: View {
    @State private var selectedTimeRange = "Week"
    private let timeRanges = ["Today", "Week", "Month", "Year"]

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Time Range Selector
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Time Range")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(timeRanges, id: \.self) { range in
                                    TimeRangeChip(
                                        title: range,
                                        isSelected: selectedTimeRange == range
                                    ) {
                                        selectedTimeRange = range
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                    }

                    // Usage Overview
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Usage Overview")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        HStack(spacing: 16) {
                            OverviewMetric(title: "Total Time", value: "28h 45m", subtitle: "Avg: 4h 6m/day")
                            OverviewMetric(title: "Total Pickups", value: "124", subtitle: "Avg: 18/day")
                        }
                        .padding(.horizontal)
                    }

                    // Usage Trends Chart
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Usage Trends")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        // Placeholder for chart
                        VStack {
                            Text("📊")
                                .font(.system(size: 40))
                            Text("Usage Trends Chart")
                                .font(.headline)
                            Text("Chart implementation pending")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .frame(height: 200)
                        .frame(maxWidth: .infinity)
                        .background(Color(UIColor.systemGray6))
                        .cornerRadius(12)
                        .padding(.horizontal)
                    }

                    // App Usage Breakdown
                    VStack(alignment: .leading, spacing: 16) {
                        Text("App Usage Breakdown")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        VStack(spacing: 12) {
                            AppBreakdownRow(app: "Social Media", time: "8h 30m", percentage: 30, change: "+15%", isPositive: false)
                            AppBreakdownRow(app: "Work Apps", time: "7h 20m", percentage: 26, change: "-5%", isPositive: true)
                            AppBreakdownRow(app: "Entertainment", time: "6h 45m", percentage: 24, change: "+8%", isPositive: false)
                            AppBreakdownRow(app: "Productivity", time: "3h 30m", percentage: 12, change: "-2%", isPositive: true)
                            AppBreakdownRow(app: "Games", time: "2h 40m", percentage: 8, change: "+22%", isPositive: false)
                        }
                    }

                    Spacer()
                }
            }
            .navigationTitle("Analytics")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct TimeRangeChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.callout)
                .fontWeight(.medium)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color(UIColor.systemGray6))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}

struct OverviewMetric: View {
    let title: String
    let value: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .font(.title2)
                .fontWeight(.semibold)
            Text(subtitle)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
    }
}

struct AppBreakdownRow: View {
    let app: String
    let time: String
    let percentage: Int
    let change: String
    let isPositive: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(app)
                    .fontWeight(.medium)
                Text(time)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                Text("\(percentage)%")
                    .fontWeight(.semibold)
                Text(change)
                    .font(.caption)
                    .foregroundColor(isPositive ? .green : .red)
            }
        }
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

// MARK: - Wellness View
struct WellnessView: View {
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Wellness Score
                    WellnessScoreCard()

                    // Wellness Metrics
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Wellness Metrics")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        VStack(spacing: 12) {
                            WellnessMetricCard(
                                title: "Daily Screen Time",
                                description: "Average for this week",
                                value: "4h 23m",
                                progress: 0.65,
                                status: .warning
                            )
                            WellnessMetricCard(
                                title: "Phone Pickups",
                                description: "Times opened today",
                                value: "18",
                                progress: 0.45,
                                status: .good
                            )
                            WellnessMetricCard(
                                title: "Breaks Taken",
                                description: "Digital breaks today",
                                value: "5",
                                progress: 0.83,
                                status: .good
                            )
                        }
                    }

                    // Digital Breaks
                    DigitalBreaksSection()

                    // Mindfulness
                    MindfulnessSection()

                    Spacer()
                }
            }
            .navigationTitle("Wellness")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct WellnessScoreCard: View {
    var body: some View {
        VStack(spacing: 16) {
            Text("Wellness Score")
                .font(.headline)
                .fontWeight(.semibold)

            ZStack {
                Circle()
                    .stroke(Color(UIColor.systemGray5), lineWidth: 8)
                Circle()
                    .trim(from: 0, to: 0.68)
                    .stroke(Color.blue, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                    .rotationEffect(.degrees(-90))

                VStack {
                    Text("68")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                    Text("↑ 12% from last week")
                        .font(.caption)
                        .foregroundColor(.green)
                }
            }
            .frame(width: 120, height: 120)

            VStack(alignment: .leading, spacing: 4) {
                Text("Insights")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                ForEach([
                    "Your screen time decreased by 15 minutes today",
                    "You took 3 more breaks than usual",
                    "Your evening usage is still high"
                ], id: \.self) { insight in
                    Text("• \(insight)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

enum WellnessStatus {
    case good, warning, critical

    var color: Color {
        switch self {
        case .good: return .green
        case .warning: return .orange
        case .critical: return .red
        }
    }
}

struct WellnessMetricCard: View {
    let title: String
    let description: String
    let value: String
    let progress: Double
    let status: WellnessStatus

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                VStack(alignment: .leading) {
                    Text(title)
                        .fontWeight(.medium)
                    Text(description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Spacer()

                Text(value)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(status.color)
            }

            ProgressView(value: progress)
                .progressViewStyle(LinearProgressViewStyle(tint: status.color))
        }
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

struct DigitalBreaksSection: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Digital Breaks")
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.horizontal)

            VStack(spacing: 12) {
                HStack {
                    VStack(alignment: .leading) {
                        Text("Breaks Today: 5")
                            .fontWeight(.medium)
                        Text("Next suggested: in 25 minutes")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }

                HStack(spacing: 12) {
                    Button("Start Break Now") {
                        // Action
                    }
                    .buttonStyle(.borderedProminent)

                    Button("Schedule Break") {
                        // Action
                    }
                    .buttonStyle(.bordered)

                    Spacer()
                }
            }
            .padding()
            .background(Color(UIColor.systemGray6))
            .cornerRadius(12)
            .padding(.horizontal)
        }
    }
}

struct MindfulnessSection: View {
    private let exercises = [
        ("Deep Breathing", "5 mins", "🫁"),
        ("Body Scan", "10 mins", "🧘"),
        ("Gratitude", "3 mins", "💝"),
        ("Walking Meditation", "15 mins", "🚶")
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Mindfulness")
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.horizontal)

            VStack(spacing: 12) {
                HStack {
                    VStack(alignment: .leading) {
                        Text("Streak: 7 days")
                            .fontWeight(.medium)
                        Text("Today's progress: 60%")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }

                ProgressView(value: 0.6)
                    .progressViewStyle(LinearProgressViewStyle(tint: .purple))

                ForEach(Array(exercises.enumerated()), id: \.offset) { index, exercise in
                    HStack {
                        Text(exercise.2)
                            .font(.title2)
                        VStack(alignment: .leading) {
                            Text(exercise.0)
                                .fontWeight(.medium)
                            Text(exercise.1)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Button("Start") {
                            // Action
                        }
                        .buttonStyle(.bordered)
                        .controlSize(.small)
                    }
                    .padding(.vertical, 4)
                }
            }
            .padding()
            .background(Color(UIColor.systemGray6))
            .cornerRadius(12)
            .padding(.horizontal)
        }
    }
}

// MARK: - Goals View
struct GoalsView: View {
    @State private var selectedCategory = "All"
    private let categories = ["All", "Screen Time", "Breaks", "Mindfulness", "Pickups", "Sleep"]

    private let mockGoals = [
        Goal(id: "1", title: "Reduce Screen Time", description: "Limit daily usage to under 4 hours", category: "Screen Time", progress: 0.9, isCompleted: false, dueDate: "Dec 31, 2024"),
        Goal(id: "2", title: "Take Regular Breaks", description: "Take at least 6 breaks per day", category: "Breaks", progress: 0.83, isCompleted: false, dueDate: "Daily"),
        Goal(id: "3", title: "Morning Mindfulness", description: "Complete 10 minutes of meditation each morning", category: "Mindfulness", progress: 1.0, isCompleted: true, dueDate: "Daily")
    ]

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // New Goal Button
                    HStack {
                        Spacer()
                        Button("New Goal") {
                            // Action
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .padding(.horizontal)

                    // Goals Overview
                    GoalsOverviewCard(
                        totalGoals: mockGoals.count,
                        completedGoals: mockGoals.filter { $0.isCompleted }.count,
                        activeGoals: mockGoals.filter { !$0.isCompleted }.count,
                        weeklyProgress: 0.74
                    )

                    // Category Selector
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Category")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(categories, id: \.self) { category in
                                    TimeRangeChip(
                                        title: category,
                                        isSelected: selectedCategory == category
                                    ) {
                                        selectedCategory = category
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                    }

                    // Goals List
                    VStack(spacing: 12) {
                        ForEach(mockGoals) { goal in
                            GoalCard(goal: goal)
                        }
                    }

                    Spacer()
                }
            }
            .navigationTitle("Goals")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct Goal: Identifiable {
    let id: String
    let title: String
    let description: String
    let category: String
    let progress: Double
    let isCompleted: Bool
    let dueDate: String
}

struct GoalsOverviewCard: View {
    let totalGoals: Int
    let completedGoals: Int
    let activeGoals: Int
    let weeklyProgress: Double

    var body: some View {
        VStack(spacing: 16) {
            Text("Goals Overview")
                .font(.headline)
                .fontWeight(.semibold)

            HStack(spacing: 16) {
                MetricColumn(title: "Total", value: "\(totalGoals)", color: .blue)
                MetricColumn(title: "Completed", value: "\(completedGoals)", color: .green)
                MetricColumn(title: "Active", value: "\(activeGoals)", color: .orange)
            }

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Weekly Progress")
                        .fontWeight(.medium)
                    Spacer()
                    Text("\(Int(weeklyProgress * 100))%")
                        .fontWeight(.semibold)
                }
                ProgressView(value: weeklyProgress)
                    .progressViewStyle(LinearProgressViewStyle(tint: .blue))
            }
        }
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

struct MetricColumn: View {
    let title: String
    let value: String
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(color)
        }
        .frame(maxWidth: .infinity)
    }
}

struct GoalCard: View {
    let goal: Goal

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(goal.title)
                        .fontWeight(.medium)
                    if !goal.description.isEmpty {
                        Text(goal.description)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                HStack {
                    Button(action: {}) {
                        Image(systemName: "pencil")
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.small)

                    Button(action: {}) {
                        Image(systemName: goal.isCompleted ? "checkmark.circle.fill" : "circle")
                            .foregroundColor(goal.isCompleted ? .green : .gray)
                    }
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Progress")
                        .font(.caption)
                    Spacer()
                    Text("\(Int(goal.progress * 100))%")
                        .font(.caption)
                        .fontWeight(.medium)
                }
                ProgressView(value: goal.progress)
                    .progressViewStyle(LinearProgressViewStyle(tint: goal.isCompleted ? .green : .blue))
            }

            HStack {
                HStack {
                    Text(goal.category)
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.blue.opacity(0.2))
                        .foregroundColor(.blue)
                        .cornerRadius(12)
                }

                Spacer()

                Text("Due: \(goal.dueDate)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(UIColor.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

// MARK: - Settings View
struct SettingsView: View {
    var body: some View {
        NavigationView {
            List {
                // Notifications Section
                Section("Notifications") {
                    SettingsToggleRow(title: "Break Reminders", subtitle: "Get notified when it's time for a break", isOn: .constant(true))
                    SettingsToggleRow(title: "Usage Alerts", subtitle: "Alert when approaching daily limits", isOn: .constant(false))
                    SettingsSliderRow(title: "Reminder Frequency", subtitle: "How often to show break reminders", value: .constant(30), range: 15...120, unit: " min")
                }

                // Privacy & Data Section
                Section("Privacy & Data") {
                    SettingsToggleRow(title: "Data Collection", subtitle: "Allow anonymous usage analytics", isOn: .constant(true))
                    SettingsActionRow(title: "Export Data", subtitle: "Download your usage data") {
                        // Action
                    }
                    SettingsActionRow(title: "Privacy Policy", subtitle: "Review our privacy practices") {
                        // Action
                    }
                }

                // About Section
                Section("About") {
                    SettingsInfoRow(title: "Version", subtitle: "Current app version", value: "1.0.0")
                    SettingsActionRow(title: "Send Feedback", subtitle: "Help us improve the app") {
                        // Action
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct SettingsToggleRow: View {
    let title: String
    let subtitle: String
    @Binding var isOn: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .fontWeight(.medium)
                if !subtitle.isEmpty {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            Toggle("", isOn: $isOn)
        }
        .padding(.vertical, 4)
    }
}

struct SettingsSliderRow: View {
    let title: String
    let subtitle: String
    @Binding var value: Double
    let range: ClosedRange<Double>
    let unit: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .fontWeight(.medium)
                Spacer()
                Text("\(Int(value))\(unit)")
                    .fontWeight(.semibold)
            }

            if !subtitle.isEmpty {
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Slider(value: $value, in: range)
        }
        .padding(.vertical, 4)
    }
}

struct SettingsActionRow: View {
    let title: String
    let subtitle: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)
                    if !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                Text("Open")
                    .foregroundColor(.blue)
            }
        }
        .padding(.vertical, 4)
    }
}

struct SettingsInfoRow: View {
    let title: String
    let subtitle: String
    let value: String

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .fontWeight(.medium)
                if !subtitle.isEmpty {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            Text(value)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    ContentView()
}