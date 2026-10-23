import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

// La clave de Google Maps se lee de local.properties (MAPS_API_KEY=...) para no subirla al repositorio
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "sv.edu.ues.dam235.compucel"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "sv.edu.ues.dam235.compucel"
        minSdk = 30
        targetSdk = 36
        versionCode = 3
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = propiedadesLocales.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.lifecycle.livedata)

    // Base de datos local
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Base de datos en la nube
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    // Mapa
    implementation(libs.play.services.maps)

    // Hash de contrasenas
    implementation(libs.bcrypt)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.room.testing)
}
