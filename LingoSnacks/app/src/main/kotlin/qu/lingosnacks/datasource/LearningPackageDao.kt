package qu.lingosnacks.datasource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import qu.lingosnacks.entity.LearningPackage
import qu.lingosnacks.entity.User
import qu.lingosnacks.entity.Word

@Dao
interface LearningPackageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(learningPackage: LearningPackage)

    @Update
    suspend fun updatePackage(learningPackage: LearningPackage)

    @Delete
    suspend fun deletePackage(learningPackage: LearningPackage)

    @Query("DELETE FROM learningPackage WHERE packageId = :packageId")
    suspend fun deletePackage(packageId: String)

    @Query("SELECT * FROM LearningPackage WHERE packageId = :packageId")
    suspend fun getPackage(packageId: String): LearningPackage?

    @Query("SELECT * FROM LearningPackage")
    fun getAllPackages(): Flow<List<LearningPackage>>

//    @Transaction
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPackageWithWords(learningPackage: LearningPackage, words: List<Word>)

//    @Query("SELECT * FROM LearningPackage WHERE packageId = :packageId")
//    suspend fun getPackageWithWords(packageId: String): LearningPackageWithWords?

    @Query("SELECT * FROM LearningPackage WHERE packageId = :packageId")
    suspend fun getPackageWithWords(packageId: String): LearningPackage?

    @Query("select count(*) from LearningPackage")
    suspend fun getPackagesCount() : Int



    @Query("SELECT * FROM User")
    fun getUser(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)


}
