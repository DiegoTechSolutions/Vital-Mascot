package com.vcm.adoptamascota.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vcm.adoptamascota.ui.components.PawPrint
import com.vcm.adoptamascota.ui.components.PawScatter
import com.vcm.adoptamascota.ui.theme.AmarilloApp
import com.vcm.adoptamascota.ui.theme.NaranjaApp
import com.vcm.adoptamascota.ui.theme.NaranjaOscuro
import com.vcm.adoptamascota.ui.theme.TextoOscuro
import kotlinx.coroutines.launch

/**
 * Pantalla de login con Google (Firebase Auth).
 * Si ya hay sesión activa avanza sola; "Continuar sin cuenta" permite entrar como invitado.
 */
@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository(context) }

    // Observa si hay un usuario logueado
    val user by repository.authState.collectAsStateWithLifecycle(initialValue = null)
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Apenas hay sesión, se sale de esta pantalla
    LaunchedEffect(user) {
        if (user != null) onLoggedIn()
    }

    Box(modifier = Modifier.fillMaxSize().background(AmarilloApp)) {
    // Huellas de perro y de gato de fondo
    PawScatter(color = NaranjaApp.copy(alpha = 0.16f))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(NaranjaApp),
            contentAlignment = Alignment.Center
        ) {
            PawPrint(modifier = Modifier.size(56.dp), color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text("VitalMascot", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NaranjaOscuro)
        Spacer(Modifier.height(8.dp))
        Text("Inicia sesión", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro)
        Spacer(Modifier.height(8.dp))
        Text(
            "Accede con tu cuenta de Google para guardar tus mascotas",
            textAlign = TextAlign.Center,
            color = TextoOscuro
        )
        Spacer(Modifier.height(32.dp))

        Button(
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    repository.signInWithGoogle(context)
                        .onFailure { error = it.message ?: "No se pudo iniciar sesión" }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = NaranjaApp, contentColor = Color.White)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Iniciar sesión con Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSkip) {
            Text("Continuar sin cuenta", color = TextoOscuro)
        }
    }
    }
}

/** Botón reutilizable de cierre de sesión (úsalo p. ej. en Home). */
@Composable
fun SignOutButton(modifier: Modifier = Modifier, onSignedOut: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository(context) }
    OutlinedButton(
        modifier = modifier,
        onClick = {
            scope.launch {
                repository.signOut()
                onSignedOut()
            }
        }
    ) { Text("Cerrar sesión") }
}
