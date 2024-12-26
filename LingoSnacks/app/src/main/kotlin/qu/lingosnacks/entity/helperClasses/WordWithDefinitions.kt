package qu.lingosnacks.entity.helperClasses

import androidx.room.Embedded
import androidx.room.Relation
import qu.lingosnacks.entity.Definition
import qu.lingosnacks.entity.Word

data class WordWithDefinitions(
    @Embedded
    val word: Word,

    @Relation(parentColumn = "id", entityColumn = "wordId")
    val sentences: List<Definition>
)
