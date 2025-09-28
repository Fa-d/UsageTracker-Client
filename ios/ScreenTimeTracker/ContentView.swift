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

// Simple placeholder views for each screen
struct DashboardView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Text("Screen Time Dashboard")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                VStack(spacing: 16) {
                    DashboardCard(title: "Today's Usage", value: "4h 32m", icon: "clock.fill")
                    DashboardCard(title: "App Pickups", value: "47", icon: "iphone")
                    DashboardCard(title: "Wellness Score", value: "7.5/10", icon: "heart.fill")
                }

                Spacer()
            }
            .padding()
            .navigationTitle("Dashboard")
        }
    }
}

struct DashboardCard: View {
    let title: String
    let value: String
    let icon: String

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .font(.title2)

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
    }
}

struct AnalyticsView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Analytics")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("Detailed usage analytics will be displayed here")
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()

                Spacer()
            }
            .padding()
            .navigationTitle("Analytics")
        }
    }
}

struct WellnessView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Wellness")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("Wellness tracking and recommendations will be shown here")
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()

                Spacer()
            }
            .padding()
            .navigationTitle("Wellness")
        }
    }
}

struct GoalsView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Goals")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("Set and track your screen time goals here")
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()

                Spacer()
            }
            .padding()
            .navigationTitle("Goals")
        }
    }
}

struct SettingsView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Settings")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("App settings and preferences will be configured here")
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()

                Spacer()
            }
            .padding()
            .navigationTitle("Settings")
        }
    }
}

#Preview {
    ContentView()
}