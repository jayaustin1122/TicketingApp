package com.example.smartticketing.views.nav

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.smartticketing.databinding.FragmentReportBinding
import com.example.smartticketing.viewmodels.DashBoardViewModel
import com.db.williamchart.data.DataPoint
import com.db.williamchart.view.BarChartView
import com.db.williamchart.view.HorizontalBarChartView
import com.example.smartticketing.R

class ReportFragment : Fragment() {
    private lateinit var binding: FragmentReportBinding
    private val viewModel: DashBoardViewModel by viewModels()
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

        viewModel.driver.observe(viewLifecycleOwner, Observer { count ->
            if (count != null) {
                updateBarChart(count, viewModel.apprehendCount.value ?: 0)
            }
        })

        viewModel.apprehendCount.observe(viewLifecycleOwner, Observer { count ->
            if (count != null) {
                updateBarChart(viewModel.driver.value ?: 0, count)
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.loadUserInfo()
            viewModel.loadDriverCount()
            viewModel.loadApprehends()
        }
    }

    private fun updateBarChart(driverCount: Int, apprehendCount: Int) {
        val dataPoints = listOf(
            Pair("Drivers", driverCount.toFloat()),
            Pair("Apprehends", apprehendCount.toFloat())
        )
        val dataPoints2 = listOf(
            Pair("Drivers", driverCount.toFloat()),
            Pair("Apprehends", apprehendCount.toFloat())
        )

        // Update Vertical Bar Chart
        binding.barChart.show(dataPoints)

        // Update Horizontal Bar Chart
        binding.barChartHorizontal.show(dataPoints2)
    }

}
