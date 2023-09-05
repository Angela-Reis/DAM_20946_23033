package pt.ipt.dam2022.projetodam.model.overpass

/**
 * Class that represents response received from the Overpass API
 */
data class OverpassResponse(
    val elements: List<OverpassElement>
)