package com.example.smartticketing.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentSignUpBinding
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class SignUpFragment : Fragment() {
    private lateinit var binding : FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding.btnCreate.setOnClickListener() {
            ProgressDialogUtils.showProgressDialog(requireContext(), "Creating Account")
            validateData()
        }
    }

    private fun validateData() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && address.isNotEmpty()) {
            if (isValidEmail(email)) {
                uploadInDb()
            } else {
                Toast.makeText(requireContext(), "Invalid Email Format!", Toast.LENGTH_SHORT).show()
                ProgressDialogUtils.dismissProgressDialog()
            }
        } else {
            Toast.makeText(requireContext(), "Empty Fields Are Not Allowed!", Toast.LENGTH_SHORT).show()
            ProgressDialogUtils.dismissProgressDialog()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun uploadInDb() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val timestamp = System.currentTimeMillis() / 1000

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val user: HashMap<String, Any?> = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "password" to password,
                        "fullName" to fullName,
                        "address" to address,
                        "timeStamp" to timestamp,
                        "userType" to "member",
                    )

                    // Now upload user data to Firestore
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users")
                        .document(uid!!)
                        .set(user)
                        .addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                findNavController().apply {
                                    popBackStack(R.id.signUpFragment, false)
                                    navigate(R.id.signInFragment)
                                }
                                Toast.makeText(requireContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                auth.signOut()
                                ProgressDialogUtils.dismissProgressDialog()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    firestoreTask.exception?.message ?: "Error creating account",
                                    Toast.LENGTH_SHORT
                                ).show()
                                ProgressDialogUtils.dismissProgressDialog()
                            }
                        }
                } else {
                    Toast.makeText(
                        requireContext(),
                        task.exception?.message ?: "Error creating user",
                        Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialogUtils.dismissProgressDialog()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                ProgressDialogUtils.dismissProgressDialog()
            }
    }

}