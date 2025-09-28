package buildlogic.test

import buildlogic.withVersionCatalog
import gradle.kotlin.dsl.accessors._592d1df61002f285ea6773698840b81b.testImplementation

plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-core")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    withVersionCatalog {
        testImplementation(libs.kotlin.coroutines.test)
        testImplementation(libs.assertk)
    }
}