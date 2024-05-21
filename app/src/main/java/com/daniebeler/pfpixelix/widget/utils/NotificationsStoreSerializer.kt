package com.daniebeler.pfpixelix.widget.utils

import androidx.datastore.core.Serializer
import com.daniebeler.pfpixelix.widget.models.NotificationsStore
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Singleton
class NotificationsStoreSerializer: Serializer<NotificationsStore> {
    override val defaultValue: NotificationsStore
        get() = NotificationsStore()

    override suspend fun readFrom(input: InputStream): NotificationsStore {
        return try {
            Json.decodeFromString(
                deserializer = NotificationsStore.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace();
            defaultValue
        }
    }

    override suspend fun writeTo(t: NotificationsStore, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = NotificationsStore.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}