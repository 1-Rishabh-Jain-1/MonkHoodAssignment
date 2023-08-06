package com.rishabh.monkhoodassignment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Patterns
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rishabh.monkhoodassignment.databinding.ActivityUpdateUserBinding
import com.rishabh.monkhoodassignment.mvvm.ViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.UUID

class UpdateUser : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateUserBinding
    private var uri: Uri? = null
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
    private var imgBitmap : Bitmap? = null
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var firebaseStorageReference: StorageReference
    private var userId : String? = ""
    private lateinit var viewModel : ViewModel

    private enum class IMAGE_MODE{
        OPEN_CAMERA, OPEN_EXT_STORAGE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("Users", Context.MODE_PRIVATE)
        progressDialog = ProgressDialog(this)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        firebaseStorageReference = storage.reference
        viewModel = ViewModel()
        userId = intent.getStringExtra("userId")
        viewModel.getUserFromUserId(userId) { user ->
            uri = Uri.parse(user!!.imgProfile)
            Glide.with(this).load(user!!.imgProfile).into(binding.imgProfile)
            binding.editTextName.setText(user.name)
            binding.editTextEmail.setText(user.email)
            binding.editTextPhone.setText(user.phone.toString())
            binding.textViewDOB.setText(user.dob)
        }
        binding.editImg.setOnClickListener {
            showImageSelectionOptionDialog()
        }

        binding.imgProfile.setOnClickListener {
            showImageSelectionOptionDialog()
        }

        binding.textViewDOB.setOnClickListener {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, {view, year, month, day ->
                binding.textViewDOB.setText("${day}/${month + 1}/${year}")
            }, year, month, day)
            datePickerDialog.show()
        }

        binding.btnSave.setOnClickListener {
            if (!validateFields()) {
                return@setOnClickListener
            }
            updateDataInSharedPreference()
            uploadImageToFirebase(imgBitmap)
            finish()
        }
        binding.imgPrevious.setOnClickListener {
            finish()
        }
    }
    private fun validateFields(): Boolean {
        if(imgBitmap == null) {
            Toast.makeText(this@UpdateUser, "Select a profile image!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.editTextName.text.isEmpty()) {
            Toast.makeText(this@UpdateUser, "Enter a name!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.editTextEmail.text.isEmpty()) {
            Toast.makeText(this@UpdateUser, "Enter an email!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.editTextPhone.text.isEmpty()) {
            Toast.makeText(this@UpdateUser, "Enter a phone number!", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.textViewDOB.text.isEmpty()) {
            Toast.makeText(this@UpdateUser, "Enter a DOB!", Toast.LENGTH_SHORT).show()
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
    private fun pickImageFromGallery() {
        val pickPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(pickPictureIntent, 2)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhotoWithCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, 1)
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

    private fun StoreImageLocallyAndReturnAddress(imgBitmap: Bitmap?, userId: String?): String? {
        val directory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (directory != null) {
            val file = File(directory, userId + ".jpg")
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

    private fun updateDataInSharedPreference() {
        val editor = sharedPreferences.edit()
        val imageLink = StoreImageLocallyAndReturnAddress(imgBitmap, userId)
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
        upload.addOnSuccessListener {
            val task = it.metadata?.reference?.downloadUrl
            task?.addOnSuccessListener {
                uri = it
                updateDataInFirebase(uri)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to uplaod image!", Toast.LENGTH_SHORT).show()

        }
    }

    private fun updateDataInFirebase(uri: Uri?) {
        val userHashMap = hashMapOf<Any, Any>("userID" to userId!!,
            "name" to binding.editTextName.text.toString(),
            "imgProfile" to uri.toString(),
            "email" to binding.editTextEmail.text.toString(),
            "phone" to binding.editTextPhone.text.toString().toLong(),
            "DOB" to binding.textViewDOB.text.toString())
        firestore.collection("Users").document(userId!!).set(userHashMap)
    }
}