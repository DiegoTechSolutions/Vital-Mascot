package com.vcm.adoptamascota.ui.screens

import com.vcm.adoptamascota.ui.theme.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcm.adoptamascota.ui.components.PawScatter
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

private val pages = listOf(
    OnboardingPage(
        "🐕", "¿Quieres disfrutar con tu mascota?",
        "Descubre perros y gatos rescatados que buscan un hogar lleno de cariño.",
        listOf(AmarilloApp, NaranjaApp)
    ),
    OnboardingPage(
        "🏠", "Registra animales rescatados",
        "Sube una foto, sus datos y tu contacto para que más personas los conozcan.",
        listOf(CelesteApp, CelesteSuave)
    ),
    OnboardingPage(
        "❤️", "Adopta y cambia una vida",
        "Busca, filtra y contacta al refugio con un solo toque.",
        listOf(RosadoSuave, AmarilloSuave)
    )
)

/** Pantalla 2 - Onboarding: 3 páginas deslizables con fondo degradado, botón OMITIR y puntos de paginación. */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.size - 1

    Box(modifier = Modifier.fillMaxSize()) {
        // Páginas deslizables (cada una tiene un fondo degradado: placeholder de imagen a pantalla completa)
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            val page = pages[index]
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(page.gradient))) {
            // Huellas de perro y de gato de fondo
            PawScatter(color = Color.White.copy(alpha = 0.30f))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(page.emoji, fontSize = 120.sp)
                Spacer(Modifier.height(32.dp))
                Text(
                    page.title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center, color = TextoOscuro
                )
                Spacer(Modifier.height(12.dp))
                Text(page.subtitle, fontSize = 16.sp, textAlign = TextAlign.Center, color = TextoOscuro.copy(alpha = 0.9f))
            }
            }
        }

        // OMITIR arriba a la derecha
        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)
        ) {
            Text("OMITIR", color = TextoOscuro, fontWeight = FontWeight.Bold)
        }

        // Puntos de paginación + botón inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val selected = pagerState.currentPage == i
                    val width by animateDpAsState(if (selected) 28.dp else 10.dp, label = "dotWidth")
                    val color by animateColorAsState(if (selected) NaranjaApp else TextoOscuro.copy(alpha = 0.25f), label = "dotColor")
                    Box(Modifier.height(10.dp).size(width, 10.dp).clip(CircleShape).background(color))
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaApp, contentColor = Color.White)
            ) {
                Text(if (isLast) "Empezar" else "Siguiente", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}



