package qu.lingosnacks.entity.helperClasses

import androidx.room.Embedded
import androidx.room.Relation
import qu.lingosnacks.entity.LearningPackage
import qu.lingosnacks.entity.Word

data class LearningPackageWithWords(

    @Embedded
    val learningPackage: LearningPackage,

    @Relation(parentColumn = "packageId", entityColumn = "packageId")
    val words: List<Word>

)
