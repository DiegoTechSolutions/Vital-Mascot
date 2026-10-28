package com.vcm.adoptamascota.ui.screens

import com.vcm.adoptamascota.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vcm.adoptamascota.ui.components.PetImage
import com.vcm.adoptamascota.ui.components.PetTextField
import com.vcm.adoptamascota.ui.state.SPECIES_OPTIONS
import com.vcm.adoptamascota.ui.state.speciesEmoji
import com.vcm.adoptamascota.ui.viewmodel.LostPetViewModel

/** Formulario "Reportar animal perdido": mismos recursos nativos (galería + fecha del sistema) y validación en vivo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLostPetScreen(
    viewModel: LostPetViewModel,
    onBack: () -> Unit
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar animal perdido") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PetImage(
                imageUri = form.imageUri,
                contentDescription = "Foto del animal",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(160.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { gallery.launch("image/*") }
            )
            TextButton(
                onClick = { gallery.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text(if (form.imageUri == null) "Elegir foto de la galería" else "Cambiar foto") }

            PetTextField(form.name, viewModel::onNameChange, "Nombre de la mascota *", Icons.Filled.Pets, form.nameError)

            Text("Especie *", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SPECIES_OPTIONS.forEach { species ->
                    FilterChip(
                        selected = form.species == species,
                        onClick = { viewModel.onSpeciesChange(species) },
                        label = { Text("${speciesEmoji(species)} $species") }
                    )
                }
            }

            PetTextField(
                form.lastSeen, viewModel::onLastSeenChange, "Lugar donde se perdió *",
                Icons.Filled.LocationOn, form.lastSeenError, placeholder = "Ej: Plaza de Armas, cerca de la fuente"
            )
            PetTextField(
                form.phone, viewModel::onPhoneChange, "Teléfono de contacto *",
                Icons.Filled.Phone, form.phoneError, keyboardType = KeyboardType.Phone
            )
            PetTextField(
                form.description, viewModel::onDescriptionChange, "Descripción (color, collar, señas) *",
                Icons.Filled.Description, form.descriptionError,
                singleLine = false, minLines = 3, counter = "${form.description.length}/300"
            )

            Button(
                onClick = { viewModel.report(onSaved = onBack) },
                enabled = form.isValid && !form.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaApp, contentColor = Color.White)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Filled.Report, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Publicar reporte")
                }
            }
        }
    }
}



