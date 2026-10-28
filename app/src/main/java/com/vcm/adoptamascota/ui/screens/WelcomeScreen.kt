package com.vcm.adoptamascota.ui.screens

import com.vcm.adoptamascota.ui.theme.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcm.adoptamascota.ui.components.PawPrint
import com.vcm.adoptamascota.ui.components.PawScatter

/** Pantalla 1 - Bienvenida: fondo mostaza con huellas, logo arriba, mascota grande al centro y botón "Comenzar". */
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    // Animación infinita: la mascota "rebota" suavemente para dar vida a la pantalla
    val transition = rememberInfiniteTransition(label = "bounce")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounceOffset"
    )

    Box(modifier = Modifier.fillMaxSize().background(AmarilloApp)) {
        // Huellas de perro y de gato repartidas por el fondo
        PawScatter(color = NaranjaApp.copy(alpha = 0.16f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (huella + nombre de la app)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(NaranjaApp),
                    contentAlignment = Alignment.Center
                ) {
                    PawPrint(modifier = Modifier.size(28.dp), color = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Text("VitalMascot", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro)
            }

            Spacer(Modifier.weight(1f))

            // Imagen grande de mascota (ilustración con huella + emojis)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(y = bounce.dp)
                    .clip(CircleShape)
                    .background(RosadoSuave),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(210.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    PawPrint(modifier = Modifier.size(120.dp), color = NaranjaApp)
                }
                Text("🐶", fontSize = 56.sp, modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
                Text("🐱", fontSize = 56.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp))
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "Rescata, adopta\ny ama a tu mascota",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextoOscuro
            )
            Spacer(Modifier.height(12.dp))
            // Detalles: huella de perro, hueso y huella de gato
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PawPrint(modifier = Modifier.size(22.dp), color = NaranjaApp)
                Text("🦴", fontSize = 22.sp)
                PawPrint(modifier = Modifier.size(22.dp), color = NaranjaApp, cat = true)
            }

            Spacer(Modifier.weight(1f))

            // Botón principal naranja redondeado (rol "primary" del tema)
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaApp, contentColor = Color.White)
            ) {
                Text("Comenzar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
