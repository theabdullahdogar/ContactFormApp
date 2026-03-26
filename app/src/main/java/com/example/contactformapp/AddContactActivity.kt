package com.example.contactformapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.TextView
import java.io.File

class AddContactActivity : AppCompatActivity() {

    private lateinit var ivPhoto: CircleImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var tvFormTitle: TextView

    private var selectedImageUri: String = ""
    private var existingContact: Contact? = null
    private var cameraImageUri: Uri? = null

    // Gallery picker
    private val pickFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it.toString()
                Glide.with(this).load(it).circleCrop().into(ivPhoto)
            }
        }

    // Camera capture
    private val captureFromCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                selectedImageUri = cameraImageUri.toString()
                Glide.with(this).load(cameraImageUri).circleCrop().into(ivPhoto)
            }
        }

    // Camera permission
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        ivPhoto     = findViewById(R.id.ivAddPhoto)
        etName      = findViewById(R.id.etName)
        etPhone     = findViewById(R.id.etPhone)
        etEmail     = findViewById(R.id.etEmail)
        etAddress   = findViewById(R.id.etAddress)
        btnSave     = findViewById(R.id.btnSave)
        btnCancel   = findViewById(R.id.btnCancel)
        tvFormTitle = findViewById(R.id.tvFormTitle)

        // Pre-fill if editing existing contact
        existingContact = intent.getSerializableExtra("contact") as? Contact
        existingContact?.let { c ->
            tvFormTitle.text    = "Edit Contact"
            etName.setText(c.name)
            etPhone.setText(c.phone)
            etEmail.setText(c.email)
            etAddress.setText(c.address)
            selectedImageUri    = c.profileImageUri
            if (c.profileImageUri.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(c.profileImageUri))
                    .circleCrop()
                    .into(ivPhoto)
            }
        }

        ivPhoto.setOnClickListener   { showImageSourceDialog() }
        btnCancel.setOnClickListener { finish() }
        btnSave.setOnClickListener   { saveContact() }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(
            "📷   Take Photo with Camera",
            "🖼️   Choose from Gallery",
            "❌   Remove Photo"
        )
        AlertDialog.Builder(this)
            .setTitle("Set Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickFromGallery.launch("image/*")
                    2 -> {
                        selectedImageUri = ""
                        ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
                        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val imageDir  = File(cacheDir, "images").also { it.mkdirs() }
        val imageFile = File(imageDir, "photo_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
        captureFromCamera.launch(cameraImageUri)
    }

    private fun saveContact() {
        val name    = etName.text.toString().trim()
        val phone   = etPhone.text.toString().trim()
        val email   = etEmail.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            etPhone.requestFocus()
            return
        }

        if (existingContact != null) {
            existingContact!!.name            = name
            existingContact!!.phone           = phone
            existingContact!!.email           = email
            existingContact!!.address         = address
            existingContact!!.profileImageUri = selectedImageUri
            Toast.makeText(this, "✅ Contact updated!", Toast.LENGTH_SHORT).show()
        } else {
            MainActivity.contactList.add(
                Contact(
                    name            = name,
                    phone           = phone,
                    email           = email,
                    address         = address,
                    profileImageUri = selectedImageUri
                )
            )
            Toast.makeText(this, "✅ Contact saved!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}