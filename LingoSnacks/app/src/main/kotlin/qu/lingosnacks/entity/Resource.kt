package qu.lingosnacks.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Resource(
    var title: String,
    var url: String,
    var type: String,
    @Contextual var imageUri: Uri? = null,
    @Contextual var videoUri: Uri? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    constructor() : this("", "", "")
    val extension: String
        get() {
            val temp = url.substringAfterLast(".")
            return if (temp == url) ".url" else ".$temp"
        }
}


//@Serializable
//data class Resource(
//    var title: String,
//    var url: String,
//    var type: String
//) {
//    constructor(): this("", "", "")
//    val extension: String
//        get() {
//            val temp = url.substringAfterLast(".")
//            return if (temp == url) ".url" else ".$temp"
//        }
//}

