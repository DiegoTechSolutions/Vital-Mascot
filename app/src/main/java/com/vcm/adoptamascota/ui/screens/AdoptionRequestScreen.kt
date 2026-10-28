package com.vcm.adoptamascota.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.vcm.adoptamascota.ui.components.PawBackButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.vcm.adoptamascota.ui.components.PetTextField
import com.vcm.adoptamascota.ui.viewmodel.AdoptionViewModel
import com.vcm.adoptamascota.ui.viewmodel.PetViewModel

/**
 * Solicitud de adopción. Si la mascota es exótica, el adoptante debe declarar su permiso o
 * documento y aceptar que cumple la normativa chilena; la app no lo verifica, lo revisa el refugio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
    petId: Int,
    petViewModel: PetViewModel,
    adoptionViewModel: AdoptionViewModel,
    onBack: () -> Unit
) {
    val pet by remember(petId) { petViewModel.observePet(petId) }.collectAsStateWithLifecycle(initialValue = null)
    val form by adoptionViewModel.form.collectAsStateWithLifecycle()
    var sent by remember { mutableStateOf(false) }
    val current = pet

    // Cuando carga la mascota, se reinicia el formulario (precargando los datos de la cuenta de Google)
    LaunchedEffect(current?.id, current?.isExotic) {
        if (current != null) {
            val user = FirebaseAuth.getInstance().currentUser
            adoptionViewModel.start(
                requiresPermit = current.isExotic,
                name = user?.displayName.orEmpty(),
                email = user?.email.orEmpty()
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar adopción") },
                navigationIcon = {
                    PawBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { padding ->
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Quieres adoptar a ${current.name}", style = MaterialTheme.typography.titleLarge)
            Text(
                "Completa tus datos. Quien publicó la mascota revisará tu solicitud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PetTextField(
                value = form.name,
                onValueChange = adoptionViewModel::onNameChange,
                label = "Tu nombre *",
                icon = Icons.Filled.Person,
                error = form.nameError
            )
            PetTextField(
                value = form.phone,
                onValueChange = adoptionViewModel::onPhoneChange,
                label = "Tu teléfono *",
                icon = Icons.Filled.Phone,
                error = form.phoneError,
                keyboardType = KeyboardType.Phone
            )
            PetTextField(
                value = form.email,
                onValueChange = adoptionViewModel::onEmailChange,
                label = "Tu correo *",
                icon = Icons.Filled.Email,
                error = form.emailError,
                keyboardType = KeyboardType.Email
            )
            PetTextField(
                value = form.message,
                onValueChange = adoptionViewModel::onMessageChange,
                label = "¿Por qué quieres adoptar? *",
                icon = Icons.Filled.Description,
                error = form.messageError,
                singleLine = false,
                minLines = 3,
                counter = "${form.message.length}/300"
            )

            // Solo para mascotas exóticas: permiso o documento del adoptante
            if (form.requiresPermit) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Mascota exótica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "La tenencia de fauna exótica en Chile se rige por la Ley de Caza, CITES y el SAG. " +
                                "Indica el permiso o documento que respalda tu tenencia. VitalMascot no lo verifica: " +
                                "el refugio lo revisará antes de entregar el animal.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        PetTextField(
                            value = form.permit,
                            onValueChange = adoptionViewModel::onPermitChange,
                            label = "N° de permiso o documento *",
                            icon = Icons.Filled.VerifiedUser,
                            error = form.permitError
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { adoptionViewModel.onLegalDeclaredChange(!form.legalDeclared) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = form.legalDeclared,
                                onCheckedChange = adoptionViewModel::onLegalDeclaredChange
                            )
                            Text(
                                "Declaro que cumplo la normativa vigente para tener este animal.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { adoptionViewModel.submit(petId) { sent = true } },
                enabled = form.isValid && !form.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar solicitud")
                }
            }
        }

        if (sent) {
            AlertDialog(
                onDismissRequest = {},
                icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                title = { Text("Solicitud enviada") },
                text = { Text("Quedó como PENDIENTE. Quien publicó a ${current.name} revisará tu solicitud.") },
                confirmButton = { TextButton(onClick = onBack) { Text("Entendido") } }
            )
        }
    }
}
