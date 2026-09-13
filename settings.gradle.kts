pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
    repositories {
        mavenCentral()
    }
}

rootProject.name = "taxlot"

// Shared library
include("shared:platform")

// API Gateway
include("services:api-gateway")

// Identity
include("services:auth-service")
include("services:user-service")

// Tenant
include("services:organization-service")

// Financial Core
include("services:accounting-service")

// Business Operations
include("services:customer-service")
include("services:invoice-service")
include("services:payment-service")
include("services:expense-service")

// Tax & Analytics
include("services:tax-service")
include("services:reporting-service")

// Supporting Capabilities
include("services:document-service")
include("services:notification-service")
include("services:ai-service")
include("services:search-service")
include("services:integration-service")
