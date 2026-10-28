package com.vcm.adoptamascota.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vcm.adoptamascota.data.local.AppDatabase
import com.vcm.adoptamascota.data.local.LostPetEntity
import com.vcm.adoptamascota.data.repository.LostPetRepository
import com.vcm.adoptamascota.ui.state.FormField
import com.vcm.adoptamascota.ui.state.LostFormState
import com.vcm.adoptamascota.utils.DateUtils
import com.vcm.adoptamascota.utils.ImageStorage
import com.vcm.adoptamascota.utils.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** ViewModel de "Animales Perdidos": misma arquitectura MVVM que PetViewModel. */
class LostPetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LostPetRepository(AppDatabase.getInstance(application).lostPetDao())

    /** Lista reactiva: Room avisa por Flow cada vez que cambia la tabla. */
    val lostPets: StateFlow<List<LostPetEntity>?> = repository.observeLostPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null) // null = cargando

    fun markFound(pet: LostPetEntity) = viewModelScope.launch { repository.markFound(pet) }

    fun delete(pet: LostPetEntity) = viewModelScope.launch {
        repository.delete(pet)
        withContext(Dispatchers.IO) { ImageStorage.delete(pet.imageUri) }
    }

    // ------------------------------------------------------------- FORMULARIO
    private val _form = MutableStateFlow(LostFormState())
    val form: StateFlow<LostFormState> = _form.asStateFlow()

    fun onNameChange(v: String) = edit(FormField.NAME) { copy(name = v) }
    fun onLastSeenChange(v: String) = edit(FormField.LOCATION) { copy(lastSeen = v) }
    fun onPhoneChange(v: String) = edit(FormField.PHONE) { copy(phone = v) }
    fun onDescriptionChange(v: String) = edit(FormField.DESCRIPTION) { copy(description = v) }
    fun onSpeciesChange(v: String) = _form.update { it.copy(species = v) }
    fun onImagePicked(uri: Uri?) = _form.update { it.copy(imageUri = uri?.toString()) }

    private fun edit(field: FormField, change: LostFormState.() -> LostFormState) {
        _form.update { it.change().copy(touched = it.touched + field).validated() }
    }

    private fun LostFormState.validated(): LostFormState {
        val n = Validators.name(name, "El nombre de la mascota")
        val l = Validators.location(lastSeen)
        val p = Validators.phone(phone)
        val d = Validators.description(description)
        return copy(
            nameError = n.takeIf { FormField.NAME in touched },
            lastSeenError = l.takeIf { FormField.LOCATION in touched },
            phoneError = p.takeIf { FormField.PHONE in touched },
            descriptionError = d.takeIf { FormField.DESCRIPTION in touched },
            isValid = n == null && l == null && p == null && d == null
        )
    }

    fun resetForm() { _form.value = LostFormState() }

    fun report(onSaved: () -> Unit) {
        val f = _form.value
        if (!f.isValid || f.isSaving) return
        _form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val image = withContext(Dispatchers.IO) {
                f.imageUri?.let { ImageStorage.copyToInternal(getApplication(), Uri.parse(it)) }
            }
            repository.report(
                LostPetEntity(
                    name = f.name.trim(),
                    species = f.species,
                    lastSeen = f.lastSeen.trim(),
                    description = f.description.trim(),
                    contactPhone = f.phone.trim(),
                    imageUri = image,
                    dateReported = DateUtils.currentDateTime()
                )
            )
            resetForm()
            onSaved()
        }
    }
}
