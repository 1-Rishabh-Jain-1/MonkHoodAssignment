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
import com.rishabh.monkhoodassignment.databinding.FragmentFirebaseBinding
import com.rishabh.monkhoodassignment.mvvm.ViewModel

class FirebaseFragment : Fragment() {
    private lateinit var binding: FragmentFirebaseBinding
    private lateinit var viewModel : ViewModel
    private lateinit var userAdapter : UserInfoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFirebaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ViewModel::class.java]
        userAdapter = UserInfoAdapter()
        binding.recyclerUserListFirebase.adapter = userAdapter
        binding.recyclerUserListFirebase.layoutManager = LinearLayoutManager(requireContext())
        viewModel.getUsersFromFirebase().observe(viewLifecycleOwner, Observer {
            userAdapter.setList(it)
        })
    }
}