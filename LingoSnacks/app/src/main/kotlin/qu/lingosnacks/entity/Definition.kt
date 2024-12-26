package qu.lingosnacks.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Definition(
    var text: String,
    var source: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) {
    constructor(): this("", "")
}