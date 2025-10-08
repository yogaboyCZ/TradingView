import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class TradingviewComposeConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        project.pluginManager.withPlugin("com.android.library") {
            project.extensions.configure<LibraryExtension> {
                buildFeatures { compose = true }
            }
            project.dependencies {
                add("implementation", platform("androidx.compose:compose-bom:2025.09.00"))
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.material3:material3")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
            }
        }
    }
}