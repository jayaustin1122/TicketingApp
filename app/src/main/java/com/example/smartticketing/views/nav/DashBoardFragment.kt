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
    private var isDialogShowing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.reportCrime.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("selectedFragmentId", R.id.navigation_services)
            }
            findNavController().navigate(R.id.holderFragment, bundle)
        }


        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.loadUserInfo()
            viewModel.loadDriverCount()
        }


        binding.noHelmet.setOnClickListener {
            viewModel.loadViolationData("No Helmet") // Load data for no helmet violations
            observeAndShowViolationDialog("No Helmet")
        }

        binding.drunkAndDrive.setOnClickListener {
            viewModel.loadViolationData("Driving Under the Influence") // Load data for drunk driving violations
            observeAndShowViolationDialog("Driving Under the Influence")
        }

        binding.speeding.setOnClickListener {
            viewModel.loadViolationData("Over Speeding") // Load data for over speeding violations
            observeAndShowViolationDialog("Over Speeding")
        }

        binding.noInsurance.setOnClickListener {
            viewModel.loadViolationData("No Registration") // Load data for no insurance violations
            observeAndShowViolationDialog("No Registration")
        }

        binding.trafficViolation.setOnClickListener {
            viewModel.loadViolationData("Running a Red Light") // Load data for traffic violations
            observeAndShowViolationDialog("Running a Red Light")
        }

        binding.withoutLicence.setOnClickListener {
            viewModel.loadViolationData("No License") // Load data for no license violations
            observeAndShowViolationDialog("No License")
        }
    }
        private fun observeAndShowViolationDialog(violationType: String) {
            if (isDialogShowing) return // Prevent showing multiple dialogs at once

            // Observe the LiveData and show the dialog with the latest apprehension count
            viewModel.apprehendCount.observe(viewLifecycleOwner) { count ->
                if (!isDialogShowing) {
                    if (count != null) {
                        showViolationDialog(violationType, count)
                    }
                }
            }
        }
        private fun showViolationDialog(violationType: String, count: Int) {
            isDialogShowing = true // Mark that the dialog is being shown

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Apprehended for $violationType")
                .setMessage("There are $count apprehensions for $violationType")
                .setPositiveButton("OK") { _, _ ->
                    isDialogShowing = false // Reset flag when the dialog is dismissed
                }
                .setOnDismissListener {
                    isDialogShowing = false // Reset flag when dialog is dismissed in any way
                    viewModel.apprehendCount.removeObservers(viewLifecycleOwner) // Remove observer to avoid redundant dialogs
                }
                .create()
            dialog.show()
        }

}
