package com.example.smartticketing.views.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentUpdateInfoBinding
import com.example.smartticketing.model.UserInfo
import com.example.smartticketing.utilities.ProgressDialogUtils
import com.example.smartticketing.viewmodels.DashBoardViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar


class UpdateInfoFragment : Fragment() {
    private lateinit var binding : FragmentUpdateInfoBinding
    private val viewModelUserInfo: DashBoardViewModel by viewModels()
    private lateinit var selectedImage: Uri
    private val CAMERA_PERMISSION_CODE = 101
    private val IMAGE_PICK_GALLERY_CODE = 102
    private val IMAGE_PICK_CAMERA_CODE = 103

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateInfoBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init
        selectedImage = Uri.EMPTY
        binding.imgPersonal.setOnClickListener {
            showImagePickerDialog()
        }
        viewModelUserInfo.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
            userInfo?.let {
                binding.apply {
                    // Set the user's profile image
                    Glide.with(requireContext())
                        .load(userInfo.profileUrl)
                        .error(R.drawable.baseline_person_24)
                        .into(imgPersonal)

                    etFirstName.setText(userInfo.firstName)
                    etLastName.setText(userInfo.lastName)
                    etBirthDate.setText(userInfo.birthdate)
                    etAddress.setText(userInfo.address)

                    btnSave.setOnClickListener {
                        ProgressDialogUtils.showProgressDialog(requireContext(),"Loading...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            saveUserInfo(userInfo)
                            ProgressDialogUtils.dismissProgressDialog()
                        },1000)
                    }
                    etBirthDate.setOnClickListener {
                        showDatePickerDialog()
                    }
                }
            }
        })
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val bundle = Bundle().apply {
                        putInt("selectedFragmentId", R.id.navigation_account)
                    }
                    findNavController().navigate(R.id.holderFragment, bundle)
                }
            }
        )
        binding.backButton.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("selectedFragmentId", R.id.navigation_account)
            }
            findNavController().navigate(R.id.holderFragment, bundle)
        }
        viewModelUserInfo.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModelUserInfo.loadUserInfo()
        }

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

            binding.etBirthDate.setText("${month}/${day}/${year}")
        }
        datePicker.show(parentFragmentManager, "MaterialDatePicker")
    }
    private fun saveUserInfo(userInfo: UserInfo) {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val birthdate = binding.etBirthDate.text.toString()
        val address = binding.etAddress.text.toString()

        if (selectedImage == null || selectedImage == Uri.EMPTY) {
            viewModelUserInfo.updateInFirestore(
                Uri.EMPTY,
                firstName,
                lastName,
                birthdate,
                address
            )
        } else {

            viewModelUserInfo.updateInFirestore(
                selectedImage!!,
                firstName,
                lastName,
                birthdate,
                address
            )
        }
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                selectedImage = data?.data!!
                binding.imgPersonal.setImageURI(selectedImage)

                Log.d("TwoSignupFragment", "Image selected: $selectedImage")
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                binding.imgPersonal.setImageURI(selectedImage)

                Log.d("TwoSignupFragment", "Image selected: $selectedImage")
            }
        }
    }
}