package qu.lingosnacks.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import qu.lingosnacks.datasource.Converters
import java.time.format.DateTimeFormatter

@Serializable
@Entity
data class LearningPackage(
    @PrimaryKey
    var packageId: String,
    val author: String,
    var category: String,
    var description: String,
    var iconUrl: String,
    var language: String,
    var _lastUpdatedDate: String = LocalDate.toString(),
    var level: String,
    var title: String,
    var version: Int = 1,
    // ToDo: Do NOT store this property in the online DB
    // ToDo: When you get packages, you should set it to true for packages that were downloaded to the local DB
    @get:Exclude
    var isDownloaded : Boolean = false,
//    var avgRating: Double = 0.0,
//    internal var numRatings: Int = 0,
    @TypeConverters(Converters::class)
    var words: MutableList<Word> = mutableListOf(),
    @DocumentId
    var id: String = ""
) {
    constructor() : this(packageId = "0",
        title="", description = "",
        category = "", level = "",
        language = "", iconUrl = "",
        author = "",
    )

    // ToDo: not clear why this is needed
    fun getWordTotals(word: String): Map<String, Int> {
        val wordObj = words.find { it.text == word }
        return wordObj?.totals ?: mapOf(
            "definitions" to 0, "sentences" to 0,
            "def" to 0, "sent" to 0
        )
    }

//    val lastUpdatedDate: LocalDate
//        get() = LocalDate.parse(_lastUpdatedDate)
//
//    // Setter for lastUpdatedDate that converts LocalDate to String
//    fun setLastUpdatedDate(date: LocalDate) {
//        this._lastUpdatedDate = date.toString()
//    }
}

fun LearningPackage.isAuthor(user: User?) = user?.email.equals(this.author, true)