package com.example.smartticketing.views.nav

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentReportBinding
import com.example.smartticketing.model.ViolationItem
import com.example.smartticketing.utilities.Violations

class ReportFragment : Fragment() {
    private lateinit var binding : FragmentReportBinding
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
        val violationNames = Violations.violationsList.map { it.name }.toTypedArray()

        val adapters = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, violationNames
        )
        binding.tvViolation.setAdapter(adapters)
        binding.tvViolation.setOnItemClickListener { parent, _, position, _ ->
            val selectedViolation = Violations.violationsList[position]
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Apprehension Type")
                .setMessage("Is this a license apprehension or no license apprehension?")
                .setPositiveButton("With License") { dialog, which ->
                    findNavController().navigate(R.id.withLicensedFragment)
                }
                .setNegativeButton("No License") { dialog, which ->
                    findNavController().navigate(R.id.noLicensedFragment)
                }
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
            binding.tvViolation.clearFocus()
        }
    }
}