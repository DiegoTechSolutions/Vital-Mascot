package com.vcm.adoptamascota.ui.screens

import com.vcm.adoptamascota.ui.theme.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.House
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.vcm.adoptamascota.auth.AuthRepository
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import com.vcm.adoptamascota.auth.SignOutButton
import com.vcm.adoptamascota.ui.components.PawPrint
import com.vcm.adoptamascota.ui.components.PawScatter
import com.vcm.adoptamascota.data.ServicesData
import com.vcm.adoptamascota.ui.viewmodel.LostPetViewModel
import com.vcm.adoptamascota.ui.viewmodel.PetViewModel

private const val GUEST_NAME = "Invitado"
private const val POINTS_PER_PET = 10

private fun initials(name: String) =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

/** Pantalla 3 - Dashboard con banners, grilla de servicios y barra de navegación inferior. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PetViewModel,
    lostViewModel: LostPetViewModel,
    onAdopt: () -> Unit,
    onLost: () -> Unit,
    onService: (String) -> Unit,
    onLogin: () -> Unit
) {
    // Sesión de Firebase: null = invitado
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val user by authRepository.authState.collectAsStateWithLifecycle(initialValue = null)
    val userName = user?.displayName?.takeIf { it.isNotBlank() } ?: GUEST_NAME
    val state by viewModel.listState.collectAsStateWithLifecycle()
    val lostPets by lostViewModel.lostPets.collectAsStateWithLifecycle()
    val activeLost = lostPets?.count { !it.isFound } ?: 0
    val points = state.totalCount * POINTS_PER_PET
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            // Iniciales del usuario a la izquierda, estrella con puntos a la derecha
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(initials(userName), 40.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Hola, ${userName.substringBefore(" ")}", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // Sin sesión: acceso directo al login
                    if (user == null) {
                        TextButton(onClick = onLogin) {
                            Text("Iniciar sesión", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(NaranjaApp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Puntos", tint = AmarilloApp, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$points", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                // TopAppBar celeste (rol "secondary" del tema)
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = BlancoApp, tonalElevation = 8.dp) {
                val items = listOf(
                    "Inicio" to Icons.Filled.Home,
                    "Buscar" to Icons.Filled.Search,
                    "Mensajes" to Icons.Filled.Chat,
                    "Perfil" to Icons.Filled.Person
                )
                items.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            // "Buscar" lleva directo al catálogo de adopción (con buscador y filtros)
                            if (index == 1) onAdopt() else selectedTab = index
                        },
                        icon = {
                            // "Inicio" usa una garrita de perro; el resto, íconos conocidos
                            if (index == 0) {
                                PawPrint(
                                    modifier = Modifier.size(26.dp),
                                    color = if (selectedTab == 0) NaranjaApp else TextoSecundario
                                )
                            } else {
                                Icon(icon, contentDescription = label)
                            }
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NaranjaApp,
                            selectedTextColor = NaranjaApp,
                            indicatorColor = RosadoSuave,
                            unselectedIconColor = TextoSecundario,
                            unselectedTextColor = TextoSecundario
                        )
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.padding(padding),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "homeTabs"
        ) { tab ->
            when (tab) {
                2 -> Box(Modifier.fillMaxSize()) {
                    PawScatter(color = NaranjaApp.copy(alpha = 0.10f))
                    MessagesTab(onExplore = onAdopt)
                }
                3 -> Box(Modifier.fillMaxSize()) {
                    PawScatter(color = NaranjaApp.copy(alpha = 0.10f))
                    ProfileTab(
                        name = userName,
                        email = user?.email,
                        loggedIn = user != null,
                        petsCount = state.totalCount,
                        points = points,
                        onLogin = onLogin
                    )
                }
                else -> DashboardTab(
                    petsCount = state.totalCount,
                    lostCount = activeLost,
                    onAdopt = onAdopt,
                    onLost = onLost,
                    onService = onService
                )
            }
        }
    }
}

@Composable
private fun Avatar(text: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(NaranjaApp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4f).sp)
    }
}

/** Pestaña Inicio: 2 banners + título + LazyVerticalGrid de 3 columnas. */
@Composable
private fun DashboardTab(
    petsCount: Int,
    lostCount: Int,
    onAdopt: () -> Unit,
    onLost: () -> Unit,
    onService: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Los banners ocupan las 3 columnas (span completo)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Banner(
                title = "Adoptar",
                subtitle = if (petsCount == 0) "Registra a la primera mascota" else "$petsCount mascotas esperando un hogar",
                icon = Icons.Filled.Pets,
                colors = listOf(NaranjaApp, NaranjaApp),
                contentColor = Color.White,
                buttonText = "Ver mascotas",
                onClick = onAdopt,
                buttonIcon = { PawPrint(modifier = Modifier.size(18.dp), color = NaranjaApp) }
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Banner(
                title = "Animales Perdidos",
                subtitle = if (lostCount == 0) "Ayuda a reunirlos con su familia" else "$lostCount buscando a su familia",
                icon = Icons.Filled.Warning,
                colors = listOf(CelesteApp, CelesteApp),
                contentColor = TextoOscuro,
                buttonText = "Ver reportes",
                onClick = onLost,
                catPaw = true,
                buttonIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "Servicios y mucho más",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(ServicesData.all) { service ->
            Card(
                modifier = Modifier.aspectRatio(1f).clickable { onService(service.name) },
                shape = RoundedCornerShape(16.dp),
                // Tarjeta pastel (rosado / amarillo / celeste) con ícono blanco sobre círculo naranja
                colors = CardDefaults.cardColors(containerColor = service.color)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(NaranjaApp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(service.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(service.name, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun Banner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    contentColor: Color,
    buttonText: String,
    onClick: () -> Unit,
    catPaw: Boolean = false,
    buttonIcon: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Fondo con degradado horizontal propio de cada banner, con una huella como marca de agua
        Box(modifier = Modifier.background(Brush.horizontalGradient(colors))) {
        PawPrint(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-6).dp, y = 16.dp)
                .size(120.dp)
                .rotate(-18f),
            color = contentColor.copy(alpha = 0.13f),
            cat = catPaw
        )
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = contentColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = contentColor.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextoOscuro),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    buttonIcon?.let {
                        it()
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
            Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = 0.85f), modifier = Modifier.size(72.dp))
        }
        }
    }
}

@Composable
private fun MessagesTab(onExplore: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(72.dp), tint = NaranjaApp)
        Spacer(Modifier.height(16.dp))
        Text("Sin mensajes todavía", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Cuando contactes a un refugio, tus conversaciones aparecerán aquí.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onExplore,
            colors = ButtonDefaults.buttonColors(containerColor = NaranjaApp, contentColor = Color.White)
        ) { Text("Explorar mascotas") }
    }
}

@Composable
private fun ProfileTab(
    name: String,
    email: String?,
    loggedIn: Boolean,
    petsCount: Int,
    points: Int,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(initials(name), 96.dp)
        Spacer(Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            if (loggedIn) email.orEmpty() else "Inicia sesión para guardar tu perfil",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        if (loggedIn) {
            SignOutButton()
        } else {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaApp, contentColor = Color.White)
            ) { Text("Iniciar sesión con Google") }
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Mascotas\nregistradas", "$petsCount", Icons.Filled.Pets, Modifier.weight(1f))
            StatCard("Puntos\nacumulados", "$points", Icons.Filled.Star, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Ganas $POINTS_PER_PET puntos por cada mascota que registras.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = NaranjaApp, modifier = Modifier.size(32.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
        }
    }
}


