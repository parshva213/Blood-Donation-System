package com.example.bd_system

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bd_system.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val DB_URL = "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        // Explicitly pointing to the Asia-Southeast1 URL
        database = FirebaseDatabase.getInstance(DB_URL)

        setupBloodGroupDropdown()

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun setupBloodGroupDropdown() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bloodGroups)
        binding.etBloodGroup.setAdapter(adapter)
    }

    private fun performRegistration() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val bloodGroup = binding.etBloodGroup.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        
        val selectedGenderId = binding.rgGender.checkedRadioButtonId
        val gender = if (selectedGenderId != -1) {
            findViewById<RadioButton>(selectedGenderId).text.toString()
        } else {
            ""
        }

        if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && 
            bloodGroup.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty() && gender.isNotEmpty()) {
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val user = mapOf(
                            "uid" to userId,
                            "fullName" to fullName,
                            "email" to email,
                            "bloodGroup" to bloodGroup,
                            "gender" to gender,
                            "phone" to phone,
                            "address" to address,
                            "role" to "user"
                        )
                        
                        if (userId != null) {
                            database.getReference("Users").child(userId).setValue(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        val dbHandler = MyDbHandler(this)
                                        dbHandler.registerUser(fullName, email, password, bloodGroup, gender, phone, address, userId)
                                        
                                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                        
                                        // Auto sign out after registration to allow fresh login
                                        auth.signOut()
                                        finish()
                                    } else {
                                        Log.e("RegisterActivity", "Database error", dbTask.exception)
                                        Toast.makeText(this, "Account created, but failed to save profile: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    } else {
                        val exception = task.exception
                        Log.e("RegisterActivity", "Auth error", exception)
                        Toast.makeText(this, "Registration Failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
