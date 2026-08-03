package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable(with = IngredientDtoSerializer::class)
data class IngredientDto(
    @PropertyName("name") val name: String = "",
    @PropertyName("quantity") val quantity: String = ""
)

@Serializable
private data class IngredientSurrogate(
    val name: String = "",
    val quantity: String = ""
)

/**
 * Decodes both the current object form and the pre-migration string form of a cached ingredient.
 *
 * The DataStore cache is one JSON blob covering all of [com.cook.easypan.easypan.data.datastore.AppSettings],
 * so a payload written before ingredients gained quantities (`"ingredients":["Chicken"]`) would
 * otherwise fail to decode and take the user's other settings down with it via the
 * `ReplaceFileCorruptionHandler`. Always *writes* the object form.
 */
object IngredientDtoSerializer : KSerializer<IngredientDto> {

    private val surrogate = IngredientSurrogate.serializer()

    override val descriptor: SerialDescriptor = surrogate.descriptor

    override fun deserialize(decoder: Decoder): IngredientDto {
        val jsonDecoder = decoder as? JsonDecoder ?: return surrogate.deserialize(decoder).toDto()
        val element = jsonDecoder.decodeJsonElement()
        return when {
            element is JsonNull -> IngredientDto()
            element is JsonPrimitive -> IngredientDto(name = element.content.trim())
            else -> jsonDecoder.json.decodeFromJsonElement(surrogate, element).toDto()
        }
    }

    override fun serialize(encoder: Encoder, value: IngredientDto) {
        surrogate.serialize(encoder, IngredientSurrogate(value.name, value.quantity))
    }

    private fun IngredientSurrogate.toDto() = IngredientDto(name = name, quantity = quantity)
}
