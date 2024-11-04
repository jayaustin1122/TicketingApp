package com.example.smartticketing.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartticketing.R
import com.example.smartticketing.adapters.ViolationAdapter
import com.example.smartticketing.databinding.FragmentNoLicensedBinding
import com.example.smartticketing.model.ViolationItem
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.viewmodels.DashBoardViewModel
import com.example.smartticketing.viewmodels.NoLicensedViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class NoLicensedFragment : Fragment() {
    private lateinit var binding: FragmentNoLicensedBinding
    private val viewModel: NoLicensedViewModel by viewModels()
    private lateinit var adapter: ViolationAdapter
    private val violationList = mutableListOf<ViolationItem>()
    private val selectedViolations = mutableListOf<ViolationItem>()
    private var totalAmount = 0
    private val viewModelUserInfo: DashBoardViewModel by viewModels()
    private var user: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoLicensedBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    val violationsList = listOf(
        ViolationItem(code = "1", name = "No Helmet", amount = "100"),
        ViolationItem(code = "2", name = "No Seatbelt", amount = "150"),
        ViolationItem(code = "3", name = "Overspeeding", amount = "500"),
        ViolationItem(code = "4", name = "Illegal Parking", amount = "300"),
        ViolationItem(code = "5", name = "Running a Red Light", amount = "1000"),
        ViolationItem(code = "6", name = "No License", amount = "200"),
        ViolationItem(code = "7", name = "No Registration", amount = "250"),
        ViolationItem(code = "8", name = "Driving Under the Influence", amount = "2000"),
        ViolationItem(code = "9", name = "Use of Mobile Phone While Driving", amount = "500"),
        ViolationItem(code = "10", name = "Failure to Signal", amount = "300")
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelUserInfo.loadUserInfo()

        val date = LocalDate.now()
        val currentTime = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        binding.tvDate.text = date.format(dateFormatter)
        binding.tvTime.text = currentTime.format(timeFormatter)
        adapter = ViolationAdapter(violationList) { removedAmount ->
            updateTotalAmount(-removedAmount)
        }

        binding.rvAddViolations.adapter = adapter
        binding.rvAddViolations.layoutManager = LinearLayoutManager(requireContext())

        binding.saveAndPrint.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Once Data saved will not be able to edit")
            builder.setTitle("Note!")
            builder.setCancelable(false)
            builder.setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    ProgressDialogUtils.showProgressDialog(requireContext(),"Loading...")

                    Handler(Looper.getMainLooper()).postDelayed({
                        validateData()
                        ProgressDialogUtils.dismissProgressDialog()
                    },1000)
                } as DialogInterface.OnClickListener)
            builder.setNegativeButton("No",
                DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->

                    dialog.cancel()
                } as DialogInterface.OnClickListener)

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()

        }
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.birthdate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.tvViolation.setOnItemClickListener { parent, _, position, _ ->
            val selectedViolation = violationsList[position]
            val newViolation = ViolationItem(
                name = selectedViolation.name,
                code = selectedViolation.code,
                amount = selectedViolation.amount
            )
            adapter.addViolation(newViolation)
            updateTotalAmount(newViolation.amount.toInt())
            selectedViolations.add(newViolation)
            binding.tvViolation.clearFocus()
        }
        val violationsArray = resources.getStringArray(R.array.vehicles)
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, violationsArray
        )
        binding.tvVehicle.setAdapter(adapter)

        binding.tvVehicle.setOnItemClickListener { parent, _, position, _ ->
            val selectedViolation = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(), "Selected: $selectedViolation", Toast.LENGTH_SHORT)
                .show()
        }
        val violationNames = violationsList.map { it.name }.toTypedArray()

        val adapters = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, violationNames
        )
        binding.tvViolation.setAdapter(adapters)

        viewModel.uploadStatus.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })


    }
    private fun showDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
            }
            val year = calendar.get(Calendar.YEAR).toString()
            val month = (calendar.get(Calendar.MONTH) + 1).toString()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString()

            binding.birthdate.setText("${month}/${day}/${year}")
        }
        datePicker.show(parentFragmentManager, "MaterialDatePicker")
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalAmount(amount: Int) {
        totalAmount += amount
        binding.tvTotalAmount.text = totalAmount.toString()
    }

    private fun getSelectedViolations(): List<ViolationItem> {
        return selectedViolations
    }

    private fun validateData() {
        val fullName = binding.fullName.text.toString().trim()
        val licenseNumber = binding.licenseNumber.text.toString().trim()
        val birthdate = binding.birthdate.text.toString().trim()
        val sex = binding.sex.text.toString().trim()
        val address = binding.address.text.toString().trim()
        val vehicleType = binding.tvViolation.text.toString().trim()
        val totalAmount = binding.tvTotalAmount.text.toString().trim()
        val plate = binding.plate.text.toString().trim()
        val selectedViolations = getSelectedViolations()

        // Validate input fields
        when {

            fullName.isEmpty() -> {
                binding.fullName.error = "Full name is required"
                binding.fullName.requestFocus()
            }
            birthdate.isEmpty() -> {
                binding.birthdate.error = "Birthdate is required"
                binding.birthdate.requestFocus()
            }
            sex.isEmpty() -> {
                binding.sex.error = "Sex is required"
                binding.sex.requestFocus()
            }
            address.isEmpty() -> {
                binding.address.error = "Address is required"
                binding.address.requestFocus()
            }
            vehicleType.isEmpty() || vehicleType == "Select Vehicle Type" -> {
                Toast.makeText(requireContext(), "Please select a vehicle type", Toast.LENGTH_SHORT).show()
            }
            totalAmount.isEmpty() -> {
                binding.tvTotalAmount.error = "Total amount is required"
                binding.tvTotalAmount.requestFocus()
            }
            selectedViolations.isEmpty() -> {
                Toast.makeText(requireContext(), "Please select at least one violation", Toast.LENGTH_SHORT).show()
            }
           
            else -> {

                Log.d("UserInfo", "User's First Name: $user")
                viewModelUserInfo.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
                    userInfo?.let {
                        user = it.firstName ?: ""
                        Log.d("UserInfo", "User's First Name: $user")
                        Log.d("UserInfo", "User's Last Name: ${it.lastName}")
                        viewModel.uploadViolation(
                            fullName,
                            licenseNumber,
                            birthdate,
                            sex,
                            address,
                            vehicleType,
                            totalAmount,
                            "With Licensed",
                            selectedViolations,
                            "${userInfo.firstName} ${userInfo.lastName}",
                            plate
                        )
                        viewModel.uploadNoLicensedViolationDriver(
                            fullName,
                            "",
                            birthdate,
                            sex,
                            address,
                            plate
                        )

                    }
                })



                findNavController().navigateUp()
            }
        }
    }


}