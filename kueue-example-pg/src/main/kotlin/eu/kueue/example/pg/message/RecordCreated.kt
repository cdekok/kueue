package eu.kueue.example.pg.message

import eu.kueue.Message
import kotlinx.serialization.Serializable

@Serializable
data class TestMessage(
    val id: Int,
    val title: String,
) : Message

@Serializable
data class RecordCreated(
    val id: Int,
    val title: String,
) : Message
