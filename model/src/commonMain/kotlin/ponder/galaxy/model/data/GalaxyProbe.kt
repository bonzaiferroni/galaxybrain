package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class GalaxyProbe(
    val galaxyId: GalaxyId,
    val starLogs: List<StarLog>
)