package com.example.smartticketing.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
class SignInViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _signInStatus = MutableLiveData<Boolean>()
    val signInStatus: LiveData<Boolean> get() = _signInStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _userType = MutableLiveData<String?>()
    val userType: LiveData<String?> get() = _userType

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Email and Password cannot be empty"
            return
        }

        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Sign in the user
                    auth.signInWithEmailAndPassword(email, password).await()

                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {

                        val userDocument = fireStore.collection("users").document(firebaseUser.uid)
                        val snapshot = userDocument.get().await()

                        withContext(Dispatchers.Main) {
                            if (snapshot.exists()) {
                                val userType = snapshot.getString("userType")
                                _userType.value = userType

                                _signInStatus.value = true
                            } else {
                                _errorMessage.value = "User data not found."
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = e.message
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
