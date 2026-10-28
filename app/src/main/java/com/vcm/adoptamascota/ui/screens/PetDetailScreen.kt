package com.vcm.adoptamascota.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.OutlinedButton
import com.vcm.adoptamascota.data.local.AdoptionRequestEntity
import com.vcm.adoptamascota.data.local.RequestStatus
import com.vcm.adoptamascota.ui.components.PetImage
import com.vcm.adoptamascota.ui.components.PetLocationMap
import com.vcm.adoptamascota.ui.state.categoryLabel
import com.vcm.adoptamascota.ui.state.speciesEmoji
import com.vcm.adoptamascota.ui.viewmodel.AdoptionViewModel
import com.vcm.adoptamascota.ui.viewmodel.PetViewModel
import com.vcm.adoptamascota.utils.coordinatesOf

/** Pantalla 2 - Detalle completo de la mascota, con opción de llamar y de eliminar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: Int,
    viewModel: PetViewModel,
    adoptionViewModel: AdoptionViewModel,
    onRequestAdoption: () -> Unit,
    onBack: () -> Unit
) {
    val pet by remember(petId) { viewModel.observePet(petId) }.collectAsStateWithLifecycle(initialValue = null)
    val requests by remember(petId) { adoptionViewModel.observeRequests(petId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Animación de entrada del contenido (se activa una vez al abrir la pantalla)
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "Detalle") },
                navigationIcon = {
                    PawBackButton(onClick = onBack)
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }, enabled = pet != null) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar mascota")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { padding ->
        val current = pet
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically { it / 8 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                PetImage(
                    imageUri = current.imageUri,
                    contentDescription = "Foto de ${current.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
                Spacer(Modifier.height(16.dp))

                Text(current.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${speciesEmoji(current.species)} ${current.species}") }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(categoryLabel(current.category)) }
                    )
                }
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        InfoRow(
                            Icons.Filled.Cake, "Edad",
                            current.age.ifBlank { "No informada" }
                        )
                        InfoRow(Icons.Filled.Phone, "Contacto", current.contactPhone)
                        InfoRow(Icons.Filled.CalendarToday, "Registrada el", current.dateRegistered)
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text("Sobre ${current.name}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(current.description, style = MaterialTheme.typography.bodyLarge)

                // Mascota exótica: datos del origen legal declarado por quien la publicó
                if (current.isExotic) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Fauna exótica", style = MaterialTheme.typography.titleMedium)
                            InfoRow(Icons.Filled.Pets, "Especie", current.exoticSpecies.ifBlank { current.species })
                            InfoRow(Icons.Filled.VerifiedUser, "Permiso / documento declarado", current.permitNumber)
                            Text(
                                "Documento declarado por quien publicó, pendiente de verificación. " +
                                    "Para adoptar, deberás declarar tu propio permiso o documento conforme a la " +
                                    "normativa chilena (Ley de Caza, CITES y SAG).",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Ubicación: dónde fue encontrada y dónde se extravió (si se sabe)
                val found = coordinatesOf(current.foundLat, current.foundLng)
                val lost = coordinatesOf(current.lostLat, current.lostLng)
                if (found != null || lost != null || current.foundPlace.isNotBlank() || current.lostPlace.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Ubicación", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (current.foundPlace.isNotBlank()) {
                        InfoRow(Icons.Filled.Place, "Encontrada en", current.foundPlace)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (current.lostPlace.isNotBlank()) {
                        InfoRow(Icons.Filled.Place, "Se extravió en", current.lostPlace)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (found != null || lost != null) {
                        PetLocationMap(found = found, lost = lost)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Solicitud de adopción (si la mascota es exótica, el formulario pide el permiso del adoptante)
                Button(
                    onClick = onRequestAdoption,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Solicitar adopción")
                }
                Spacer(Modifier.height(8.dp))
                // Abre el marcador del teléfono con el número del refugio
                OutlinedButton(
                    onClick = {
                        val number = current.contactPhone.filter { it.isDigit() || it == '+' }
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar al contacto")
                }

                // Solicitudes recibidas: quien publicó decide si las aprueba o las rechaza
                if (requests.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("Solicitudes de adopción (${requests.size})", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        requests.forEach { request ->
                            RequestCard(
                                request = request,
                                showPermit = current.isExotic,
                                onApprove = { adoptionViewModel.updateStatus(request, RequestStatus.APPROVED) },
                                onReject = { adoptionViewModel.updateStatus(request, RequestStatus.REJECTED) }
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Filled.Pets, contentDescription = null) },
                title = { Text("Eliminar registro") },
                text = { Text("¿Seguro que deseas eliminar a ${current.name}? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deletePet(current, onDeleted = onBack)
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

/** Tarjeta de una solicitud de adopción con su estado y, si está pendiente, acciones para resolverla. */
@Composable
private fun RequestCard(
    request: AdoptionRequestEntity,
    showPermit: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    request.applicantName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(onClick = {}, label = { Text(request.status) })
            }
            Text("${request.applicantPhone} · ${request.applicantEmail}", style = MaterialTheme.typography.bodySmall)
            Text(request.message, style = MaterialTheme.typography.bodyMedium)
            if (showPermit && request.permitNumber.isNotBlank()) {
                Text(
                    "Permiso/documento declarado: ${request.permitNumber} (pendiente de verificación)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(request.dateRequested, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (request.status == RequestStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApprove) { Text("Aprobar") }
                    OutlinedButton(onClick = onReject) { Text("Rechazar") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

