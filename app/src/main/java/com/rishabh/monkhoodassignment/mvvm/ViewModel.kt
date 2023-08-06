package com.rishabh.monkhoodassignment.mvvm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rishabh.monkhoodassignment.models.Users
import kotlinx.coroutines.Dispatchers
import com.rishabh.MyApplication
import kotlinx.coroutines.launch

class ViewModel: ViewModel() {
    val userListFirebase = MutableLiveData<List<Users>>()
    val userListSharedPreferences = MutableLiveData<List<Users>>()

    val firestore = FirebaseFirestore.getInstance()
    val sharedPreferences: SharedPreferences = MyApplication.getAppContext()
        .getSharedPreferences("Users", Context.MODE_PRIVATE)

    fun getUsersFromFirebase() : LiveData<List<Users>> {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("Users")
                    .addSnapshotListener {snapshot, exception ->
                        if (exception!=null) {
                            return@addSnapshotListener
                        }
                        val listOfUsers = snapshot?.documents?.mapNotNull {
                            it.toObject(Users::class.java)
                        }
                        userListFirebase.postValue(listOfUsers!!)
                    }
            } catch (e: Exception) {
                // handle exception
            }
        }
        return userListFirebase
    }

    fun deleteUserFromFirebase(userId: String) {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(MyApplication.getAppContext(), "Invalid user ID!", Toast.LENGTH_SHORT).show()
            return
        }
        val collection = firestore.collection("Users")
        val document = collection.document(userId)
        document.delete()
            .addOnSuccessListener {
                Toast.makeText(MyApplication.getAppContext(), "User deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(MyApplication.getAppContext(), "User deletion failed!\nTry again", Toast.LENGTH_SHORT).show()
                Log.e("DeleteUser", "Error deleting user with ID: $userId", exception)
            }
    }

    fun getUsersFromSharedPreferences() : LiveData<List<Users>> {
        val listOfUsers = mutableListOf<Users>()
        val allData = sharedPreferences.all
        for ((key, value) in allData) {
            val userArray = (value as String).split(",")
            if (userArray.size == 6) {
                val userId = userArray[0]
                val userName = userArray[1]
                val userImgProfile = userArray[2]
                Toast.makeText(MyApplication.getAppContext(), "$userImgProfile", Toast.LENGTH_SHORT).show()
                Log.d("hellothere", "$userImgProfile")
                val userEmail = userArray[3]
                val userPhone = userArray[4].trim()
                val userDob = userArray[5]
                try {
                    val phone = userPhone.toLong()
                    val user = Users(userId, userName, userImgProfile, userEmail, phone, userDob)
                    listOfUsers.add(user)
                } catch (e: NumberFormatException) {
                    // Handle the NumberFormatException
                }
            }
        }
        userListSharedPreferences.postValue(listOfUsers!!)
        return userListSharedPreferences
    }

    fun deleteUserFromSharedPreferences(userId : String){
        val editor = sharedPreferences.edit()
        editor.remove(userId).apply()
        getUsersFromSharedPreferences()
    }

    fun getUserFromUserId(userId: String?, callback: (Users?) -> Unit) {
        if (userId != null) {
            firestore.collection("Users").document(userId).get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument!=null) {
                        val user = userDocument.toObject(Users::class.java)
                        Log.d("ImageCheck", "${user?.imgProfile}")
                        callback(user)
                    } else {
                        callback(null)
                    }
                }
        }
    }
}