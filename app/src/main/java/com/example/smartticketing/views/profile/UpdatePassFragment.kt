package com.example.smartticketing.views.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentUpdateInfoBinding
import com.example.smartticketing.databinding.FragmentUpdatePassBinding
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.viewmodels.DashBoardViewModel


class UpdatePassFragment : Fragment() {
    private lateinit var binding : FragmentUpdatePassBinding
    private val viewModelUserInfo: DashBoardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdatePassBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

          binding.btnSave.setOnClickListener {
              ProgressDialogUtils.showProgressDialog(requireContext(),"Loading...")
              Handler(Looper.getMainLooper()).postDelayed({
                  validateData()
                  ProgressDialogUtils.dismissProgressDialog()
              },1000)
          }
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    private fun validateData() {
        val oldPass = binding.etOldPassword.text.toString()
        val confirmPass = binding.etConfirmPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()


        if (oldPass.isEmpty()) {
            Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        else if (confirmPass.isEmpty()) {
            Toast.makeText(requireContext(), "Please confirm your password", Toast.LENGTH_SHORT).show()
            return
        }
        else if (newPass != confirmPass) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        else {
            viewModelUserInfo.updatePassword(oldPass,newPass)
        }

    }
}