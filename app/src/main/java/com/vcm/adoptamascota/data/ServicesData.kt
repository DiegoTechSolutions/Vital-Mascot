package com.vcm.adoptamascota.data

import com.vcm.adoptamascota.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Un elemento de una pantalla de servicio (clínica, playa, evento, producto o consejo). */
data class ServiceItem(
    val title: String,
    val subtitle: String,   // línea corta visible siempre
    val detail: String,     // texto que aparece al expandir la tarjeta
    val tag: String? = null,
    val phone: String? = null
)

data class ServiceInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color,       // tono pastel de la tarjeta del servicio en el Home
    val intro: String,
    val items: List<ServiceItem>,
    val linksToAdoption: Boolean = false
)

/**
 * Contenido de los servicios del Home. Son datos de ejemplo fijos (no vienen de una API):
 * la pantalla genérica ServiceScreen los muestra todos con el mismo diseño.
 */
object ServicesData {
    val all: List<ServiceInfo> = listOf(
        ServiceInfo(
            name = "Veterinario",
            icon = Icons.Filled.MedicalServices,
            color = CelesteSuave,
            intro = "Clínicas y urgencias veterinarias para el cuidado de tu mascota.",
            items = listOf(
                ServiceItem("Clínica Patitas", "Consultas y vacunas · Lun a Sáb",
                    "Atención general, vacunación, desparasitación y control sano. Horario 09:00 a 19:00.",
                    "Consulta general", "+56 2 2000 0001"),
                ServiceItem("Vet 24 Horas", "Urgencias · Todos los días",
                    "Atención de urgencias las 24 horas, hospitalización y cirugía menor.",
                    "Urgencias", "+56 2 2000 0002"),
                ServiceItem("Centro Huellitas", "Esterilización a bajo costo",
                    "Campañas de esterilización y chip de identificación a precio solidario para animales rescatados.",
                    "Solidario", "+56 2 2000 0003")
            )
        ),
        ServiceInfo(
            name = "Playas",
            icon = Icons.Filled.BeachAccess,
            color = AmarilloSuave,
            intro = "Lugares donde puedes pasear con tu perro. Recuerda llevar correa y bolsas.",
            items = listOf(
                ServiceItem("Playa Las Gaviotas", "Sector canino habilitado",
                    "Zona delimitada para perros con acceso a agua dulce. Horario libre antes de las 10:00.", "Pet friendly"),
                ServiceItem("Playa El Sol", "Permitido con correa",
                    "Se permiten mascotas con correa fuera de la temporada alta. Hay basureros y bebederos.", "Con correa"),
                ServiceItem("Caleta Los Lobos", "Paseos tranquilos",
                    "Playa poco concurrida, ideal para perros que se asustan con la multitud.", "Tranquila")
            )
        ),
        ServiceInfo(
            name = "Refugios",
            icon = Icons.Filled.House,
            color = RosadoSuave,
            intro = "Refugios aliados donde puedes colaborar o adoptar.",
            linksToAdoption = true,
            items = listOf(
                ServiceItem("Refugio Huellas de Esperanza", "Perros y gatos · 40 animales",
                    "Recibe donaciones de alimento y mantas. Visitas con cita previa los fines de semana.",
                    "Voluntariado", "+56 2 2000 0010"),
                ServiceItem("Hogar Gatuno", "Especializado en gatos",
                    "Red de casas de acogida para gatos rescatados. Buscan hogares temporales.",
                    "Casas de acogida", "+56 2 2000 0011"),
                ServiceItem("Amigos de Cuatro Patas", "Rescate y rehabilitación",
                    "Rehabilitan animales con maltrato. Necesitan voluntarios para paseos y limpieza.",
                    "Voluntariado", "+56 2 2000 0012")
            )
        ),
        ServiceInfo(
            name = "Eventos",
            icon = Icons.Filled.Event,
            color = CelesteSuave,
            intro = "Actividades comunitarias para mascotas y sus familias.",
            items = listOf(
                ServiceItem("Feria de adopción", "Sábado · 11:00 · Plaza Central",
                    "Conoce animales rescatados listos para adoptar. Trae tu cédula; habrá charla de tenencia responsable.",
                    "Adopción"),
                ServiceItem("Jornada de vacunación", "Domingo · 10:00 · Parque Norte",
                    "Vacunación antirrábica gratuita para perros y gatos. Cupos limitados por orden de llegada.",
                    "Gratis"),
                ServiceItem("Caminata canina", "Último sábado del mes · 09:30",
                    "Paseo grupal de 3 km con tu perro. Se premia al más simpático y se recauda para los refugios.",
                    "Solidario")
            )
        ),
        ServiceInfo(
            name = "Tienda",
            icon = Icons.Filled.ShoppingBag,
            color = AmarilloSuave,
            intro = "Productos esenciales. Parte de lo recaudado va a los refugios.",
            items = listOf(
                ServiceItem("Alimento premium 3 kg", "$12.990",
                    "Alimento balanceado para perros adultos. Con proteínas y omega 3 para pelaje brillante.", "Perros"),
                ServiceItem("Rascador con torre", "$24.990",
                    "Torre de 3 niveles con cueva y juguete colgante. Ideal para gatos activos.", "Gatos"),
                ServiceItem("Collar con chapa grabada", "$5.990",
                    "Collar ajustable con chapa de identificación personalizada con nombre y teléfono.", "Todos")
            )
        ),
        ServiceInfo(
            name = "Consejos",
            icon = Icons.Filled.Lightbulb,
            color = RosadoSuave,
            intro = "Guía rápida para recibir y cuidar a tu nueva mascota.",
            items = listOf(
                ServiceItem("Primer día en casa", "Prepara un lugar tranquilo",
                    "Dale un rincón con cama, agua y comida. Evita visitas y ruidos fuertes los primeros días; deja que explore a su ritmo.", "Adopción"),
                ServiceItem("Vacunas y desparasitación", "Calendario básico",
                    "Cachorros: primeras vacunas a las 6-8 semanas con refuerzos mensuales. Desparasita cada 3 meses y consulta a tu veterinario.", "Salud"),
                ServiceItem("Alimentación", "Porciones y horarios",
                    "Ofrece 2 comidas al día a perros adultos. Evita chocolate, cebolla, uvas y huesos cocidos; siempre agua limpia.", "Salud"),
                ServiceItem("Paseos y ejercicio", "Al menos 30 minutos al día",
                    "El ejercicio diario reduce la ansiedad y la destrucción de muebles. En gatos, usa juegos con cañas y rascadores.", "Bienestar")
            )
        )
    )

    fun find(name: String): ServiceInfo? = all.firstOrNull { it.name == name }
}



