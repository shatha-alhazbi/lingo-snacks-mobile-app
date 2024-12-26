//package qu.lingosnacks.entity.helperClasses
//
//import androidx.room.Embedded
//import androidx.room.Relation
//import qu.lingosnacks.entity.Definition
//import qu.lingosnacks.entity.Sentence
//import qu.lingosnacks.entity.Word
//
//data class SentenceWithResources(
//    @Embedded
//    val sentence: Sentence,
//
//    @Relation(parentColumn = "id", entityColumn = "wordId")
//    val sentences: List<Definition>) {
//
//}