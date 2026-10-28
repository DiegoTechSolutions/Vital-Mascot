package com.vcm.adoptamascota.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.vcm.adoptamascota.ui.components.PawBackButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vcm.adoptamascota.data.local.PetEntity
import com.vcm.adoptamascota.ui.components.PetImage
import com.vcm.adoptamascota.ui.state.SPECIES_FILTERS
import com.vcm.adoptamascota.ui.state.speciesEmoji
import com.vcm.adoptamascota.ui.components.PawPrint
import com.vcm.adoptamascota.ui.viewmodel.PetViewModel

/**
 * Pantalla 1 - Home. REQ. 1 (Material 3): Scaffold + TopAppBar + FAB + Cards + FilterChips.
 * La pantalla no tiene lógica: lee listState y avisa eventos al ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionListScreen(
    viewModel: PetViewModel,
    onBack: () -> Unit,
    onAddPet: () -> Unit,
    onPetClick: (Int) -> Unit
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Pets, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adopción", style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    PawBackButton(onClick = onBack)
                },
                // Barra amarilla mostaza, igual que el resto de la app
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPet,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text("Registrar") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // REQ. 6: barra de búsqueda por texto (filtra por nombre o descripción)
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nombre o descripción") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    // El botón "limpiar" aparece/desaparece con animación
                    AnimatedVisibility(
                        visible = state.searchQuery.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            // REQ. 6: chips para filtrar por especie
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SPECIES_FILTERS) { species ->
                    FilterChip(
                        selected = state.selectedSpecies == species,
                        onClick = { viewModel.onSpeciesFilterChange(species) },
                        label = { Text("${speciesEmoji(species)} $species") }
                    )
                }
            }

            Text(
                text = "${state.pets.size} de ${state.totalCount} mascotas",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Transición animada entre los 3 estados: cargando / vacío / lista
            val screen = when {
                state.isLoading -> ListContent.LOADING
                state.pets.isEmpty() -> ListContent.EMPTY
                else -> ListContent.LIST
            }
            Crossfade(targetState = screen, label = "listContent") { current ->
                when (current) {
                    ListContent.LOADING -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    ListContent.EMPTY -> EmptyState(hasPets = state.totalCount > 0)

                    ListContent.LIST -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.pets, key = { it.id }) { pet ->
                            // animateItem: las tarjetas se reordenan/aparecen con animación al filtrar
                            PetCard(
                                pet = pet,
                                onClick = { onPetClick(pet.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class ListContent { LOADING, EMPTY, LIST }

@Composable
private fun PetCard(pet: PetEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetImage(
                imageUri = pet.imageUri,
                contentDescription = "Foto de ${pet.name}",
                modifier = Modifier.size(88.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${speciesEmoji(pet.species)} ${pet.species}" +
                        (if (pet.isExotic) " · Exótica" else "") +
                        (if (pet.age.isNotBlank()) " · ${pet.age}" else ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pet.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = pet.dateRegistered,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Garrita: indica que la tarjeta se puede abrir para ver el detalle
            PawPrint(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Estado vacío: distingue "no hay mascotas aún" de "los filtros no encontraron nada". */
@Composable
private fun EmptyState(hasPets: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasPets) Icons.Filled.SearchOff else Icons.Filled.Pets,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (hasPets) "Sin resultados" else "Aún no hay mascotas",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (hasPets) "Prueba con otra búsqueda o cambia el filtro."
            else "Toca «Registrar» para agregar la primera mascota rescatada.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


