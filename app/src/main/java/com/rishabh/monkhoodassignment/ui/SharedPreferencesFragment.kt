package com.rishabh.monkhoodassignment.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rishabh.monkhoodassignment.adapters.UserInfoAdapter
import com.rishabh.monkhoodassignment.databinding.FragmentSharedPreferencesBinding
import com.rishabh.monkhoodassignment.mvvm.ViewModel

class SharedPreferencesFragment : Fragment() {
    private lateinit var binding: FragmentSharedPreferencesBinding
    private lateinit var viewModel: ViewModel
    private lateinit var userAdapter: UserInfoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSharedPreferencesBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[ViewModel::class.java]
        userAdapter = UserInfoAdapter()
        binding.recyclerUserListSharedPreferences.adapter = userAdapter
        binding.recyclerUserListSharedPreferences.layoutManager = LinearLayoutManager(requireContext())
        viewModel.getUsersFromSharedPreferences().observe(viewLifecycleOwner) {
            userAdapter.setList(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUsersFromSharedPreferences().observe(viewLifecycleOwner) {
            userAdapter.setList(it)
        }
    }
}