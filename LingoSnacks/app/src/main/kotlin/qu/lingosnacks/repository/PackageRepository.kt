package qu.lingosnacks.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import qu.lingosnacks.datasource.LearningPackageDao
import qu.lingosnacks.datasource.LingoSnacksDB
import qu.lingosnacks.entity.LearningPackage
import qu.lingosnacks.entity.Review
import qu.lingosnacks.entity.Score
import qu.lingosnacks.entity.User

class PackageRepository(val context: Context) {

    //Data base related
    private val fireBaseDB: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val localDB = LingoSnacksDB.getDatabase(context)


    private val lPackageDao: LearningPackageDao by lazy {
        localDB.learningPackageDao()
    }

    //Collection references
    private val lpackageRef by lazy {
        fireBaseDB.collection("packages")
    }

    private val userCollectionRef by lazy {
        fireBaseDB.collection("users")
    }

    private val authRepo by lazy { AuthRepository(context) }


    // ToDo: Implement all PackageRepository methods to read/write from the online/local database
    private fun getPackages(): List<LearningPackage> {
        val data = context.assets.open("packages.json")
            .bufferedReader().use { it.readText() }
        var packages: List<LearningPackage> = listOf()
        try {
            packages =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<LearningPackage>>(data)
        } catch (e: Exception) {
            println(e.message)
        }
        return packages
    }

    fun getPackagesFromFB(searchText: String = ""): Flow<List<LearningPackage>> = callbackFlow {
        val snapshotListener = lpackageRef
            .addSnapshotListener { values, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val packages = mutableListOf<LearningPackage>()
                for (document in values!!.documents) {
                    val learningPackage = document.toObject(LearningPackage::class.java)
                    if (learningPackage != null &&
                        (learningPackage.description.contains(
                            searchText,
                            ignoreCase = true
                        ) || learningPackage.words.any { word ->
                            word.text.contains(
                                searchText,
                                ignoreCase = true
                            )
                        })
                    ) {
                        packages.add(learningPackage)
                    }
                }

                trySend(packages)
            }

        awaitClose { snapshotListener.remove() }

//        val snapshotListener = lpackageRef.addSnapshotListener { values, err ->
//            if (err != null)
//                return@addSnapshotListener
//
//            val packages = values!!.toObjects(LearningPackage::class.java)
//            trySend(packages)
//        }
//        awaitClose { snapshotListener.remove() }
}

    fun getPackages(searchText: String) : List<LearningPackage> {
        var packages = getPackages()

        if (searchText.isEmpty())
            return packages

        packages = packages.filter {
            it.words.any { w -> w.text.contains(searchText, ignoreCase = true) } ||
            it.description.contains(searchText, true)
        }
        return packages
    }

    suspend fun getPackagesFB(searchText: String): List<LearningPackage> {
        val querySnapshot = lpackageRef
            .whereArrayContains("description", searchText)
            .whereArrayContains("words", searchText)
            .get()
            .await()

        val packages = mutableListOf<LearningPackage>()

        for (document in querySnapshot.documents) {
            val learningPackage = document.toObject(LearningPackage::class.java)
            learningPackage?.let {
                packages.add(it)
            }
        }

        return packages
    }


    suspend fun getPackage(packageId: String) = lpackageRef
        .document(packageId)
        .get()
        .await()
        .toObject(LearningPackage::class.java)

    // ToDo: Add/update the learning package to Firestore & upload the associated media files to Firebase Cloud Storage
    suspend fun addPackage(learningPackage: LearningPackage)  {
        Log.d("FB", "ADD PACKAGE CALLED")
        this.lpackageRef.document(learningPackage.id).set(learningPackage).await()
    }

    fun updatePackage(learningPackage: LearningPackage) = this.lpackageRef
        .document(learningPackage.packageId)
        .set(learningPackage)

    // ToDo: Delete package from Firestore and its associated resources from Cloud Storage
    suspend fun deleteOnlinePackage(packageId: String) {
        Log.d("DELETE", "Delete called")
        lpackageRef.document(packageId)
            .delete().await()
    }

    /* ToDo: Download package from Firestore to a local SqlLite database
       and download its associated resources from Cloud Storage
       so that the package can be used offline */
    suspend fun downloadPackage(learningPackage: LearningPackage) {
        try {
            withContext(Dispatchers.IO) {
                val learningPackage = lpackageRef.document(learningPackage.id)
                    .get()
                    .await()
                    .toObject(LearningPackage::class.java)

                learningPackage?.let {
                    it.isDownloaded = true
                    lPackageDao.insertPackage(it)
                }
            }
        } catch (e: Exception) {
            Log.e("DownloadPackage", "Error downloading package: $e")
        }
    }

    // ToDo: Delete package from the local database and its associated resource files
    suspend fun deleteLocalPackage(packageId: String) {
        withContext(Dispatchers.IO) {
            lPackageDao.deletePackage(packageId)
        }
    }

    fun getReviews(packageId: String) : List<Review> {
        val data = context.assets.open("reviews.json")
            .bufferedReader().use { it.readText() }
        val reviews = Json{ ignoreUnknownKeys = true }.decodeFromString<List<Review>>(data)
        return reviews.filter { it.packageId == packageId }
    }

    fun getReview(packageId: String, doneBy: String) : Review? {
        val reviews = getReviews(packageId)
        return reviews.firstOrNull { it.doneBy == doneBy }
    }

    fun getPackageAvgRating(packageId: String) : Double {
        val reviews = getReviews(packageId)
        return if (reviews.isNotEmpty()) {
            val avgRating = reviews.map { it.rating }.average()
            val reviewAvgRounded = String.format("%.1f", avgRating).toDouble()
            reviewAvgRounded
        } else {
            0.0
        }
    }

    // ToDo: Update review if exists otherwise add it
    suspend fun upsertReview(review: Review) {
        lpackageRef.document(review.packageId).collection("reviews").add(review).await()
        //scoresCollectionRef.add(score)
        println(">> Debug: PackageRepository.upsertReview: $review")
    }


    fun getScores(uid: String): List<Score> {
        val data = context.assets.open("scores.json")
            .bufferedReader().use { it.readText() }
        val reviews = Json{ ignoreUnknownKeys = true }.decodeFromString<List<Score>>(data)
        //ToDo: remove the toString
        return reviews.filter { it.uid == uid }
    }

    // ToDo: replace this example data with database query to get scores summary by uid
    fun getScoresSummary(uid: String): Map<String, Int> {
        val scores = getScores(uid)
        val scoresSummary = scores.groupBy { it.gameName }.map {
            Pair(it.key, it.value.map { s-> s.score }.average().toInt())
        }.toMap()
        return scoresSummary
    }

    suspend fun addScore(score: Score) {
       lpackageRef.document(score.packageId).collection("scores").add(score).await()
        //scoresCollectionRef.add(score)
        println(">> Debug: PackageRepository.addScore: $score")
    }

    private suspend fun isTherePackageCollection(): Boolean {
        val queryResult = this.lpackageRef.limit(1).get().await()
        return queryResult.isEmpty
    }

    // ToDo initialize Firestore db with data from packages.json and users.json
    suspend fun initFirestoreDB(): String {

        if (!isTherePackageCollection()) {
            Log.d("FB", "Already initialized")
            return "Firebase database is already initialized"
        }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
//         Read from json file and write to db
//         1. Insert users
        var data = context.assets.open("users.json")
            .bufferedReader().use { it.readText() }

        val users = json.decodeFromString<List<User>>(data)
        users.forEach {
            userCollectionRef.add(it)
//                authRepo.signUp(it)
        }

        // 2. Insert packages
        data = context.assets.open("packages.json")
            .bufferedReader().use { it.readText() }

        var packages = json.decodeFromString<List<LearningPackage>>(data)
        println(">> Debug: initDB packages $packages")
        Log.d("FB", "Packages read from JSON")

        // 3. Insert Reviews
        data = context.assets.open("reviews.json")
            .bufferedReader().use { it.readText() }
        var reviews = json.decodeFromString<List<Review>>(data)
        Log.d("FB", "reviews read from JSON")

        //4. Insert Scores
        data = context.assets.open("scores.json")
            .bufferedReader().use { it.readText() }
        var scores = json.decodeFromString<List<Score>>(data)
        Log.d("FB", "scores read from JSON")


        packages.forEach {lp ->
            val packageId = lp.packageId
            addPackage(lp)

            val packageReviews = reviews.filter { it.packageId == packageId }
            val packageScores = scores.filter { it.packageId == packageId }

            packageReviews.forEach {
                upsertReview(it)
            }
            packageScores.forEach {
                addScore(it)
            }
            println(">> Debug: initDB packages ${lp.packageId}")
        }

        return "Firebase database initialized with users & ${packages.size} packages."
    }

    companion object {
        suspend fun initDB(lingoSnacksDB: LingoSnacksDB?, context: Context) {
            Log.d("Room", "Init DB called")
            if (lingoSnacksDB == null) {
                Log.d("Room", "DB IS NULL")
                return
            }
            val packageDao = lingoSnacksDB.learningPackageDao()
            packageDao.insertPackage(LearningPackage())
        }
    }
}