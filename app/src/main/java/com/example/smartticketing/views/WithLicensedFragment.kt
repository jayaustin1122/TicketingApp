package com.example.smartticketing.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartticketing.R
import com.example.smartticketing.adapters.ViolationAdapter
import com.example.smartticketing.databinding.FragmentWithLicensedBinding
import com.example.smartticketing.model.RegisteredDrivers
import com.example.smartticketing.model.ViolationItem
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.utilities.Violations
import com.example.smartticketing.utilities.Violations.violationsList
import com.example.smartticketing.viewmodels.DashBoardViewModel
import com.example.smartticketing.viewmodels.NoLicensedViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class WithLicensedFragment : Fragment() {
    private lateinit var binding: FragmentWithLicensedBinding
    private val viewModel: NoLicensedViewModel by viewModels()
    private val viewModelUserInfo: DashBoardViewModel by viewModels()
    private lateinit var adapter: ViolationAdapter
    private val violationList = mutableListOf<ViolationItem>()
    private val selectedViolations = mutableListOf<ViolationItem>()
    private lateinit var storedDrivers: List<RegisteredDrivers>
    private var totalAmount = 0.00
    private lateinit var driverNamesAdapter: ArrayAdapter<String>
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var user: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWithLicensedBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModelUserInfo.loadUserInfo()


        val date = LocalDate.now()
        val currentTime = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        binding.tvDate.text = date.format(dateFormatter)
        binding.tvTime.text = currentTime.format(timeFormatter)

        //when delete button clicked in adapter will deduct amount itrem remove
        adapter = ViolationAdapter(violationList) { removedAmount ->
            updateTotalAmount((-removedAmount).toString())
        }

        binding.birthdate.setOnClickListener {
            showDatePickerDialog()
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
                        //validateData()
                        viewModel.checkIfLicenseExists(binding.licenseNumber.text.toString().trim()) { exists ->
                            if (exists) {
                                Log.d("Firestore", "License found")
                                validateData1()
                            } else {
                                Log.d("Firestore", "License not found")
                                validateData2()
                            }
                        }
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
        // for searching drivers
        binding.tvSearchDrivers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    searchDrivers(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        //item clicked search drivers
        binding.tvSearchDrivers.setOnItemClickListener { parent, _, position, _ ->
            val selectedDriver = storedDrivers[position]
            binding.fullName.setText("${selectedDriver.firstName}${selectedDriver.lastName}")
            binding.address.setText(selectedDriver.address)
            binding.sex.setText(selectedDriver.sex)
            Glide.with(requireContext())
                .load(selectedDriver.image)
                .error(R.drawable.baseline_person_24)
                .into(binding.avatar)
            binding.birthdate.setText(selectedDriver.birthdate)
            binding.licenseNumber.setText(selectedDriver.licensedNumber)
            binding.tvSearchDrivers.setText("")
        }
        //violations
        binding.tvViolation.setOnItemClickListener { parent, _, position, _ ->
            val selectedViolation = Violations.violationsList[position]
            val newViolation = ViolationItem(
                name = selectedViolation.name,
                code = selectedViolation.code,
                amount = selectedViolation.amount
            )
            adapter.addViolation(newViolation)
            updateTotalAmount(newViolation.amount)
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

        val violationNames = Violations.violationsList.map { it.name }.toTypedArray()

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

    private fun searchDrivers(query: String) {
        firestore.collection("registered_drivers")
            .get()
            .addOnSuccessListener { documents ->
                val drivers = mutableListOf<RegisteredDrivers>()
                drivers.clear()

                // Populate the list with drivers
                for (document in documents) {
                    val driver = document.toObject(RegisteredDrivers::class.java)
                    drivers.add(driver)
                }

                val lowerCaseQuery = query.lowercase()

                // Filter the drivers by license number
                val filteredDrivers = drivers.filter { driver ->
                    driver.licensedNumber.lowercase().contains(lowerCaseQuery)
                }

                // Extract the license numbers for display in the adapter
                val filteredLicenseNumbers = filteredDrivers.map { it.licensedNumber }

                driverNamesAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    filteredLicenseNumbers
                )

                binding.tvSearchDrivers.setAdapter(driverNamesAdapter)

                // Assign the filtered drivers to storedDrivers (this expects a list of RegisteredDrivers)
                storedDrivers = filteredDrivers
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


    @SuppressLint("SetTextI18n")
    private fun updateTotalAmount(amount: String) {
        // Remove commas and convert the amount to a Long
        val cleanedAmount = amount.replace(",", "").toDouble() // First remove commas
        totalAmount += cleanedAmount.toLong() // Convert it to Long after cleaning
        binding.tvTotalAmount.text = totalAmount.toString()
    }

    private fun getSelectedViolations(): List<ViolationItem> {
        return selectedViolations
    }

    private fun validateData1() {
        val fullName = binding.fullName.text.toString().trim()
        val licenseNumber = binding.licenseNumber.text.toString().trim()
        val birthdate = binding.birthdate.text.toString().trim()
        val sex = binding.sex.text.toString().trim()
        val address = binding.address.text.toString().trim()
        val vehicleType = binding.tvVehicle.text.toString().trim()
        val totalAmount = binding.tvTotalAmount.text.toString().trim()
        val selectedViolations = getSelectedViolations()
        val plate = binding.plate.text.toString().trim()
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
                Toast.makeText(requireContext(), "Please select a vehicle type", Toast.LENGTH_SHORT)
                    .show()
            }

            plate.isEmpty() -> {
                binding.plate.error = "License number is required"
                binding.plate.requestFocus()
            }
            totalAmount.isEmpty() -> {
                binding.tvTotalAmount.error = "Total amount is required"
                binding.tvTotalAmount.requestFocus()
            }

            selectedViolations.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one violation",
                    Toast.LENGTH_SHORT
                ).show()
            }


            else -> {

                // Show dialog with all the data
                val dialogView = layoutInflater.inflate(R.layout.diaog_review, null)
                val builder = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setTitle("Review Information")
                    .setPositiveButton("Confirm") { dialog, _ ->
                        // Handle confirm button click here
                        viewModelUserInfo.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
                            userInfo?.let {
                                user = it.firstName ?: ""
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
                                    licenseNumber,
                                    birthdate,
                                    sex,
                                    address,
                                    plate
                                )
                            }
                        })
                        Toast.makeText(requireContext(), "Violation Uploaded Successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Cancel", null)

                val dialog = builder.create()

                // Set the data in the dialog
                dialogView.findViewById<TextView>(R.id.textFullName).text = "Full Name: $fullName"
                dialogView.findViewById<TextView>(R.id.textLicenseNumber).text = "License Number: $licenseNumber"
                dialogView.findViewById<TextView>(R.id.textBirthdate).text = "Birthdate: $birthdate"
                dialogView.findViewById<TextView>(R.id.textSex).text = "Sex: $sex"
                dialogView.findViewById<TextView>(R.id.textAddress).text = "Address: $address"
                dialogView.findViewById<TextView>(R.id.textVehicleType).text = "Vehicle Type: $vehicleType"
                dialogView.findViewById<TextView>(R.id.textTotalAmount).text = "Total Amount: $totalAmount"
                dialogView.findViewById<TextView>(R.id.textPlate).text = "Plate: $plate"
                dialogView.findViewById<TextView>(R.id.textSelectedViolations).text = "Violations: ${selectedViolations.joinToString(", ")}"

                dialog.show()
            }
        }
    }


    private fun validateData2() {
        val fullName = binding.fullName.text.toString().trim()
        val licenseNumber = binding.licenseNumber.text.toString().trim()
        val birthdate = binding.birthdate.text.toString().trim()
        val sex = binding.sex.text.toString().trim()
        val address = binding.address.text.toString().trim()
        val vehicleType = binding.tvViolation.text.toString().trim()
        val totalAmount = binding.tvTotalAmount.text.toString().trim()
        val selectedViolations = getSelectedViolations()
        val plate = binding.plate.text.toString().trim()
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
                Toast.makeText(requireContext(), "Please select a vehicle type", Toast.LENGTH_SHORT)
                    .show()
            }

            plate.isEmpty() -> {
                binding.plate.error = "License number is required"
                binding.plate.requestFocus()
            }
            totalAmount.isEmpty() -> {
                binding.tvTotalAmount.error = "Total amount is required"
                binding.tvTotalAmount.requestFocus()
            }

            selectedViolations.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one violation",
                    Toast.LENGTH_SHORT
                ).show()
            }


            else -> {

                Log.d("UserInfo", "User's First Name: $user")
                viewModelUserInfo.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
                    userInfo?.let {
                        user = it.firstName ?: ""
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
                        viewModel.uploadDriversNotRegisteredInDb(
                            fullName,
                            licenseNumber,
                            birthdate,
                            sex,
                            address,
                            plate
                        )

                    }
                })


                ProgressDialogUtils.dismissProgressDialog()
                findNavController().navigateUp()
            }
        }
    }


}