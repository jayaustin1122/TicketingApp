package com.example.smartticketing.utilities

import com.example.smartticketing.model.ViolationItem

object Violations {
    val violationsList = listOf(
        ViolationItem(code = "1", name = "No Helmet", amount = "1500.00"),
        ViolationItem(code = "2", name = "No Seatbelt", amount = "1,000.00"),
        ViolationItem(code = "3", name = "Over Speeding", amount = "1,000.00"),
        ViolationItem(code = "4", name = "Illegal Parking", amount = "1,000.00"),
        ViolationItem(code = "5", name = "Running a Red Light", amount = "1,000.00"),
        ViolationItem(code = "6", name = "No License", amount = "3,000.00"),
        ViolationItem(code = "7", name = "No Registration", amount = "2,000.00"),
        ViolationItem(code = "8", name = "Driving Under the Influence", amount = "20,000.00"),
        ViolationItem(code = "9", name = "Use of Mobile Phone While Driving", amount = "1,000.00"),
        ViolationItem(code = "10", name = "Failure to Signal", amount = "1,000.00")
    )
}
