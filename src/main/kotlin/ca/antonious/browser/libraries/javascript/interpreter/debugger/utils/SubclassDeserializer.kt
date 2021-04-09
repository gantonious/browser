package ca.antonious.browser.libraries.javascript.interpreter.debugger.utils

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class SubclassDeserializer<T> constructor(
    private val typeFieldName: String,
    private val classMap: Map<String, Class<out T>>
) : JsonDeserializer<T> {

    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        jsonDeserializationContext: JsonDeserializationContext
    ): T {
        val jsonObject = jsonElement.asJsonObject
        val typeElement = jsonObject[typeFieldName]
        val typeClass = classMap[typeElement.asString]
            ?: error("Attempted to deserialize unknown subtype: ${typeElement.asString}")

        return Gson().fromJson(jsonObject, typeClass)
    }
}
