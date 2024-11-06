package com.example.smartticketing.views.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.smartticketing.R
import com.example.smartticketing.databinding.FragmentHolderBinding
import com.example.smartticketing.views.profile.ProfileAccountFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class HolderFragment : Fragment() {
    private lateinit var binding : FragmentHolderBinding
    private lateinit var fragmentManager: FragmentManager
    private var isUserInfoLoaded = false
    private var selectedFragmentId: Int = R.id.navigation_Home
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHolderBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentManager = childFragmentManager
        arguments?.let {
            selectedFragmentId = it.getInt("selectedFragmentId", R.id.navigation_Home)
        }

        // Restore the selected fragment ID from savedInstanceState (on configuration change)
        savedInstanceState?.let {
            selectedFragmentId = it.getInt("selectedFragmentId", R.id.navigation_Home)
        }
        // Initialize your fragments
        val homeFragment = DashBoardFragment()
        val serviceFragment = ReportFragment()
        val accountUserFragment = ProfileAccountFragment()

        // Set up the BottomNavigationView and handle fragment transactions
        val bottomNavigationView: BottomNavigationView? = binding.bottomNavigationUser
        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            selectedFragmentId = item.itemId

            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_Home -> homeFragment
                R.id.navigation_services -> serviceFragment
                R.id.navigation_account -> accountUserFragment
                else -> return@setOnNavigationItemSelectedListener false
            }

            // Pass the selectedFragmentId to the fragment via a Bundle
            val bundle = Bundle().apply {
                putInt("selectedFragmentId", selectedFragmentId)
            }
            selectedFragment.arguments = bundle

            fragmentManager.beginTransaction()
                .replace(R.id.fragment_containerUser, selectedFragment)
                .commit()

            true
        }

        // Set the initial fragment if savedInstanceState is null (first load)
        if (savedInstanceState == null) {
            val initialFragment = when (selectedFragmentId) {
                R.id.navigation_Home -> homeFragment
                R.id.navigation_services -> serviceFragment
                R.id.navigation_account -> accountUserFragment
                else -> homeFragment
            }

            // Pass the initial item ID to the first fragment
            val bundle = Bundle().apply {
                putInt("selectedFragmentId", selectedFragmentId)
            }
            initialFragment.arguments = bundle

            fragmentManager.beginTransaction()
                .replace(R.id.fragment_containerUser, initialFragment)
                .commit()

            // Update the BottomNavigationView to reflect the selected item
            bottomNavigationView?.selectedItemId = selectedFragmentId
        }
    }

}