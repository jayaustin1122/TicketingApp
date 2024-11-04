package com.example.smartticketing.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartticketing.model.ViolationItem
import com.google.firebase.firestore.FirebaseFirestore

class NoLicensedViewModel : ViewModel() {
    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus
    fun uploadViolation(
        fullName: String,
        licenseNumber: String,
        birthdate: String,
        sex: String,
        address: String,
        vehicleType: String,
        totalAmount: String,
        type: String,
        selectedViolations: List<ViolationItem>,
        officerApprehend: String,
        plate: String,
    ) {
        val timestamp = System.currentTimeMillis() / 1000
        val violationData = hashMapOf(
            "fullName" to fullName,
            "licenseNumber" to licenseNumber,
            "birthdate" to birthdate,
            "sex" to sex,
            "address" to address,
            "vehicleType" to vehicleType,
            "totalAmount" to totalAmount,
            "timestamp" to timestamp.toString() ,
            "type" to type,
            "selectedViolations" to selectedViolations,
            "officerApprehend" to officerApprehend,
            "plateNumber" to plate,
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("apprehensions")
            .add(violationData)
            .addOnSuccessListener {
                _uploadStatus.postValue("Data uploaded successfully")
            }
            .addOnFailureListener { e ->
                _uploadStatus.postValue("Error uploading data: ${e.message}")
            }
    }
    fun uploadNoLicensedViolationDriver(
        fullName: String,
        licenseNumber: String,
        birthdate: String,
        sex: String,
        address: String,
        plate: String,
    ) {
        val timestamp = System.currentTimeMillis() / 1000
        val violationData = hashMapOf(
            "fullName" to fullName,
            "licenseNumber" to licenseNumber,
            "birthdate" to birthdate,
            "sex" to sex,
            "address" to address,
            "timestamp" to timestamp.toString() ,
            "plateNumber" to plate,
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("no_licensed_drivers")
            .add(violationData)
            .addOnSuccessListener {
                _uploadStatus.postValue("Data uploaded successfully")
            }
            .addOnFailureListener { e ->
                _uploadStatus.postValue("Error uploading data: ${e.message}")
            }
    }
    fun uploadDriversNotRegisteredInDb(
        fullName: String,
        licenseNumber: String,
        birthdate: String,
        sex: String,
        address: String,
        plate: String,
    ) {
        val timestamp = System.currentTimeMillis() / 1000
        val violationData = hashMapOf(
            "fullName" to fullName,
            "licenseNumber" to licenseNumber,
            "birthdate" to birthdate,
            "sex" to sex,
            "address" to address,
            "timestamp" to timestamp.toString() ,
            "plateNumber" to plate,
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("registered_drivers")
            .add(violationData)
            .addOnSuccessListener {
                _uploadStatus.postValue("Data uploaded successfully")
            }
            .addOnFailureListener { e ->
                _uploadStatus.postValue("Error uploading data: ${e.message}")
            }
    }
fun checkIfLicenseExists(licenseNumber: String, callback: (Boolean) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("registered_drivers")
        .whereEqualTo("licensedNumber", licenseNumber)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                callback(true)
            } else {
                callback(false)
            }
        }
        .addOnFailureListener { e ->
                _uploadStatus.postValue("Error uploading data: ${e.message}")
                callback(false)
            }


}


}