package com.example.smartticketing.views

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentMemberBinding
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.viewmodels.DashBoardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MemberFragment : Fragment() {
    private lateinit var binding: FragmentMemberBinding
    private lateinit var selectedImage: Uri
    private val CAMERA_PERMISSION_CODE = 101
    private val IMAGE_PICK_GALLERY_CODE = 102
    private val IMAGE_PICK_CAMERA_CODE = 103
    private val viewModel: DashBoardViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var currentUserName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemberBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentUserName()
        binding.btnLogout.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(), "Logging out...")
            viewModel.signOut()
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                ProgressDialogUtils.dismissProgressDialog()
                findNavController().navigate(R.id.signInFragment)
            }
        }
        selectedImage = Uri.EMPTY


        binding.btnSubmit.setOnClickListener {
            ProgressDialogUtils.showProgressDialog(requireContext(), "Submitting...")
            validateData()
        }

        binding.imageCapture.setOnClickListener {
            showImagePickerDialog()
        }

        // Date and Time dialogs
        binding.dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.loadUserInfo()
        }
        }

    private fun showImagePickerDialog() {
        val options = arrayOf("Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Image From")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                when (which) {
                    0 -> pickImageFromGallery()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                selectedImage = data?.data!!
                binding.imageCapture.setImageURI(selectedImage)
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                binding.imageCapture.setImageURI(selectedImage)
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth/${month + 1}/$year"
                binding.dateEditText.setText(date)
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                binding.timeEditText.setText(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.show()
    }

    private fun validateData() {
        val date = binding.dateEditText.text.toString()
        val time = binding.timeEditText.text.toString()
        val plateNumber = binding.plateNumberEditText.text.toString()
        val details = binding.otherDetailsEditText.text.toString()

        if (date.isNotEmpty() && time.isNotEmpty() && plateNumber.isNotEmpty() && selectedImage != Uri.EMPTY) {
            uploadImageAndData(date, time, plateNumber, details)
        } else {
            ProgressDialogUtils.dismissProgressDialog()
            Toast.makeText(requireContext(), "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCurrentUserName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("fullName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        currentUserName = "$firstName $lastName"
                        Log.d("MemberFragment", "Current User Name: $currentUserName")
                    } else {
                        Log.d("MemberFragment", "No user document found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MemberFragment", "Error getting user document: ", exception)
                }
        }
    }

    private fun uploadImageAndData(date: String, time: String, plate: String, details: String) {
        if (currentUserName.isEmpty()) {
            Toast.makeText(requireContext(), "User information not available", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = storage.reference.child("incident_images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(selectedImage)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    val incidentData = mapOf(
                        "date" to date,
                        "time" to time,
                        "plateNumber" to plate,
                        "details" to details,
                        "imageUrl" to imageUrl.toString(),
                        "reportedBy" to currentUserName
                    )
                    firestore.collection("incidentReports")
                        .add(incidentData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Incident reported successfully", Toast.LENGTH_SHORT).show()
                            binding.dateEditText.text?.clear()
                            binding.timeEditText.text?.clear()
                            binding.plateNumberEditText.text?.clear()
                            binding.otherDetailsEditText.text?.clear()
                            Glide.with(this).load(R.drawable.ico).into(binding.imageCapture)
                            ProgressDialogUtils.dismissProgressDialog()
                        }
                        .addOnFailureListener { e ->
                            ProgressDialogUtils.dismissProgressDialog()
                            Toast.makeText(requireContext(), "Failed to report incident: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                ProgressDialogUtils.dismissProgressDialog()
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
