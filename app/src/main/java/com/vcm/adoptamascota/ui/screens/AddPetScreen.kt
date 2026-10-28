package com.vcm.adoptamascota.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vcm.adoptamascota.data.local.PetCategory
import com.vcm.adoptamascota.ui.components.PetImage
import com.vcm.adoptamascota.ui.components.PetTextField
import com.vcm.adoptamascota.ui.components.mapsAvailable
import com.vcm.adoptamascota.ui.state.EXOTIC_TYPES
import com.vcm.adoptamascota.ui.state.PetFormState
import com.vcm.adoptamascota.ui.state.SPECIES_OPTIONS
import com.vcm.adoptamascota.ui.state.speciesEmoji
import com.vcm.adoptamascota.ui.viewmodel.PetViewModel
import com.vcm.adoptamascota.utils.Coordinates
import com.vcm.adoptamascota.utils.DateUtils
import com.vcm.adoptamascota.utils.coordinatesOf
import com.vcm.adoptamascota.utils.fetchCurrentLocation
import com.vcm.adoptamascota.utils.hasLocationPermission
import kotlinx.coroutines.launch

private const val SAG_URL = "https://www.sag.gob.cl"

/** Pantalla 3 - Formulario de registro con validación en tiempo real. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPetScreen(
    viewModel: PetViewModel,
    onBack: () -> Unit,
    onPickLocation: (LocationTarget) -> Unit
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // REQ. 7 - RECURSO NATIVO #1 (Galería): GetContent abre el selector de imágenes del sistema
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.onImagePicked(uri) }

    // RECURSO NATIVO: ubicación (GPS). Se pide el permiso solo cuando el usuario pulsa el botón.
    var locating by remember { mutableStateOf<LocationTarget?>(null) }
    var pendingTarget by remember { mutableStateOf<LocationTarget?>(null) }

    fun applyCurrentLocation(target: LocationTarget) {
        scope.launch {
            locating = target
            val here: Coordinates? = fetchCurrentLocation(context)
            locating = null
            if (here == null) {
                snackbar.showSnackbar("No se pudo obtener la ubicación. Activa el GPS e inténtalo de nuevo.")
            } else if (target == LocationTarget.FOUND) {
                viewModel.onFoundLocation(here.lat, here.lng)
            } else {
                viewModel.onLostLocation(here.lat, here.lng)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val target = pendingTarget
        pendingTarget = null
        if (target != null) {
            if (result.values.any { it }) applyCurrentLocation(target)
            else scope.launch { snackbar.showSnackbar("Sin permiso de ubicación no puedo usar tu posición actual.") }
        }
    }

    fun useCurrentLocation(target: LocationTarget) {
        if (hasLocationPermission(context)) {
            applyCurrentLocation(target)
        } else {
            pendingTarget = target
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Registrar mascota") },
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
            // Selector de foto: si no hay imagen elegida, PetImage muestra el ícono por defecto
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { galleryLauncher.launch("image/*") }
            ) {
                PetImage(
                    imageUri = form.imageUri,
                    contentDescription = "Foto de la mascota",
                    modifier = Modifier.size(160.dp)
                )
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Elegir foto de la galería",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            TextButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (form.imageUri == null) "Elegir foto de la galería" else "Cambiar foto")
            }

            // REQ. 4: cada campo muestra su error (isError + supportingText) en cuanto se edita
            PetTextField(
                value = form.name,
                onValueChange = viewModel::onNameChange,
                label = "Nombre *",
                icon = Icons.Filled.Pets,
                error = form.nameError
            )

            // Tipo de mascota: de compañía o exótica
            Text("Tipo de mascota *", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = form.category == PetCategory.COMPANION,
                    onClick = { viewModel.onCategoryChange(PetCategory.COMPANION) },
                    label = { Text("🐶 De compañía") }
                )
                FilterChip(
                    selected = form.category == PetCategory.EXOTIC,
                    onClick = { viewModel.onCategoryChange(PetCategory.EXOTIC) },
                    label = { Text("🦎 Exótica") }
                )
            }

            if (form.isExotic) {
                Text("Tipo de animal exótico *", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    EXOTIC_TYPES.forEach { type ->
                        FilterChip(
                            selected = form.exoticType == type,
                            onClick = { viewModel.onExoticTypeChange(type) },
                            label = { Text("${speciesEmoji(type)} $type") }
                        )
                    }
                }
                PetTextField(
                    value = form.exoticSpecies,
                    onValueChange = viewModel::onExoticSpeciesChange,
                    label = "Nombre de la especie *",
                    icon = Icons.Filled.Science,
                    error = form.exoticSpeciesError,
                    placeholder = "Ej: Iguana verde, Cacatúa"
                )
                ExoticLegalCard(form = form, viewModel = viewModel)
            } else {
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
            }

            PetTextField(
                value = form.age,
                onValueChange = viewModel::onAgeChange,
                label = "Edad (opcional)",
                icon = Icons.Filled.Cake,
                error = form.ageError,
                placeholder = "Ej: 2 años"
            )
            PetTextField(
                value = form.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Teléfono de contacto *",
                icon = Icons.Filled.Phone,
                error = form.phoneError,
                keyboardType = KeyboardType.Phone
            )
            PetTextField(
                value = form.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Descripción *",
                icon = Icons.Filled.Description,
                error = form.descriptionError,
                singleLine = false,
                minLines = 3,
                counter = "${form.description.length}/300"
            )

            // Ubicación: dónde fue encontrada y, si se sabe, dónde se extravió
            Text("Ubicación (opcional)", style = MaterialTheme.typography.titleMedium)
            LocationInput(
                title = "¿Dónde fue encontrada?",
                placeLabel = "Lugar donde fue encontrada",
                place = form.foundPlace,
                onPlaceChange = viewModel::onFoundPlaceChange,
                placeError = form.foundPlaceError,
                coordinates = coordinatesOf(form.foundLat, form.foundLng),
                locating = locating == LocationTarget.FOUND,
                onUseCurrent = { useCurrentLocation(LocationTarget.FOUND) },
                onPickOnMap = { onPickLocation(LocationTarget.FOUND) },
                onClear = { viewModel.onFoundLocation(null, null) }
            )
            LocationInput(
                title = "¿Dónde se extravió? (si se sabe)",
                placeLabel = "Lugar donde se extravió",
                place = form.lostPlace,
                onPlaceChange = viewModel::onLostPlaceChange,
                placeError = form.lostPlaceError,
                coordinates = coordinatesOf(form.lostLat, form.lostLng),
                locating = locating == LocationTarget.LOST,
                onUseCurrent = { useCurrentLocation(LocationTarget.LOST) },
                onPickOnMap = { onPickLocation(LocationTarget.LOST) },
                onClear = { viewModel.onLostLocation(null, null) }
            )

            // REQ. 7 - RECURSO NATIVO #2: la fecha se toma del sistema (DateUtils / java.util.Date)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarToday, contentDescription = null,
                    modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Se registrará con la fecha de hoy: ${DateUtils.currentDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // REQ. 4: el botón se deshabilita mientras el formulario no sea válido
            Button(
                onClick = { viewModel.savePet(onSaved = onBack) },
                enabled = form.isValid && !form.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar y publicar")
                }
            }
        }
    }
}

/** Aviso legal y datos de origen para mascotas exóticas. La app no verifica: solo exige declararlos. */
@Composable
private fun ExoticLegalCard(form: PetFormState, viewModel: PetViewModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Fauna exótica en Chile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "La tenencia, internación y venta de fauna exótica se rige por la normativa chilena " +
                    "(Ley de Caza, CITES y el SAG). Conserva los documentos que acrediten su procedencia legal. " +
                    "VitalMascot no verifica permisos: el refugio debe comprobarlos antes de entregar el animal.",
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SAG_URL)))
            }) { Text("Ver información del SAG") }

            PetTextField(
                value = form.permit,
                onValueChange = viewModel::onPermitChange,
                label = "N° de resolución SAG / permiso CITES / documento *",
                icon = Icons.Filled.VerifiedUser,
                error = form.permitError
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onLegalDeclaredChange(!form.legalDeclared) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = form.legalDeclared,
                    onCheckedChange = viewModel::onLegalDeclaredChange
                )
                Text(
                    "Declaro que el origen del animal es legal y cuento con la documentación.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Bloque de ubicación: texto descriptivo + coordenadas (GPS o mapa) con opción de quitarlas. */
@Composable
private fun LocationInput(
    title: String,
    placeLabel: String,
    place: String,
    onPlaceChange: (String) -> Unit,
    placeError: String?,
    coordinates: Coordinates?,
    locating: Boolean,
    onUseCurrent: () -> Unit,
    onPickOnMap: () -> Unit,
    onClear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            PetTextField(
                value = place,
                onValueChange = onPlaceChange,
                label = placeLabel,
                icon = Icons.Filled.Place,
                error = placeError,
                placeholder = "Ej: Plaza de Armas, Santiago"
            )
            if (coordinates != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("Ubicación guardada: ${coordinates.format()}", modifier = Modifier.weight(1f))
                    TextButton(onClick = onClear) { Text("Quitar") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onUseCurrent,
                    enabled = !locating,
                    modifier = Modifier.weight(1f)
                ) {
                    if (locating) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mi ubicación")
                    }
                }
                // El mapa interactivo solo se ofrece si hay clave de Google Maps configurada
                if (mapsAvailable) {
                    OutlinedButton(onClick = onPickOnMap, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Elegir en mapa")
                    }
                }
            }
        }
    }
}
