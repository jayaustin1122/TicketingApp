package com.example.smartticketing.views

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartticketing.databinding.FragmentDashBoardBinding
import com.example.smartticketing.viewmodels.DashBoardViewModel

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.smartticketing.R
import com.example.smartticketing.utilities.ProgressDialogUtils

class DashBoardFragment : Fragment() {

    private lateinit var binding: FragmentDashBoardBinding
    private val viewModel: DashBoardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.driver.observe(viewLifecycleOwner, Observer { count ->
            binding.tvDriverCount.text = count.toString()
        })
        viewModel.apprehendCount.observe(viewLifecycleOwner, Observer { count ->
            binding.tvCountApprehend.text = count.toString()
        })
        binding.btnNoLicensed.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(),"Loading...")

            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.noLicensedFragment)
                ProgressDialogUtils.dismissProgressDialog()
            },1000)
        }

        binding.btnWithLicensed.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(),"Loading...")

            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.withLicensedFragment)
                ProgressDialogUtils.dismissProgressDialog()
            },1000)
        }

        viewModel.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
            userInfo?.let {
                binding.apply {
                    // Set the user's profile image
                    Glide.with(requireContext())
                        .load(userInfo.profileUrl)
                        .error(R.drawable.baseline_person_24)
                        .into(imgUser)

                    textView.text = userInfo.firstName
                    textView.setTextColor(Color.WHITE)
                }
            }
        })
        binding.imgUser.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(),"Loading...")

            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.profileAccountFragment)
                ProgressDialogUtils.dismissProgressDialog()
            },1000)

        }

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.loadUserInfo()
            viewModel.loadDriverCount()
            viewModel.loadApprehends()
        }
    }
}
