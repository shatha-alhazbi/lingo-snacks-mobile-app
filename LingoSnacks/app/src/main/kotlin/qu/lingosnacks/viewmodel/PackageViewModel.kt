package qu.lingosnacks.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import qu.lingosnacks.entity.LearningPackage
import qu.lingosnacks.entity.ResourceTypeEnum
import qu.lingosnacks.entity.Review
import qu.lingosnacks.entity.Score
import qu.lingosnacks.entity.Word
import qu.lingosnacks.repository.AuthRepository
import qu.lingosnacks.repository.PackageRepository
import qu.lingosnacks.utils.getRandomId

class PackageViewModel(application: Application) : AndroidViewModel(application) {

    private val packageRepository = PackageRepository(application)
    lateinit var selectedPackage: LearningPackage //selected package's ID

    var packages = packageRepository.getPackagesFromFB().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("FB", "INIT CALLED")
            packageRepository.initFirestoreDB()
        }
    }

    fun getPackages(searchText: String): StateFlow<List<LearningPackage>> {
        viewModelScope.launch(Dispatchers.IO) {
         packages = packageRepository.getPackagesFromFB(searchText).stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        }
        return packages
    }

    //fun getPackage(packageId: String) = packageRepository.getPackage(packageId)
    fun getPackage(packageId: String) : LearningPackage {
        //packageRepository.getPackage(packageId)
        return packages.value.find { it.packageId == packageId } ?: LearningPackage()
    }

    fun deleteOnlinePackage(packageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            packageRepository.deleteOnlinePackage(packageId)
        }
        //packages.removeIf { it.packageId == packageId }
    }

    fun deleteLocalPackage(packageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            packageRepository.deleteLocalPackage(packageId)
            //packages.removeIf { it.packageId == packageId }
        }
    }

    // Update package if exists otherwise add it
    fun upsertPackage(learningPackage: LearningPackage) {
        if (learningPackage.packageId != "0") {
            packageRepository.updatePackage(learningPackage)

            // A trick to trigger UI recomposition after update
            //Todo: FIX THIS LATER TO WORK WITH FLOW
//            packages = packages.map { lp ->
//                if (lp.packageId == learningPackage.packageId) learningPackage
//                else lp
//            }.toMutableStateList()

            // This does NOT trigger UI recomposition
        } else {
            learningPackage.packageId = getRandomId()
            viewModelScope.launch(Dispatchers.IO) {
            packageRepository.addPackage(learningPackage)
            //packages.add(learningPackage)
            //println(">> Debug: upsertPackage: ${packages.size}")
                }
        }
    }

    fun downloadPackage(learningPackage: LearningPackage) {
        viewModelScope.launch { packageRepository.downloadPackage(learningPackage) }

    }

    fun getReviews(packageId: String) = packageRepository.getReviews(packageId)
    fun getReview(packageId: String, doneBy: String) = packageRepository.getReview(packageId, doneBy)
    fun getPackageAvgRating(packageId: String) = packageRepository.getPackageAvgRating(packageId)
    // Update review if exists otherwise add it
    fun upsertReview(review: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            packageRepository.upsertReview(review)
        }
    }

    fun getScores(uid: String) = packageRepository.getScores(uid)
    fun getScoresSummary(uid: String) = packageRepository.getScoresSummary(uid)
    fun addScore(score: Score) {
        viewModelScope.launch(Dispatchers.IO) {
            packageRepository.addScore(score)
        }
    }
    //fun getLeaderBoard() = packageRepository.getLeaderBoard()

    // Hardcoded for simplicity. In real app data should come from a DB
    fun getLevels() = listOf("Beginner", "Intermediate", "Advanced")
    fun getLanguages() = listOf("English", "Arabic", "French", "Spanish", "Italian")
    fun getCategories() = listOf("Humanities", "Science and Technology", "Health and Fitness", "Business", "Politics", "Travel")
    fun getResourceTypes() = listOf(ResourceTypeEnum.Photo.toString(),
                                    ResourceTypeEnum.Video.toString(),
                                    ResourceTypeEnum.Website.toString())

}