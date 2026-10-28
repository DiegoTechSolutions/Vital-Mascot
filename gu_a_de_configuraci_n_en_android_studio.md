# 🛠️ Guía Paso a Paso: Configuración del Proyecto en Android Studio

Esta guía contiene las instrucciones exactas para inicializar, configurar y estructurar el proyecto **"AdoptaMascota VcM"** en Android Studio cumpliendo con los estándares de la Evaluación Parcial 2.

---

## 1. Creación del Proyecto en Android Studio

1. Abre **Android Studio** (versión Hedgehog, Iguana, Jellyfish o posterior).
2. Haz clic en **New Project**.
3. En la sección de plantillas, selecciona **Empty Activity** (asegúrate de que tenga el icono de Jetpack Compose).
4. Configura los siguientes parámetros:
   * **Name:** `AdoptaMascotaVcM`
   * **Package name:** `com.vcm.adoptamascota`
   * **Save location:** Selecciona una carpeta en tu equipo.
   * **Language:** `Kotlin`
   * **Minimum SDK:** `API 24: Android 7.0 (Nougat)` o superior (`API 26` recomendada).
   * **Build configuration language:** `Kotlin DSL (build.gradle.kts)`.
5. Haz clic en **Finish** y espera a que Gradle sincronice el proyecto por primera vez.

---

## 2. Configuración de Gradle y Dependencias (`build.gradle.kts`)

Abre el archivo **`build.gradle.kts (Module :app)`** e incluye las dependencias necesarias para **Room**, **Navigation Compose**, **ViewModel** e **Icons**:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Plugin necesario para Room Compiler
}

android {
    namespace = "com.vcm.adoptamascota"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vcm.adoptamascota"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose & Lifecycle ViewModel
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // Room Database (Persistencia Local SQLite)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Pruebas y Depuración
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

> **Nota:** Tras pegar el código en `build.gradle.kts`, haz clic en **Sync Now** en la barra superior derecha.

---

## 3. Estructura de Paquetes del Proyecto (MVVM)

En la vista de proyecto (**Android > java > com.vcm.adoptamascota**), crea la siguiente estructura de carpetas (Packages):

```text
com.vcm.adoptamascota/
├── data/
│   ├── local/          # PetEntity.kt, PetDao.kt, AppDatabase.kt
│   └── repository/     # PetRepository.kt
├── ui/
│   ├── navigation/     # AppNavigation.kt
│   ├── screens/        # PetListScreen.kt, AddPetScreen.kt
│   ├── state/          # PetUiState.kt
│   └── viewmodel/      # PetViewModel.kt
└── utils/              # DateUtils.kt
```

---

## 4. Configuración de Git, GitHub y Trello (IE 2.3.2)

1. **Inicializar Git:**
   * En Android Studio, ve al menú superior: **VCS > Enable Version Control Integration** -> selecciona **Git**.
2. **Crear Repositorio en GitHub:**
   * Crea un nuevo repositorio en GitHub llamado `vcm-app-adopta-mascota`.
   * Vincula tu proyecto con: `git remote add origin https://github.com/tu-usuario/vcm-app-adopta-mascota.git`.
   * Realiza commits frecuentes divididos por integrantes (ej: "feat: agrega estructura Room", "feat: implementa formulario validado").
3. **Tablero Trello / GitHub Projects:**
   * Crea un tablero en Trello con 3 columnas: `Por hacer`, `En proceso`, `Finalizado`.
   * Agrega tarjetas como: *Crear Base de Datos Room*, *Diseñar Formulario en Compose*, *Implementar Recursos Nativos*, *Validaciones de Formulario*.