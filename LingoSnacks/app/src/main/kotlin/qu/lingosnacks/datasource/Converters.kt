package qu.lingosnacks.datasource

import android.net.Uri
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import qu.lingosnacks.entity.Definition
import qu.lingosnacks.entity.Resource
import qu.lingosnacks.entity.Sentence
import qu.lingosnacks.entity.Word

@ProvidedTypeConverter
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    //====================================Date======================================
    // Convert from Date to a value that can be stored in SQLite Database
    @TypeConverter
    fun fromDate(date: LocalDate) : Long =
        date.atTime(0,0,0 ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    // Convert from date Long value read from SQLite DB to a Date value
    // that can be assigned to an entity property
    @TypeConverter
    fun toDate(dateLong: Long) : LocalDate =
        Instant.fromEpochMilliseconds(dateLong).toLocalDateTime(TimeZone.currentSystemDefault()).date


    @TypeConverter
    fun toString(word: Word): String {
        return json.encodeToString(word)
    }
    @TypeConverter
    fun toWord(jsonString: String): Word {
        return json.decodeFromString(jsonString)
    }

    //Sentence
    @TypeConverter
    fun fromSentence(sentence: Sentence): String {
        return json.encodeToString(sentence)
    }
    @TypeConverter
    fun toSentence(jsonString: String): Sentence {
        return json.decodeFromString(jsonString)
    }
    //Definition
    @TypeConverter
    fun fromDefinition(definition: Definition): String {
        return json.encodeToString(definition)
    }
    @TypeConverter
    fun toDefinition(jsonString: String): Definition {
        return json.decodeFromString(jsonString)
    }
    //Resource
    @TypeConverter
    fun fromResource(resource: Resource): String {
        return json.encodeToString(resource)
    }
    @TypeConverter
    fun toResource(jsonString: String): Resource {
        return json.decodeFromString(jsonString)
    }
    //============================Converting Lists=================================

    //Words
    @TypeConverter
    fun toWordList(value: String): MutableList<Word> {
        //return json.decodeFromString(value)
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return json.decodeFromString<MutableList<Word>>(value)
    }
    @TypeConverter
    fun fromWordList(list: MutableList<Word>): String {
        //return json.encodeToString(list)
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true        }
        return json.encodeToString(list)
    }


    //Sentences
    @TypeConverter
    fun toSentenceList(value: String): MutableList<Sentence> {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return json.decodeFromString<MutableList<Sentence>>(value)
    }

    @TypeConverter
    fun fromSentenceList(list: MutableList<Sentence> ): String {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true        }
        return json.encodeToString(list)
    }

    //Definitions
    @TypeConverter
    fun toDefinitionList(value: String): MutableList<Definition> {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return json.decodeFromString<MutableList<Definition>>(value)
    }

    @TypeConverter
    fun fromDefinitionList(list: MutableList<Definition>): String {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true        }
        return json.encodeToString(list)
    }

    //Resources
    @TypeConverter
    fun toResources(value: String): MutableList<Resource> {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return json.decodeFromString<MutableList<Resource>>(value)
    }

    @TypeConverter
    fun fromResources(list: MutableList<Resource>): String {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true        }
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true        }
        return json.encodeToString(uri)
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return uriString?.let { json.decodeFromString<Uri>(it) }
    }

}
