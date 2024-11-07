package com.example.smartticketing.views.nav

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentReportBinding
import com.example.smartticketing.model.ViolationItem
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.utilities.Violations
import com.example.smartticketing.viewmodels.DashBoardViewModel

class ReportFragment : Fragment() {
    private lateinit var binding : FragmentReportBinding
    private val viewModel: DashBoardViewModel by viewModels()
    private var selectedFragmentId: Int? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            selectedFragmentId = it.getInt("selectedFragmentId", R.id.navigation_services)
        }
        viewModel.driver.observe(viewLifecycleOwner, Observer { count ->
            binding.tvDriverCount.text = count.toString()
        })
        viewModel.apprehendCount.observe(viewLifecycleOwner, Observer { count ->
            binding.tvCountApprehend.text = count.toString()
        })


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