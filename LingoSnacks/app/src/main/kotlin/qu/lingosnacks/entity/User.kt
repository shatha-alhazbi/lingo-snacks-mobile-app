package qu.lingosnacks.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable
import qu.lingosnacks.utils.getRandomId

@Entity
@Serializable
data class User(
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    val role: String,
    var photoUrl: String = ""
) {


    @PrimaryKey()
    @DocumentId
    var uid: String = getRandomId()

    constructor(uid : String) : this(firstName = "",
        lastName = "", email = "",
        password = "", role = "") {
        this.uid = uid
    }


    val fullName: String
        get() = "$firstName $lastName"

    override fun toString()
            = "${firstName.trim()} ${lastName.trim()} - ${email.trim()}".trim()
}