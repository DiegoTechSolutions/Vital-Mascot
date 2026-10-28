package com.vcm.adoptamascota

import com.vcm.adoptamascota.utils.Validators
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    // ---- Reglas de mascotas de compañía

    @Test
    fun validBasicFieldsHaveNoErrors() {
        assertNull(Validators.name("Luna"))
        assertNull(Validators.phone("+56 9 1234 5678"))
        assertNull(Validators.description("Cariñosa y juguetona"))
    }

    @Test
    fun invalidBasicFieldsReturnIndependentErrors() {
        assertNotNull(Validators.name("  "))
        assertNotNull(Validators.phone("1234567"))
        assertNotNull(Validators.description("Pequeña"))
    }

    @Test
    fun phoneValidationCountsDigitsRatherThanFormattingCharacters() {
        assertNull(Validators.phone("+56 9 12345"))
        assertNotNull(Validators.phone("abc12345678"))
    }

    // ---- Mascotas exóticas

    @Test
    fun exoticSpeciesIsRequired() {
        assertNotNull(Validators.exoticSpecies(""))
        assertNotNull(Validators.exoticSpecies("ab"))
        assertNull(Validators.exoticSpecies("Iguana verde"))
    }

    @Test
    fun permitAcceptsResolutionStyleNumbers() {
        assertNull(Validators.permit("SAG 1234/2020"))
        assertNull(Validators.permit("CITES-AR-45.678"))
    }

    @Test
    fun permitRejectsEmptyShortOrStrangeCharacters() {
        assertNotNull(Validators.permit(""))
        assertNotNull(Validators.permit("123"))
        assertNotNull(Validators.permit("@@@@@@"))
    }

    // ---- Ubicación y solicitud de adopción

    @Test
    fun optionalPlaceAllowsBlankButChecksLength() {
        assertNull(Validators.optionalPlace(""))
        assertNull(Validators.optionalPlace("Plaza de Armas"))
        assertNotNull(Validators.optionalPlace("ab"))
        assertNotNull(Validators.optionalPlace("x".repeat(81)))
    }

    @Test
    fun emailMustLookLikeAnEmail() {
        assertNull(Validators.email("diego@correo.cl"))
        assertNotNull(Validators.email(""))
        assertNotNull(Validators.email("sin-arroba"))
        assertNotNull(Validators.email("a@b"))
    }

    @Test
    fun adoptionMessageNeedsAtLeastTenCharacters() {
        assertNotNull(Validators.message(""))
        assertNotNull(Validators.message("corto"))
        assertNull(Validators.message("Tengo patio y mucho cariño"))
    }
}
