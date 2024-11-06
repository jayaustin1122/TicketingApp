package com.example.smartticketing.views

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth: FirebaseAuth
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        Handler().postDelayed({
            checkUser()
        }, 5000)
    }

    private fun checkUser() {
        GlobalScope.launch(Dispatchers.Main) {
            if (isNetworkAvailable()) {
                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    findNavController().navigate(R.id.signInFragment)
                } else {
                    checkUserType(firebaseUser.uid) // Check user type
                }
            } else {
                Toast.makeText(requireContext(), "No Internet Detected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun checkUserType(userId: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val userDocument = fireStore.collection("users").document(userId)
                val snapshot = userDocument.get().await()

                if (snapshot.exists()) {
                    val userType = snapshot.getString("userType")
                    when (userType) {

                        "member" -> findNavController().navigate(R.id.memberFragment)
                        else -> findNavController().navigate(R.id.holderFragment)
                    }
                } else {
                    Toast.makeText(requireContext(), "User data not found.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.signInFragment)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to retrieve user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            this@SplashFragment.requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
