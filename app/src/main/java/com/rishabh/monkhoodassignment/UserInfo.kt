package com.rishabh.monkhoodassignment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Patterns
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rishabh.monkhoodassignment.databinding.ActivityUserInfoBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.UUID

class UserInfo : AppCompatActivity() {
    private lateinit var binding : ActivityUserInfoBinding
    private var imgUri: Uri? = null
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
    private var imgBitmap : Bitmap? = null
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var firebaseStorageReference: StorageReference
    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("Users", Context.MODE_PRIVATE)
        progressDialog = ProgressDialog(this)
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseStorageReference = firebaseStorage.reference
        userId = UUID.randomUUID().toString()

        binding.editImg.setOnClickListener {
            showImageSelectionOptionDialog()
        }

        binding.imgProfile.setOnClickListener {
            showImageSelectionOptionDialog()
        }


        val calender = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, date ->
            calender.set(Calendar.YEAR,year)
            calender.set(Calendar.MONTH,month)
            calender.set(Calendar.DAY_OF_MONTH,date)
            updatedatelabel(calender,binding.textViewDOB)
        }

        binding.textViewDOB.setOnClickListener {
            DatePickerDialog(
                this,
                datePicker,
                calender.get(Calendar.YEAR),
                calender.get(Calendar.MONTH),
                calender.get(Calendar.DAY_OF_MONTH),
            ).show()
        }


        binding.btnSave.setOnClickListener {
            if (!validateFields()) {
                return@setOnClickListener
            }
            saveDataInSharedPreference()
            uploadImageToFirebase(imgBitmap)
            finish()
        }

        binding.imgPrevious.setOnClickListener {
            finish()
        }
    }

    private fun validateFields(): Boolean {
        if(imgBitmap == null) {
            Toast.makeText(this@UserInfo, "Select a profile image!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.editTextName.text.isEmpty()) {
            Toast.makeText(this@UserInfo, "Enter a name!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.editTextEmail.text.isEmpty()) {
            Toast.makeText(this@UserInfo, "Enter an email!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.editTextPhone.text.isEmpty()) {
            Toast.makeText(this@UserInfo, "Enter a phone number!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.textViewDOB.text.isEmpty()) {
            Toast.makeText(this@UserInfo, "Enter a DOB!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(binding.editTextEmail.text.toString()).matches()) {
            Toast.makeText(this, "Enter a valid email address!", Toast.LENGTH_SHORT).show()
            binding.editTextEmail.requestFocus()
            return false
        }
        return true
    }

    private fun showImageSelectionOptionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_select_image_options)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.layoutTakePicture).setOnClickListener {
            takePhotoWithCamera()
            dialog.dismiss()
        }

        dialog.findViewById<ConstraintLayout>(R.id.layoutSelectFromGallery).setOnClickListener {
            pickImageFromGallery()
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            //See description at declaration
        }
        dialog.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhotoWithCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, 1)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun pickImageFromGallery() {
        val pickPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(pickPictureIntent, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    imgBitmap = data?.extras?.get("data") as Bitmap
                    try {
                        binding.imgProfile.setImageBitmap(imgBitmap)
                    }catch (e: Exception){}
                }
                2 -> {
                    val imageUri = data?.data
                    imgBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    try {
                        binding.imgProfile.setImageBitmap(imgBitmap)
                    }catch (e :Exception){
                    }
                }
            }
        }
    }

    private fun storeImageLocallyAndReturnAddress(imgBitmap: Bitmap?, userId: String?): String? {
        val directory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (directory != null) {
            val file = File(directory, "$userId.jpg")
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                imgBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                return file.path
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    fileOutputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun saveDataInSharedPreference() {
        val editor = sharedPreferences.edit()
        val imageLink = storeImageLocallyAndReturnAddress(imgBitmap, userId)
        val currentUser = "${userId}, ${binding.editTextName.text}, ${imageLink}," +
                "${binding.editTextEmail.text}, ${binding.editTextPhone.text}," +
                "${binding.textViewDOB.text}"
        editor.putString(userId, currentUser)
        editor.apply()
    }

    private fun uploadImageToFirebase(imgBitmap: Bitmap?) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        imgBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        val path = firebaseStorageReference.child("Photos/${UUID.randomUUID()}.jpg")
        val upload = path.putBytes(data)
        upload.addOnSuccessListener { it ->
            val task = it.metadata?.reference?.downloadUrl
            task?.addOnSuccessListener {
                imgUri = it
                saveDataInFirebase(imgUri)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to uplaod image!", Toast.LENGTH_SHORT).show()

        }
    }

    private fun saveDataInFirebase(uri: Uri?) {
        val userHashMap = hashMapOf<Any, Any>("userId" to userId!!,
            "name" to binding.editTextName.text.toString(),
            "imgProfile" to uri.toString(),
            "email" to binding.editTextEmail.text.toString(),
            "phone" to binding.editTextPhone.text.toString().toLong(),
            "DOB" to binding.textViewDOB.text.toString())
        firestore.collection("Users").document(userId).set(userHashMap)
    }
}

@SuppressLint("SimpleDateFormat", "SetTextI18n")
private fun updatedatelabel(calender: Calendar, collDate : TextView) {
    val day = SimpleDateFormat("dd").format(calender.time)
    val month = SimpleDateFormat("MM").format(calender.time)
    val year = SimpleDateFormat("yyyy").format(calender.time)
    collDate.text = "${day}-${month}-${year}"
}
