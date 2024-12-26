package qu.lingosnacks.datasource

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import qu.lingosnacks.entity.Definition
import qu.lingosnacks.entity.LearningPackage
import qu.lingosnacks.entity.Resource
import qu.lingosnacks.entity.Sentence
import qu.lingosnacks.entity.User
import qu.lingosnacks.entity.Word
import qu.lingosnacks.repository.PackageRepository

@Database(entities = [LearningPackage::class, Word::class, Definition::class, Sentence::class,
                     Resource::class, User::class],
        version = 8, exportSchema = false)
@TypeConverters(Converters::class)
    abstract class LingoSnacksDB : RoomDatabase() {
        abstract fun learningPackageDao(): LearningPackageDao
        companion object{
            private var db: LingoSnacksDB? = null
            fun getDatabase(context: Context): LingoSnacksDB {
                Log.d("Room", "Room DB init")
                if(db == null){
                    db = Room.databaseBuilder(context.applicationContext, LingoSnacksDB::class.java, "LingoSnacksDB")
                        .fallbackToDestructiveMigration().addTypeConverter(Converters())
                        .build()

                    CoroutineScope(Dispatchers.IO).launch {
//                        PackageRepository.loadPackagesToDB(context)
                        PackageRepository.initDB(db, context)
                    }
                }
                return db as LingoSnacksDB
            }
        }
}