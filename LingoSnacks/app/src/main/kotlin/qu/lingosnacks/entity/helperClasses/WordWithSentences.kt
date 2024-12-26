package qu.lingosnacks.entity.helperClasses

import androidx.room.Embedded
import androidx.room.Relation
import qu.lingosnacks.entity.LearningPackage
import qu.lingosnacks.entity.Sentence
import qu.lingosnacks.entity.Word

data class WordWithSentences(
    @Embedded
    val word: Word,

    @Relation(parentColumn = "id", entityColumn = "wordId")
    val sentences: List<Sentence>
)
