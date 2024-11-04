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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth: FirebaseAuth
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
                    findNavController().navigate(R.id.dashBoardFragment)
                }
            } else {
                // showNoInternetDialog()
                Toast.makeText(requireContext(),"No InterNet Detected!",Toast.LENGTH_SHORT).show()
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