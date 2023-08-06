package com.rishabh.monkhoodassignment.mvvm

import android.content.Context
import android.content.SharedPreferences
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
    val sharedPreferences: SharedPreferences = MyApplication.getAppContext().getSharedPreferences("Users",
        Context.MODE_PRIVATE)

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

    fun deleteUserFromFirebase(userId : String){
        val collection = firestore.collection("Users")
        val document = collection.document(userId)
        document.delete()
            .addOnSuccessListener {
                Toast.makeText(MyApplication.getAppContext(), "User deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(MyApplication.getAppContext(), "User deletion failed!\nTry again", Toast.LENGTH_SHORT).show()
            }
    }

    fun getUsersFromSharedPreferences() : LiveData<List<Users>> {
        val listOfUsers = mutableListOf<Users>()
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            val userArray = (value as String).split(",")
            if (userArray.size == 6) {
                val userId = userArray[0]
                val userName = userArray[1]
                val userImgProfile = userArray[2]
                val userEmail = userArray[3]
                val userPhone = userArray[4]
                val userDob = userArray[5]
                val user = Users(userId, userName, userImgProfile, userEmail, userPhone.toLong(), userDob)
                listOfUsers.add(user)
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
        var user: Users? = null
        firestore.collection("Users").document(userId!!).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument!=null) {
                    user = userDocument.toObject(Users::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
    }
}