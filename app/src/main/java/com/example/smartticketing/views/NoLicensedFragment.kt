package com.example.smartticketing.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartticketing.R
import com.example.smartticketing.adapters.ViolationAdapter
import com.example.smartticketing.databinding.FragmentNoLicensedBinding
import com.example.smartticketing.model.ViolationItem
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.utilities.Violations.violationsList
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
    private var totalAmount = 0.0
    private val viewModelUserInfo: DashBoardViewModel by viewModels()
    private var user: String? = null
    private lateinit var selectedImage: Uri
    private val CAMERA_PERMISSION_CODE = 101
    private val IMAGE_PICK_GALLERY_CODE = 102
    private val IMAGE_PICK_CAMERA_CODE = 103
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoLicensedBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                selectedImage = data?.data!!
                binding.imageCapture.visibility = View.VISIBLE
                binding.imageCapture.setImageURI(selectedImage)

                Log.d("TwoSignupFragment", "Image selected: $selectedImage")
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                binding.imageCapture.visibility = View.VISIBLE
                binding.imageCapture.setImageURI(selectedImage)

                Log.d("TwoSignupFragment", "Image selected: $selectedImage")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load user info and set initial values
        viewModelUserInfo.loadUserInfo()
        selectedImage = Uri.EMPTY

        val violationType = arguments?.getString("violationType")
        Toast.makeText(requireContext(), "$violationType", Toast.LENGTH_SHORT).show();
        // Capture image functionality
        binding.capture.setOnClickListener {
            showImagePickerDialog()
        }

        // Set current date and time
        val date = LocalDate.now()
        val currentTime = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        binding.tvDate.text = date.format(dateFormatter)
        binding.tvTime.text = currentTime.format(timeFormatter)

        // Initialize adapter for RecyclerView
        adapter = ViolationAdapter(violationList) { removedAmount ->
            updateTotalAmount((-removedAmount).toString())
        }

        binding.rvAddViolations.adapter = adapter
        binding.rvAddViolations.layoutManager = LinearLayoutManager(requireContext())

        // Handle "Save and Print" button click with a confirmation dialog
        binding.saveAndPrint.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Once Data saved will not be able to edit")
                .setTitle("Note!")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, which ->
                    ProgressDialogUtils.showProgressDialog(requireContext(), "Loading...")

                    Handler(Looper.getMainLooper()).postDelayed({
                        validateData()
                        ProgressDialogUtils.dismissProgressDialog()
                    }, 1000)
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.cancel()
                }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        // Handle back button click
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Show date picker dialog for birthdate
        binding.birthdate.setOnClickListener {
            showImagePickerDialog()
        }

        // Add violation from dropdown when selected
        binding.tvViolation.setOnItemClickListener { parent, _, position, _ ->
            val selectedViolation = violationsList[position]
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

        // Initialize dropdown for vehicles
        val vehiclesArray = resources.getStringArray(R.array.vehicles)
        val vehicleAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, vehiclesArray
        )
        binding.tvVehicle.setAdapter(vehicleAdapter)

        // Handle vehicle dropdown selection
        binding.tvVehicle.setOnItemClickListener { parent, _, position, _ ->
            val selectedVehicle = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(), "Selected: $selectedVehicle", Toast.LENGTH_SHORT).show()
        }

        // Initialize violation dropdown with violation names
        val violationNames = violationsList.map { it.name }.toTypedArray()
        val violationAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, violationNames
        )
        binding.tvViolation.setAdapter(violationAdapter)
        val noLicenseViolation = ViolationItem(code = "6", name = "No License", amount = "3,000.00")
        adapter.addViolation(noLicenseViolation)
        updateTotalAmount(noLicenseViolation.amount)
        selectedViolations.add(noLicenseViolation)
        violationType?.let { type ->
            val initialViolation = violationsList.find { it.name == type }
            initialViolation?.let { violation ->
                val newViolation = ViolationItem(
                    name = violation.name,
                    code = violation.code,
                    amount = violation.amount
                )
                // Add the selected violation to the RecyclerView and update the total amount
                adapter.addViolation(newViolation)
                updateTotalAmount(newViolation.amount)
                selectedViolations.add(newViolation)
            }
        }

        // Observe upload status from ViewModel
        viewModel.uploadStatus.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Image From")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        if (checkCameraPermission()) {
                            pickImageFromCamera()
                        } else {
                            requestCameraPermission()
                        }
                    }

                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickImageFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        selectedImage =
            requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
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

    private fun validateData() {
        val fullName = binding.fullName.text.toString().trim()
        val licenseNumber = binding.licenseNumber.text.toString().trim()
        val birthdate = binding.birthdate.text.toString().trim()
        val sex = binding.sex.text.toString().trim()
        val address = binding.address.text.toString().trim()
        val vehicleType = binding.tvVehicle.text.toString().trim()
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
                                    "No Licensed",
                                    selectedViolations,
                                    "${userInfo.firstName} ${userInfo.lastName}",
                                    plate,
                                    selectedImage!!.toString()
                                )
                                viewModel.uploadNoLicensedViolationDriver(
                                    fullName,
                                    "none",
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


}