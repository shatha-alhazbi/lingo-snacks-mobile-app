package qu.lingosnacks.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import qu.lingosnacks.datasource.Converters

@Entity(
//    foreignKeys = [
//        ForeignKey(
//            entity = LearningPackage::class,
//            parentColumns = ["packageId"],
//            childColumns = ["packageId"],
//            onDelete = ForeignKey.CASCADE,
//            onUpdate = ForeignKey.CASCADE
//        )
//    ]
)
@Serializable
data class Word(
    var text: String,
    @TypeConverters(Converters::class)
    var definitions: MutableList<Definition> = mutableListOf(),
    @TypeConverters(Converters::class)
    var sentences: MutableList<Sentence> = mutableListOf(),
   // val packageId: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    //An empty constructor:
    constructor(): this(text= "")
    private val definitionsCount: Int
        get() {
            return definitions.size
        }

    private val sentencesCount: Int
        get() {
            return sentences.size
        }

    //ToDo: not sure why this is needed
    val totals: Map<String, Int>
        get() {
            return mapOf(
                "definitions" to definitionsCount, "sentences" to sentencesCount,
                "def" to definitionsCount, "sent" to sentencesCount
            )
        }

    fun addResource(sentence: Sentence, resource: Resource): Boolean {
        val sentIndex = sentences.indexOf(sentence)
        if (sentIndex == -1) return false
        return sentences[sentIndex].addResource(resource)
    }

    fun addResource(sentenceText: String, resource: Resource): Boolean {
        val sentIndex = sentences.indexOfFirst { it.text == sentenceText }
        if (sentIndex == -1) return false
        return sentences[sentIndex].addResource(resource)
    }
}
