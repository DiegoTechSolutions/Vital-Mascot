package com.vcm.adoptamascota.ui.state

import com.vcm.adoptamascota.data.local.PetCategory
import com.vcm.adoptamascota.data.local.PetEntity

const val SPECIES_ALL = "Todas"
const val FILTER_EXOTIC = "Exóticas"
val SPECIES_FILTERS = listOf(SPECIES_ALL, "Perro", "Gato", "Otro", FILTER_EXOTIC)
val SPECIES_OPTIONS = listOf("Perro", "Gato", "Otro")

/** Tipos de mascota exótica que se pueden registrar. */
val EXOTIC_TYPES = listOf("Reptil", "Ave", "Mamífero pequeño", "Anfibio", "Otro exótico")

fun speciesEmoji(species: String): String = when (species) {
    "Perro" -> "🐶"
    "Gato" -> "🐱"
    "Reptil" -> "🦎"
    "Ave" -> "🦜"
    "Mamífero pequeño" -> "🐹"
    "Anfibio" -> "🐸"
    FILTER_EXOTIC -> "🦎"
    "Todas" -> "🐾"
    else -> "🐾"
}

/** Etiqueta legible de la categoría de una mascota. */
fun categoryLabel(category: String): String =
    if (category == PetCategory.EXOTIC) "Exótica" else "De compañía"

/** Estado inmutable de la pantalla de listado (la UI solo lo lee, nunca lo modifica). */
data class PetListState(
    val pets: List<PetEntity> = emptyList(),   // ya filtradas por texto y especie
    val totalCount: Int = 0,                   // total en Room, sin filtros
    val searchQuery: String = "",
    val selectedSpecies: String = SPECIES_ALL,
    val isLoading: Boolean = true
)

/** Campos del formulario; sirve para saber cuáles ya fueron tocados por el usuario. */
enum class FormField {
    NAME, PHONE, AGE, DESCRIPTION, LOCATION,
    EXOTIC_SPECIES, PERMIT, FOUND_PLACE, LOST_PLACE
}

/** Estado del formulario "Reportar animal perdido". */
data class LostFormState(
    val name: String = "",
    val species: String = SPECIES_OPTIONS.first(),
    val lastSeen: String = "",
    val phone: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val nameError: String? = null,
    val lastSeenError: String? = null,
    val phoneError: String? = null,
    val descriptionError: String? = null,
    val touched: Set<FormField> = emptySet(),
    val isValid: Boolean = false,
    val isSaving: Boolean = false
)

/**
 * Estado del formulario. Los mensajes de error y [isValid] los calcula el ViewModel
 * (lógica de validación centralizada y desacoplada de la UI).
 */
data class PetFormState(
    val name: String = "",
    val category: String = PetCategory.COMPANION,
    val species: String = SPECIES_OPTIONS.first(),    // solo aplica a mascotas de compañía
    val exoticType: String = EXOTIC_TYPES.first(),    // solo aplica a exóticas
    val exoticSpecies: String = "",
    val permit: String = "",
    val legalDeclared: Boolean = false,               // "declaro que el origen del animal es legal"
    val age: String = "",
    val phone: String = "",
    val description: String = "",
    val imageUri: String? = null,       // Uri temporal elegido en la galería
    val foundPlace: String = "",
    val foundLat: Double? = null,
    val foundLng: Double? = null,
    val lostPlace: String = "",
    val lostLat: Double? = null,
    val lostLng: Double? = null,
    val nameError: String? = null,
    val phoneError: String? = null,
    val ageError: String? = null,
    val descriptionError: String? = null,
    val exoticSpeciesError: String? = null,
    val permitError: String? = null,
    val foundPlaceError: String? = null,
    val lostPlaceError: String? = null,
    val touched: Set<FormField> = emptySet(),
    val isValid: Boolean = false,
    val isSaving: Boolean = false
) {
    val isExotic: Boolean get() = category == PetCategory.EXOTIC
}
