plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

val ktlint by configurations.creating {
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
    }
}

dependencies {
    ktlint(libs.ktlint.cli)
}

val ktlintSources =
    listOf(
        "*.gradle.kts",
        "androidApp/*.gradle.kts",
        "shared/*.gradle.kts",
        "androidApp/src/**/*.kt",
        "shared/src/**/*.kt",
    )

tasks.register<JavaExec>("ktlintCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin source and Gradle script formatting."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(ktlintSources)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Format Kotlin sources and Gradle scripts."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args("--format")
    args(ktlintSources)
}
