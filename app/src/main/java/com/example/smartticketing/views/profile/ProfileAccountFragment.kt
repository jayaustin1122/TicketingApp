package com.example.smartticketing.views.profile

import android.os.Build
import android.os.Bundle
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
import com.example.smartticketing.databinding.FragmentProfileAccountBinding
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.viewmodels.DashBoardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ProfileAccountFragment : Fragment() {
    private lateinit var binding: FragmentProfileAccountBinding
    private val viewModel: DashBoardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileAccountBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
            userInfo?.let {
                binding.apply {

                    Glide.with(requireContext())
                        .load(userInfo.profileUrl)
                        .error(R.drawable.baseline_person_24)
                        .into(avatar)

                    name.text = userInfo.firstName
                    email.text = userInfo.email
                    phone.text = userInfo.phone
                    address.text = userInfo.address
                    role.text = userInfo.position


                }
            }
        })
        binding.btnLogout.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(), "Logging out...")
            viewModel.signOut()
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                ProgressDialogUtils.dismissProgressDialog()
                findNavController().navigate(R.id.signInFragment)
            }
        }
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }


        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.loadUserInfo()
        }
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.updateInfoFragment)
        }
        binding.btnUpdatePass.setOnClickListener {
            findNavController().navigate(R.id.updatePassFragment)
        }
    }
}