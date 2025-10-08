import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class TradingviewComposeConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        target.pluginManager.withPlugin("com.android.library") {
            target.extensions.configure<LibraryExtension> {
                buildFeatures { compose = true }
            }
            target.dependencies {
                add("implementation", platform("androidx.compose:compose-bom:2025.09.00"))
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.material3:material3")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
            }
        }
    }
}