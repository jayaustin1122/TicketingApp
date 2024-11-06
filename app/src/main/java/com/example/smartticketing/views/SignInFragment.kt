package com.example.smartticketing.views

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentSignInBinding
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.viewmodels.SignInViewModel

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.buttonLoginLogin.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(),"Logging in...")
            validateData()
        }


        viewModel.signInStatus.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.holderFragment)
            }
        })


        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Email or Password is Incorrect", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateData() {
        val email = binding.etUsernameLogin.text.toString().trim()
        val pass = binding.etPass.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Email Invalid", Toast.LENGTH_SHORT).show()
        } else if (pass.isEmpty()) {
            Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.loginUser(email, pass)
            ProgressDialogUtils.dismissProgressDialog()
        }
    }
}
