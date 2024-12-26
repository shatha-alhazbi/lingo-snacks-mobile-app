package qu.lingosnacks.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import qu.lingosnacks.datasource.LearningPackageDao
import qu.lingosnacks.datasource.LingoSnacksDB
import qu.lingosnacks.entity.User
import java.lang.Exception


class AuthRepository(val context: Context) {


    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val lpDao by lazy {
        LingoSnacksDB.getDatabase(context).learningPackageDao()
    }

    private val userCollectionRef by lazy {
        Firebase.firestore.collection("users")
    }


    private fun getUsers() : List<User> {
        val data = context.assets.open("users.json")
            .bufferedReader().use { it.readText() }
        return Json.decodeFromString(data)
    }

    private suspend fun getUsersFb() : List<User> {
        val querySnapshot = userCollectionRef.get().await()

        // Mapping each document to a User object and filtering out null values
        return querySnapshot.documents.mapNotNull {
            it.toObject(User::class.java)
        }
    }


    fun signIn(email: String, password: String): User? {
        val users = getUsers()
        return users.find { it.email.lowercase() == email.lowercase() && it.password == password }
    }


//    suspend fun signIn(email: String, password: String): User? = withContext(Dispatchers.IO) {
//        try {
//            println("***inside SIGN IN ;)***")
//            val authResult = auth.signInWithEmailAndPassword(email, password).await()
//            println(">> Debug: signIn.authResult : ${authResult.user?.uid}")
//            println(" ${authResult?.user?.uid}")
//            authResult?.user?.let {
//                val deferredResult = async {
//                    getUser(it.uid)
//                }
//                val user = deferredResult.await()
//                println("USER $user")
//                user?.let {
//                    addCurrentUser(user)
//                }
//                user
//                getUser(it.uid)
//
//            } ?: throw Exception("Email and/or password invalid")
//        } catch (e: FirebaseAuthException) {
//            // Handle specific FirebaseAuthException, e.g., invalid email, user not found, etc.
//            println("Firebase Authentication Error: ${e.message}")
//            throw e
//        } catch (e: Exception) {
//            // Handle generic exceptions
//            println("Error during sign-in: ${e.message}")
//            throw e
//        }
//
//    }



    /* ToDo: Implement userRepository.signIn using FirebaseAuth (email and password authentication).
   ToDo: After successful sign in call userRepository.getUser to get the user details from Firstore.users collection
   ToDo: Then Call userRepository.addCurrentUser to add successfully authenticated user to lingosnacks local database
*/
//    suspend fun signIn(email: String, password: String): User? = withContext(Dispatchers.IO) {
//        try {
//            Log.d("SignIn", "***inside SIGN IN ;)***)")
//            val authResult = auth.signInWithEmailAndPassword(email, password).await()
//            Log.d("SignIn", ">> Debug: signIn.authResult : ${authResult.user?.uid}")
//
//            val user = authResult?.user?.let {
//                val deferredResult = async { getUser(it.uid) }
//                deferredResult.await()
//            }
//
//            Log.d("SignIn", "USER $user")
//
//            user?.let {
//                addCurrentUser(it)
//            }
//
//            return@withContext user
//                ?: throw Exception("Email and/or password invalid")
//        } catch (e: FirebaseAuthException) {
//            Log.e("SignIn", "Firebase Authentication Error Code: ${e.errorCode}")
//            Log.e("SignIn", "Firebase Authentication Error: ${e.message}")
//            throw e
//        } catch (e: Exception) {
//            Log.e("SignIn", "Error during sign-in: ${e.message}")
//            throw e
//        }
//    }




    // ToDo: UserRepository.addUser : Add the user to FirebaseAuth &
    //  Firestore users collection (password should NOT stored in users collection)
    suspend fun signUp(user: User) : User? = withContext(Dispatchers.IO) {
        val authResult =
            Firebase.auth.createUserWithEmailAndPassword(user.email, user.password).await()

        authResult?.user?.let {

            user.uid = it.uid
            println(">> Debug: signUp.user.uid : ${user.uid}")
            addUser(user)
//            addCurrentUser(user)
            user
        } ?: throw Exception("Signup failed")

    }

    // ToDo: Get the user details from Firstore.users collection

    private suspend fun getUser(uid: String): User? {
        val documentSnapshot = userCollectionRef.document(uid).get().await()

        // Check if the document exists before converting it to a User object
        return if (documentSnapshot.exists()) {
            documentSnapshot.toObject(User::class.java)
        } else {
            null
        }
    }


    private suspend fun addUser(user: User) {
        userCollectionRef.document(user.uid).set(user).await()
    }



//    private suspend fun getUser(email: String): User? {
//        try {
//            val userDocument = FirebaseFirestore.getInstance()
//                .collection("users")
//                .whereEqualTo("email", email)
//                .get()
//                .await()
//
//            if (!userDocument.isEmpty) {
//                // Assuming there's only one document matching the email
//                val userData = userDocument.documents[0].toObject(User::class.java)
//                if (userData != null) {
//                    Log.d(TAG, "User data retrieved successfully: $userData")
//                    return userData
//                } else {
//                    Log.e(TAG, "Failed to convert document to User object")
//                }
//            } else {
//                Log.d(TAG, "No user found with email: $email")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error retrieving user: ${e.message}", e)
//        }
//
//        return null
//    }



    // ToDo: Get the authenticated user from lingosnacks local database. Return null if not found (which mean the user did not login before).
    fun getCurrentUser(): String {
        return Firebase.auth.currentUser!!.uid
    }


        // ToDo: Add successfully authenticated user to lingosnacks local database
    private suspend fun addCurrentUser(user: User) {
            lpDao.addUser(user)

    }

    // ToDo: Delete authenticated user from lingosnacks local database
    private suspend fun deleteCurrentUser(user: User) {
        lpDao.deleteUser(user)

    }

    // ToDo: signout from FirebaseAuth and delete the user from lingosnacks local database
    fun signOut() {
        Firebase.auth.signOut()
    }


}