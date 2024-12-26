package qu.lingosnacks.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import qu.lingosnacks.datasource.Converters

@Serializable
@Entity
data class Sentence(
    var text: String,
    @TypeConverters(Converters::class)
    var resources: MutableList<Resource> = mutableListOf(),
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) {
    constructor(): this("")
    fun addResource(resource: Resource): Boolean {
        return resources.add(resource)
    }

    fun removeResource(resource: Resource): Boolean {
        return resources.remove(resource)
    }

    fun removeResourceByTitle(title: String): Boolean {
        return resources.removeIf { it.title == title }
    }

    fun removeResourceByURL(url: String): Boolean {
        return resources.removeIf { it.url == url }
    }
}
