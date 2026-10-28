# Prompt para Claude: Proyecto VcM "AdoptaMascota" (DSY1105)

> **Instrucciones:** Copia todo el contenido de este bloque y pégalo en una sesión de Claude para que te genere el código completo, ajustado a la rúbrica y listo para compilar.

---

```text
Actúa como un Senior Android Developer y Docente de Programación Móvil. Necesito que me generes todo el código fuente y archivos de configuración para la Evaluación Parcial 2 del proyecto de Vinculación con el Medio (VcM) de la asignatura Desarrollo de Aplicaciones Móviles (DSY1105).

### 🎯 CONTEXTO DEL PROYECTO
- Nombre de la app: "AdoptaMascota VcM" (Plataforma para rescate, adopción y gestión comunitaria de mascotas).
- Lenguaje: Kotlin.
- Framework UI: Jetpack Compose con Material 3.
- Paquete raíz: `com.vcm.adoptamascota`
- Arquitectura estricta: MVVM (Model - View - ViewModel) con Clean Architecture básica y repositorio.

---

### 📋 REQUERIMIENTOS TÉCNICOS OBLIGATORIOS (RÚBRICA DE EVALUACIÓN)

1. **Arquitectura MVVM & Room (IE 2.3.1 - 15%):**
   - Entidad `PetEntity`: id (Int, PK autoGenerate), name (String), species (String), age (String), description (String), contactPhone (String), imageUri (String?), dateRegistered (String).
   - `PetDao`: Métodos `@Query` para obtener todas las mascotas ordenadas por ID descendente (devuelve `Flow<List<PetEntity>>`), `@Insert(onConflict = REPLACE)` y `@Delete`.
   - `AppDatabase`: Singleton para la base de datos Room (`pet_adoption_db`).
   - `PetRepository`: Capa intermedia de abstracción de datos.

2. **Gestión de Estado y Lógica Desacoplada (IE 2.2.1 - 10%):**
   - `PetFormState`: Data class con campos del formulario, mensajes de error individuales (`nameError`, `phoneError`, `descriptionError`) y flag `isValid: Boolean`.
   - `PetListState`: Data class con lista de mascotas, `searchQuery` y `selectedSpecies`.
   - `PetViewModel`: Hereda de `AndroidViewModel`, expone `StateFlow` para el formulario y la lista. Gestiona validaciones en tiempo real y operaciones asíncronas con Coroutines (`viewModelScope`).

3. **Formularios con Validaciones Visuales (IE 2.1.2 - 15%):**
   - Pantalla `AddPetScreen`:
     - Campo Nombre (obligatorio, no vacío).
     - Campo Teléfono (obligatorio, mínimo 8 dígitos).
     - Campo Especie (chips de selección: Perro, Gato, Otro).
     - Campo Edad (opcional).
     - Campo Descripción (obligatorio, mínimo 10 caracteres).
     - Botón "Guardar y Publicar" deshabilitado si el formulario no es válido (`isValid == false`).
     - Mensajes de error en color rojo (`MaterialTheme.colorScheme.error`) debajo de cada campo con error.

4. **Listas, Búsquedas y Filtros (IE 2.2.2 - 10%):**
   - Pantalla `PetListScreen`:
     - `OutlinedTextField` para búsqueda por texto (filtra por nombre o descripción en tiempo real).
     - `FilterChip` para filtrar por especie ("Todas", "Perro", "Gato", "Otro").
     - `LazyColumn` que muestra las tarjetas (`Card`) de mascotas registradas en Room.

5. **Navegación Funcional (IE 2.1.1 - 15%):**
   - `AppNavigation`: Usa `NavHost` y `rememberNavController` para navegar entre `PetListScreen` y `AddPetScreen`.
   - Utiliza `Scaffold` con `TopAppBar` y `FloatingActionButton`.

6. **Recursos Nativos del Dispositivo (IE 2.4.1 - 15%):**
   - **Recurso 1 (Galería):** Uso de `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())` para seleccionar una imagen de la mascota.
   - **Recurso 2 (Fecha/Hora del Sistema):** `DateUtils` utilizando `SimpleDateFormat` para estampar la fecha exacta de registro del animal.

7. **Documentación y Trabajo Colaborativo (IE 2.3.2 - 20%):**
   - Genera el contenido completo de un archivo `README.md` estructurado para GitHub, explicando el contexto VcM, integrantes, arquitectura MVVM, Room, recursos nativos y enlace simbólico al tablero Trello.

---

### 📦 ENTREGABLE REQUERIDO

Entrégame el código completo, libre de errores y listo para copiar en Android Studio para cada uno de los siguientes archivos:

1. `data/local/PetEntity.kt`
2. `data/local/PetDao.kt`
3. `data/local/AppDatabase.kt`
4. `data/repository/PetRepository.kt`
5. `utils/DateUtils.kt`
6. `ui/state/PetUiState.kt`
7. `ui/viewmodel/PetViewModel.kt`
8. `ui/screens/PetListScreen.kt`
9. `ui/screens/AddPetScreen.kt`
10. `ui/navigation/AppNavigation.kt`
11. `MainActivity.kt`
12. `README.md`

Asegúrate de agregar comentarios explicativos breves en el código para facilitar la defensa individual (Evaluación Parcial 3).
```