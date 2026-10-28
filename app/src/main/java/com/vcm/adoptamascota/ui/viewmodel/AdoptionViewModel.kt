package com.vcm.adoptamascota.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vcm.adoptamascota.data.local.AdoptionRequestEntity
import com.vcm.adoptamascota.data.local.AppDatabase
import com.vcm.adoptamascota.data.repository.AdoptionRepository
import com.vcm.adoptamascota.ui.state.AdoptionField
import com.vcm.adoptamascota.ui.state.AdoptionFormState
import com.vcm.adoptamascota.utils.DateUtils
import com.vcm.adoptamascota.utils.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lógica de las solicitudes de adopción: formulario validado y gestión del estado de cada solicitud.
 * La app NO verifica permisos: solo exige que el adoptante los declare cuando la mascota es exótica;
 * quien publicó la mascota decide si aprueba o rechaza la solicitud.
 */
class AdoptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdoptionRepository(AppDatabase.getInstance(application).adoptionRequestDao())

    private val _form = MutableStateFlow(AdoptionFormState())
    val form: StateFlow<AdoptionFormState> = _form.asStateFlow()

    fun observeRequests(petId: Int): Flow<List<AdoptionRequestEntity>> = repository.observeRequests(petId)

    /** Reinicia el formulario para una mascota; precarga nombre y correo si hay sesión iniciada. */
    fun start(requiresPermit: Boolean, name: String = "", email: String = "") {
        _form.value = AdoptionFormState(requiresPermit = requiresPermit, name = name, email = email).validated()
    }

    fun onNameChange(v: String) = updateForm(AdoptionField.NAME) { copy(name = v) }
    fun onPhoneChange(v: String) = updateForm(AdoptionField.PHONE) { copy(phone = v) }
    fun onEmailChange(v: String) = updateForm(AdoptionField.EMAIL) { copy(email = v) }
    fun onMessageChange(v: String) = updateForm(AdoptionField.MESSAGE) { copy(message = v) }
    fun onPermitChange(v: String) = updateForm(AdoptionField.PERMIT) { copy(permit = v) }
    fun onLegalDeclaredChange(v: Boolean) = _form.update { it.copy(legalDeclared = v).validated() }

    private fun updateForm(field: AdoptionField, change: AdoptionFormState.() -> AdoptionFormState) {
        _form.update { it.change().copy(touched = it.touched + field).validated() }
    }

    private fun AdoptionFormState.validated(): AdoptionFormState {
        val nameMsg = Validators.name(name, "El nombre")
        val phoneMsg = Validators.phone(phone)
        val emailMsg = Validators.email(email)
        val messageMsg = Validators.message(message)
        val permitMsg = if (requiresPermit) Validators.permit(permit) else null
        val declared = !requiresPermit || legalDeclared
        return copy(
            nameError = nameMsg.takeIf { AdoptionField.NAME in touched },
            phoneError = phoneMsg.takeIf { AdoptionField.PHONE in touched },
            emailError = emailMsg.takeIf { AdoptionField.EMAIL in touched },
            messageError = messageMsg.takeIf { AdoptionField.MESSAGE in touched },
            permitError = permitMsg.takeIf { AdoptionField.PERMIT in touched },
            isValid = nameMsg == null && phoneMsg == null && emailMsg == null &&
                messageMsg == null && permitMsg == null && declared
        )
    }

    fun submit(petId: Int, onSaved: () -> Unit) {
        val f = _form.value
        if (!f.isValid || f.isSaving) return
        _form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveRequest(
                AdoptionRequestEntity(
                    petId = petId,
                    applicantName = f.name.trim(),
                    applicantPhone = f.phone.trim(),
                    applicantEmail = f.email.trim(),
                    message = f.message.trim(),
                    permitNumber = if (f.requiresPermit) f.permit.trim() else "",
                    dateRequested = DateUtils.currentDateTime()
                )
            )
            _form.update { it.copy(isSaving = false) }
            onSaved()
        }
    }

    fun updateStatus(request: AdoptionRequestEntity, status: String) {
        viewModelScope.launch { repository.updateStatus(request.id, status) }
    }
}
