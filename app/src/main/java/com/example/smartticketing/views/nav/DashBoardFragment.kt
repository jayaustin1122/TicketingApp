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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartticketing.databinding.FragmentDashBoardBinding
import com.example.smartticketing.viewmodels.DashBoardViewModel

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.smartticketing.R
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.utilities.Violations

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


        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.loadUserInfo()
            viewModel.loadDriverCount()
        }
        val violationNames = Violations.violationsList.map { it.name }.toTypedArray()

        val adapters = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, violationNames
        )
        binding.tvViolation.setAdapter(adapters)
        binding.tvViolation.setOnItemClickListener { parent, _, position, _ ->
            val selectedViolation = Violations.violationsList[position]
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Apprehension for ${selectedViolation.name}")
                .setMessage("Is this a license apprehension or no license apprehension?")
                .setPositiveButton("With License") { dialog, which ->
                    val bundle = Bundle().apply {
                        putString("violationType", selectedViolation.name)
                        putString("apprehensionType", "With License")
                    }
                    findNavController().navigate(R.id.withLicensedFragment, bundle)
                }
                .setNegativeButton("No License") { dialog, which ->
                    val bundle = Bundle().apply {
                        putString("violationType", selectedViolation.name)
                        putString("apprehensionType", "No License")
                    }
                    findNavController().navigate(R.id.noLicensedFragment, bundle)
                }
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
            binding.tvViolation.clearFocus()
        }

        binding.noHelmet.setOnClickListener {
            alertDialog("No Helmet")
        }

        binding.drunkAndDrive.setOnClickListener {
            alertDialog("Driving Under the Influence")
        }

        binding.speeding.setOnClickListener {
            alertDialog("Over Speeding")
        }

        binding.noInsurance.setOnClickListener {
            alertDialog("No Registration")
        }

        binding.trafficViolation.setOnClickListener {
            alertDialog("Traffic Violation")
        }

        binding.withoutLicence.setOnClickListener {
            alertDialog("Without License")
        }
        binding.reclessDriving.setOnClickListener {
            alertDialog("Reckless Driving")
        }
        binding.beatingTheRedLight.setOnClickListener {
            alertDialog("Beating The Red Light")
        }
        binding.noorcr.setOnClickListener {
            alertDialog("No OR-CR")
        }
        binding.illegalParking.setOnClickListener {
            alertDialog("Illegal Parking")
        }
        binding.illegalLights.setOnClickListener {
            alertDialog("Illegal Lights")
        }
        binding.overloading.setOnClickListener {
            alertDialog("Overloading")
        }
        binding.obstruction.setOnClickListener {
            alertDialog("Obstruction")
        }
        binding.unregisteredVehicle.setOnClickListener {
            alertDialog("Unregistered Vehicle")
        }
        binding.defectiveParts.setOnClickListener {
            alertDialog("Defective Parts")
        }
        binding.smokeBelching.setOnClickListener {
            alertDialog("Smoke Belching")
        }
        binding.codingViolation.setOnClickListener {
            alertDialog("Coding")
        }
        binding.others.setOnClickListener {
            alertDialog("")
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

    fun alertDialog(violationType: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Violation for $violationType")
            .setMessage("Is this a license apprehension or no license apprehension?")
            .setPositiveButton("With License") { dialog, which ->
                val bundle = Bundle().apply {
                    putString("violationType", violationType)
                    putString("apprehensionType", "With License")
                }
                findNavController().navigate(R.id.withLicensedFragment, bundle)
            }
            .setNegativeButton("No License") { dialog, which ->
                val bundle = Bundle().apply {
                    putString("violationType", violationType)
                    putString("apprehensionType", "No License")
                }
                findNavController().navigate(R.id.noLicensedFragment, bundle)
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }
}
