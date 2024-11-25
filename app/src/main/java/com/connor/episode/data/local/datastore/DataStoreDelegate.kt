package com.connor.episode.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.primaryConstructor

/**
 * 确保 T 数据类标注 @Serializable
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> protobufDataStore(
    fileName: String,
    defaultValue: T? = null,
): ReadOnlyProperty<Context, DataStore<T>> {
    val v = defaultValue
        ?: T::class.primaryConstructor?.callBy(emptyMap())
        ?: error("Type ${T::class.simpleName} must have a no-arg constructor or provide a default value")

    val serializer = object : Serializer<T> {

        override val defaultValue: T = v

        override suspend fun readFrom(input: InputStream): T {
            return ProtoBuf.decodeFromByteArray(serializer(), input.readBytes())
        }

        override suspend fun writeTo(t: T, output: OutputStream) {
            ProtoBuf.encodeToByteArray(serializer(), t).also { output.write(it) }
        }
    }
    return dataStore(fileName, serializer)
}