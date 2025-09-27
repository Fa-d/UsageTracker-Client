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
        if (preCommitSource.exists()) {
            Files.copy(
                preCommitSource.toPath(),
                preCommitTarget.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
            preCommitTarget.setExecutable(true)
            println("✅ Pre-commit hook installed")
        }

        // Copy pre-push hook
        val prePushSource = file("${rootProject.rootDir}/config/git-hooks/pre-push")
        val prePushTarget = file("${rootProject.rootDir}/.git/hooks/pre-push")
        if (prePushSource.exists()) {
            Files.copy(
                prePushSource.toPath(),
                prePushTarget.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
            prePushTarget.setExecutable(true)
            println("✅ Pre-push hook installed")
        }

        println("🎯 Git hooks installed successfully!")
    }
}

// Auto-install on project sync
gradle.projectsEvaluated {
    rootProject.tasks.findByName("preBuild")?.dependsOn("installGitHooks")
}