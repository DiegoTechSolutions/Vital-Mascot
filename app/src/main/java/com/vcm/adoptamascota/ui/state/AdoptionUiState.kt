package com.vcm.adoptamascota.ui.state

/** Campos del formulario de solicitud de adopción (para saber cuáles ya tocó el usuario). */
enum class AdoptionField { NAME, PHONE, EMAIL, MESSAGE, PERMIT }

/**
 * Estado del formulario "Solicitar adopción". Si la mascota es exótica ([requiresPermit]),
 * el adoptante debe indicar su permiso/documento y declarar que cumple la normativa.
 */
data class AdoptionFormState(
    val requiresPermit: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val message: String = "",
    val permit: String = "",
    val legalDeclared: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val messageError: String? = null,
    val permitError: String? = null,
    val touched: Set<AdoptionField> = emptySet(),
    val isValid: Boolean = false,
    val isSaving: Boolean = false
)
