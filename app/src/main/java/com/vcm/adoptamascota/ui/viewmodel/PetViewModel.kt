package com.vcm.adoptamascota.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vcm.adoptamascota.data.local.AppDatabase
import com.vcm.adoptamascota.data.local.PetEntity
import com.vcm.adoptamascota.data.repository.PetRepository
import com.vcm.adoptamascota.ui.state.FILTER_EXOTIC
import com.vcm.adoptamascota.ui.state.FormField
import com.vcm.adoptamascota.ui.state.PetFormState
import com.vcm.adoptamascota.ui.state.PetListState
import com.vcm.adoptamascota.ui.state.SPECIES_ALL
import com.vcm.adoptamascota.utils.DateUtils
import com.vcm.adoptamascota.utils.ImageStorage
import com.vcm.adoptamascota.utils.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel (MVVM): toda la lógica vive aquí, las pantallas solo observan StateFlow
 * y notifican eventos. Así la validación y los filtros no dependen de la UI.
 */
class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PetRepository(
        AppDatabase.getInstance(application).petDao(),
        AppDatabase.getInstance(application).adoptionRequestDao()
    )

    // ---------------------------------------------------------------- LISTADO
    private val searchQuery = MutableStateFlow("")
    private val selectedSpecies = MutableStateFlow(SPECIES_ALL)

    /**
     * REQ. 6 (Búsqueda y filtros): combine() junta 3 flujos (Room + texto + especie).
     * Cada vez que cambia cualquiera, se recalcula la lista filtrada automáticamente.
     */
    val listState: StateFlow<PetListState> = combine(
        repository.observePets(), searchQuery, selectedSpecies
    ) { pets, query, species ->
        val filtered = pets.filter { pet ->
            // "Exóticas" filtra por categoría; "Otro" solo incluye mascotas de compañía de otra especie
            val matchesSpecies = when (species) {
                SPECIES_ALL -> true
                FILTER_EXOTIC -> pet.isExotic
                else -> !pet.isExotic && pet.species == species
            }
            val matchesText = query.isBlank() ||
                pet.name.contains(query.trim(), ignoreCase = true) ||
                pet.description.contains(query.trim(), ignoreCase = true)
            matchesSpecies && matchesText
        }
        PetListState(
            pets = filtered,
            totalCount = pets.size,
            searchQuery = query,
            selectedSpecies = species,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetListState())

    fun onSearchChange(query: String) { searchQuery.value = query }
    fun onSpeciesFilterChange(species: String) { selectedSpecies.value = species }

    // ---------------------------------------------------------------- DETALLE
    fun observePet(id: Int): Flow<PetEntity?> = repository.observePet(id)

    fun deletePet(pet: PetEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deletePet(pet)
            withContext(Dispatchers.IO) { ImageStorage.delete(pet.imageUri) }
            onDeleted()
        }
    }

    // ------------------------------------------------------------- FORMULARIO
    private val _formState = MutableStateFlow(PetFormState())
    val formState: StateFlow<PetFormState> = _formState.asStateFlow()

    fun onNameChange(value: String) = updateForm(FormField.NAME) { copy(name = value) }
    fun onPhoneChange(value: String) = updateForm(FormField.PHONE) { copy(phone = value) }
    fun onAgeChange(value: String) = updateForm(FormField.AGE) { copy(age = value) }
    fun onDescriptionChange(value: String) = updateForm(FormField.DESCRIPTION) { copy(description = value) }
    fun onSpeciesChange(value: String) = _formState.update { it.copy(species = value) }
    fun onImagePicked(uri: Uri?) = _formState.update { it.copy(imageUri = uri?.toString()) }

    // Tipo de mascota y datos legales de las exóticas
    fun onCategoryChange(value: String) = _formState.update { it.copy(category = value).validated() }
    fun onExoticTypeChange(value: String) = _formState.update { it.copy(exoticType = value) }
    fun onExoticSpeciesChange(value: String) = updateForm(FormField.EXOTIC_SPECIES) { copy(exoticSpecies = value) }
    fun onPermitChange(value: String) = updateForm(FormField.PERMIT) { copy(permit = value) }
    fun onLegalDeclaredChange(value: Boolean) = _formState.update { it.copy(legalDeclared = value).validated() }

    // Ubicación donde fue encontrada y (si se sabe) donde se extravió
    fun onFoundPlaceChange(value: String) = updateForm(FormField.FOUND_PLACE) { copy(foundPlace = value) }
    fun onLostPlaceChange(value: String) = updateForm(FormField.LOST_PLACE) { copy(lostPlace = value) }
    fun onFoundLocation(lat: Double?, lng: Double?) = _formState.update { it.copy(foundLat = lat, foundLng = lng) }
    fun onLostLocation(lat: Double?, lng: Double?) = _formState.update { it.copy(lostLat = lat, lostLng = lng) }

    /** Aplica el cambio, marca el campo como "tocado" y revalida todo el formulario. */
    private fun updateForm(field: FormField, change: PetFormState.() -> PetFormState) {
        _formState.update { it.change().copy(touched = it.touched + field).validated() }
    }

    /**
     * REQ. 4 (Validación en tiempo real): reglas centralizadas.
     * - Los mensajes de error solo se muestran en campos que el usuario ya tocó.
     * - isValid se calcula con TODAS las reglas, y la UI lo usa para habilitar el botón.
     */
    private fun PetFormState.validated(): PetFormState {
        val nameMsg = Validators.name(name)
        val phoneMsg = Validators.phone(phone)
        val ageMsg = if (age.length > 20) "Máximo 20 caracteres" else null // edad es opcional
        val descMsg = Validators.description(description)
        // Los datos legales solo se exigen en mascotas exóticas
        val exoticMsg = if (isExotic) Validators.exoticSpecies(exoticSpecies) else null
        val permitMsg = if (isExotic) Validators.permit(permit) else null
        val declared = !isExotic || legalDeclared
        val foundMsg = Validators.optionalPlace(foundPlace)
        val lostMsg = Validators.optionalPlace(lostPlace)
        return copy(
            nameError = nameMsg.takeIf { FormField.NAME in touched },
            phoneError = phoneMsg.takeIf { FormField.PHONE in touched },
            ageError = ageMsg.takeIf { FormField.AGE in touched },
            descriptionError = descMsg.takeIf { FormField.DESCRIPTION in touched },
            exoticSpeciesError = exoticMsg.takeIf { FormField.EXOTIC_SPECIES in touched },
            permitError = permitMsg.takeIf { FormField.PERMIT in touched },
            foundPlaceError = foundMsg.takeIf { FormField.FOUND_PLACE in touched },
            lostPlaceError = lostMsg.takeIf { FormField.LOST_PLACE in touched },
            isValid = nameMsg == null && phoneMsg == null && ageMsg == null && descMsg == null &&
                exoticMsg == null && permitMsg == null && declared && foundMsg == null && lostMsg == null
        )
    }

    fun resetForm() { _formState.value = PetFormState() }

    /**
     * REQ. 5 y 7: guarda en Room (corrutina) copiando la foto de la galería al
     * almacenamiento interno y registrando la fecha/hora actual del sistema.
     */
    fun savePet(onSaved: () -> Unit) {
        val form = _formState.value
        if (!form.isValid || form.isSaving) return
        _formState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val storedImage = withContext(Dispatchers.IO) {
                form.imageUri?.let { ImageStorage.copyToInternal(getApplication(), Uri.parse(it)) }
            }
            repository.savePet(
                PetEntity(
                    name = form.name.trim(),
                    // En exóticas "species" guarda el tipo (Reptil, Ave...) para listar y mostrar el emoji
                    species = if (form.isExotic) form.exoticType else form.species,
                    age = form.age.trim(),
                    description = form.description.trim(),
                    contactPhone = form.phone.trim(),
                    imageUri = storedImage,
                    dateRegistered = DateUtils.currentDateTime(),
                    category = form.category,
                    exoticSpecies = if (form.isExotic) form.exoticSpecies.trim() else "",
                    permitNumber = if (form.isExotic) form.permit.trim() else "",
                    foundPlace = form.foundPlace.trim(),
                    foundLat = form.foundLat,
                    foundLng = form.foundLng,
                    lostPlace = form.lostPlace.trim(),
                    lostLat = form.lostLat,
                    lostLng = form.lostLng
                )
            )
            resetForm()
            onSaved()
        }
    }
}
