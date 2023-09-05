package pt.ipt.dam2022.projetodam.model.overpass

/**
 * Class that represents single element from the response received from the overpass API
 * Represents only the fields necessary for the app
 */
data class OverpassElement(
    val type: String,
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val center: Center?,
    val nodes: List<Long>?,
){
    // Defines a nested data class named Center to represent center coordinates
    data class Center(
        val lat: Double,
        val lon: Double
    )
}


