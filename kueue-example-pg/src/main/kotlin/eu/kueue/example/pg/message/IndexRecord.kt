package eu.kueue.example.pg.message

import eu.kueue.Message
import kotlinx.serialization.Serializable

@Serializable
data class IndexRecord(
    val id: Int,
) : Message
