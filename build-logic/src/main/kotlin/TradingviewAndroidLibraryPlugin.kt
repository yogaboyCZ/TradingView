import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class TradingviewAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        setPlugins(project)
        setProjectConfig(project)

        project.tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    private fun setPlugins(project: Project) {
        project.plugins.apply("com.android.library")
    }

    private fun setProjectConfig(project: Project) {
        val libs = project.extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("libs")

        val compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
        val minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
        val namespace = libs.findVersion("namespace").get().requiredVersion

        project.android().apply {
            this.namespace = namespace
            this.compileSdk = compileSdk

            defaultConfig {
                this.minSdk = minSdk
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    private fun Project.android(): LibraryExtension =
        extensions.getByType(LibraryExtension::class.java)
}