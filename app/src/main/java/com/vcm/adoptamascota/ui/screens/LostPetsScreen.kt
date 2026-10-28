package com.vcm.adoptamascota.ui.screens

import com.vcm.adoptamascota.ui.theme.*
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.vcm.adoptamascota.ui.components.PawBackButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vcm.adoptamascota.data.local.LostPetEntity
import com.vcm.adoptamascota.ui.components.PetImage
import com.vcm.adoptamascota.ui.state.speciesEmoji
import com.vcm.adoptamascota.ui.viewmodel.LostPetViewModel

private val LostRed = Color(0xFFC62828)
private val FoundGreen = Color(0xFF2E7D32)

private enum class LostFilter(val label: String) { ALL("Todos"), LOST("Perdidos"), FOUND("Encontrados") }

/** Listado de animales perdidos (Room) con filtro por estado, llamada al dueño y acciones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetsScreen(
    viewModel: LostPetViewModel,
    onBack: () -> Unit,
    onReport: () -> Unit
) {
    val pets by viewModel.lostPets.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(LostFilter.ALL) }
    var toDelete by remember { mutableStateOf<LostPetEntity?>(null) }

    val shown = pets?.filter {
        when (filter) {
            LostFilter.ALL -> true
            LostFilter.LOST -> !it.isFound
            LostFilter.FOUND -> it.isFound
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animales Perdidos") },
                navigationIcon = {
                    PawBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onReport,
                containerColor = NaranjaApp,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text("Reportar") }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LostFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.label) }
                    )
                }
            }

            val screen = when {
                shown == null -> 0
                shown.isEmpty() -> 1
                else -> 2
            }
            Crossfade(targetState = screen, label = "lostContent") { current ->
                when (current) {
                    0 -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    1 -> Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.SentimentSatisfied, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Text("No hay reportes aquí", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Si perdiste o viste un animal perdido, toca «Reportar».",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(shown.orEmpty(), key = { it.id }) { pet ->
                            LostPetCard(
                                pet = pet,
                                onFound = { viewModel.markFound(pet) },
                                onDelete = { toDelete = pet },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { pet ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar reporte") },
            text = { Text("¿Eliminar el reporte de ${pet.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(pet); toDelete = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun LostPetCard(
    pet: LostPetEntity,
    onFound: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statusColor = if (pet.isFound) FoundGreen else LostRed

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PetImage(pet.imageUri, Modifier.size(96.dp), "Foto de ${pet.name}")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${speciesEmoji(pet.species)} ${pet.species}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (pet.isFound) "ENCONTRADO" else "PERDIDO",
                        color = statusColor, fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar reporte")
                }
            }
            Spacer(Modifier.height(8.dp))
            IconText(Icons.Filled.LocationOn, "Visto en: ${pet.lastSeen}")
            IconText(Icons.Filled.CalendarToday, "Reportado el ${pet.dateReported}")
            Spacer(Modifier.height(6.dp))
            Text(pet.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val number = pet.contactPhone.filter { it.isDigit() || it == '+' }
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                }) {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Llamar")
                }
                if (!pet.isFound) {
                    Button(
                        onClick = onFound,
                        colors = ButtonDefaults.buttonColors(containerColor = FoundGreen, contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ya apareció")
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



