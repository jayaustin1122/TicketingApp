package com.example.smartticketing.viewmodels

import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.smartticketing.model.UserInfo
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.bouncycastle.asn1.isismtt.x509.DeclarationOfMajority.dateOfBirth


class DashBoardViewModel : ViewModel() {

    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?> get() = _userInfo

    private val _driverCount = MutableLiveData<Int?>()
    val driver: LiveData<Int?> get() = _driverCount

    private val _apprehendCount = MutableLiveData<Int?>()
    val apprehendCount: LiveData<Int?> get() = _apprehendCount

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadUserInfo() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("uid", currentUser.uid)
                        .get().await()

                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents.firstOrNull()

                        document?.let {
                            val firstName = it.getString("firstName")
                            val lastName = it.getString("lastName")
                            val address = it.getString("address")
                            val profile = it.getString("profilePicture")
                            val position = it.getString("position")
                            val email = it.getString("email")
                            val phone = it.getString("phone")
                            val birthdate = it.getString("birthdate")


                            val userInfo = UserInfo(
                                firstName = firstName,
                                lastName = lastName,
                                address = address,
                                profileUrl = profile,
                                position = position,
                                email = email,
                                phone = phone,
                                birthdate = birthdate
                            )

                            withContext(Dispatchers.Main) {
                                _userInfo.value = userInfo
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _errorMessage.value = "No user data found"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = e.message
                    }
                }
            }
        } else {
            _errorMessage.value = "User not authenticated"
        }
    }

    fun updateInFirestore(
        image: Uri,
        firstName: String,
        lastName: String,
        birthdate: String,
        address: String
    ) {
        val storage = FirebaseStorage.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val updatedData = hashMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "birthdate" to birthdate,
                "address" to address
            )

            if (image != Uri.EMPTY) {
                val reference = storage.reference.child("profile/${user.uid}.jpg")
                reference.putFile(image).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            updatedData["profilePicture"] = uri.toString()  // Add profile picture URL to the data
                            updateUserDocument(user.uid, updatedData)
                        }.addOnFailureListener { exception ->
                            _errorMessage.value = "Image upload failed: ${exception.message}"
                        }
                    } else {
                        _errorMessage.value = "Image upload failed: ${task.exception?.message}"
                    }
                }
            } else {
                // If no image, just update other user info without profile picture
                updateUserDocument(user.uid, updatedData)
            }
        } ?: run {
            _errorMessage.value = "User not authenticated"
        }
    }

    private fun updateUserDocument(uid: String, updatedData: Map<String, Any>) {
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .update(updatedData)
            .addOnSuccessListener {
                // Handle success (e.g., show a success message)
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = "Update failed: ${exception.message}"
            }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _userInfo.value = null
    }
    fun updatePassword(
        oldUserPassword: String?,
        newPass: String?
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        val user = auth.currentUser

        userId?.let { uid ->
            firestore.collection("users").document(uid).get().addOnSuccessListener { document ->
                val email = document.getString("email")

                if (email != null) {
                    val credential: AuthCredential = EmailAuthProvider.getCredential(email, oldUserPassword!!)

                    user?.reauthenticate(credential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (newPass != null) {
                                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {

                                    } else {
                                        // Handle password update failure
                                        _errorMessage.value = "Password update failed: ${updateTask.exception?.message}"
                                    }
                                }
                            }
                        } else {

                            _errorMessage.value = "Reauthentication failed: ${task.exception?.message}"
                        }
                    }
                } else {

                    _errorMessage.value = "Email not found for user."
                }
            }.addOnFailureListener { exception ->

                _errorMessage.value = "Failed to retrieve email: ${exception.message}"
            }
        } ?: run {
            _errorMessage.value = "User not authenticated"
        }
    }
    fun loadDriverCount() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val querySnapshot = db.collection("registered_drivers")
                        .get().await()

                    // Get the count of documents in the collection
                    val driverCount = querySnapshot.size()

                    withContext(Dispatchers.Main) {
                        // Bind the result to the fragment
                        _driverCount.value = driverCount
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = e.message
                    }
                }
            }
        } else {
            _errorMessage.value = "User not authenticated"
        }
    }

    fun loadApprehends() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val querySnapshot = db.collection("apprehensions")
                        .get().await()

                    // Get the count of documents in the collection
                    val appCount = querySnapshot.size()

                    withContext(Dispatchers.Main) {
                        // Bind the result to the fragment
                        _apprehendCount.value = appCount
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = e.message
                    }
                }
            }
        } else {
            _errorMessage.value = "User not authenticated"
        }
    }
}
