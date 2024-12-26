package qu.lingosnacks.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import qu.lingosnacks.entity.User
import qu.lingosnacks.repository.AuthRepository

class AuthViewModel(appContext: Application) : AndroidViewModel(appContext) {
    private val authRepository = AuthRepository(appContext)

    // ToDo: Initialize _currentUser by calling userRepository.getCurrentUser to get cached authenticated user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser as StateFlow<User?>

    fun signIn(email: String, password: String){
        viewModelScope.launch(Dispatchers.IO) {
            val user = authRepository.signIn(email, password)
            _currentUser.value = user
        }
//        return currentUser.value
    }


    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun signUp(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signUp(user)
            _currentUser.value = user
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
    }

    // Hardcoded for simplicity. In real app data should come from a DB
    fun getRoles() = listOf("Member", "Author")
}