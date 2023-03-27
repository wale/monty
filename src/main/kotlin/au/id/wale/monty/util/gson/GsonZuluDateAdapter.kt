package au.id.wale.monty.util.gson

import com.google.gson.*
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GsonZuluDateAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    init {
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(dateFormat.format(src))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        try {
            return dateFormat.parse(json.asString)
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }
    }
}