package com.vcm.adoptamascota.utils

/**
 * Reglas de validación compartidas por todos los formularios.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 */
object Validators {

    fun name(value: String, label: String = "El nombre"): String? = when {
        value.isBlank() -> "$label es obligatorio"
        value.trim().length < 2 -> "Debe tener al menos 2 caracteres"
        value.trim().length > 30 -> "Máximo 30 caracteres"
        else -> null
    }

    fun phone(value: String): String? {
        val digits = value.count { it.isDigit() }
        return when {
            value.isBlank() -> "El teléfono es obligatorio"
            !value.all { it.isDigit() || it == '+' || it == ' ' } -> "Solo números, espacios y +"
            digits < 8 -> "Debe tener al menos 8 dígitos"
            digits > 15 -> "Máximo 15 dígitos"
            else -> null
        }
    }

    fun description(value: String): String? = when {
        value.isBlank() -> "La descripción es obligatoria"
        value.trim().length < 10 -> "Debe tener al menos 10 caracteres"
        value.length > 300 -> "Máximo 300 caracteres"
        else -> null
    }

    fun location(value: String): String? = when {
        value.isBlank() -> "El lugar es obligatorio"
        value.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        value.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    /** Lugar opcional (ej: dónde fue encontrada): vacío es válido, pero si se escribe debe tener sentido. */
    fun optionalPlace(value: String): String? = when {
        value.isBlank() -> null
        value.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        value.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    /** Nombre de la especie exótica (ej: "Iguana verde", "Cacatúa"). */
    fun exoticSpecies(value: String): String? = when {
        value.isBlank() -> "La especie es obligatoria"
        value.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        value.length > 40 -> "Máximo 40 caracteres"
        else -> null
    }

    /**
     * N° de resolución SAG, permiso CITES o documento que acredite el origen legal.
     * La app solo comprueba el formato: la verificación real la hace el refugio.
     */
    fun permit(value: String): String? = when {
        value.isBlank() -> "El N° de permiso o documento es obligatorio"
        !value.all { it.isLetterOrDigit() || it == ' ' || it == '-' || it == '/' || it == '.' } ->
            "Solo letras, números, espacios, - / ."
        value.trim().length < 4 -> "Debe tener al menos 4 caracteres"
        value.length > 40 -> "Máximo 40 caracteres"
        else -> null
    }

    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun email(value: String): String? = when {
        value.isBlank() -> "El correo es obligatorio"
        !EMAIL_REGEX.matches(value.trim()) -> "Ingresa un correo válido"
        else -> null
    }

    fun message(value: String): String? = when {
        value.isBlank() -> "Cuéntanos por qué quieres adoptar"
        value.trim().length < 10 -> "Debe tener al menos 10 caracteres"
        value.length > 300 -> "Máximo 300 caracteres"
        else -> null
    }
}
