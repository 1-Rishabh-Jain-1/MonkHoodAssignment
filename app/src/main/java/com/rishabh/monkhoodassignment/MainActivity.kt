package com.rishabh.monkhoodassignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rishabh.monkhoodassignment.databinding.ActivityMainBinding
import com.rishabh.monkhoodassignment.ui.FirebaseFragment
import com.rishabh.monkhoodassignment.ui.SharedPreferencesFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView = binding.bottomNav
        val firebaseFragment = FirebaseFragment()
        val sharedPreferences = SharedPreferencesFragment()

        setFragment(firebaseFragment)

        binding.bottomNav.background = null

        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.firebaseActivity -> {
                    setFragment(firebaseFragment)
                }

                R.id.sharedPreferencesActivity -> {
                    setFragment(sharedPreferences)
                }
            }
            true
        }

        binding.addUser.setOnClickListener {
            val intent = Intent(this, UserInfo::class.java)
            startActivity(intent)
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentHolder, fragment)
            commit()

        }
    }
}