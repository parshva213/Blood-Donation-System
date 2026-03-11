package com.example.bd_system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bd_system.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val DB_URL = "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        // Explicitly pointing to the Asia-Southeast1 URL to prevent regional hangs
        database = FirebaseDatabase.getInstance(DB_URL)

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { authTask ->
                    if (authTask.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            database.getReference("Users").child(userId)
                                .get()
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        val snapshot = dbTask.result
                                        if (snapshot != null && snapshot.exists()) {
                                            val role = snapshot.child("role").getValue(String::class.java) ?: "user"
                                            completeLogin(userId, email, role)
                                        } else {
                                            Log.d("LoginActivity", "Profile missing for $userId, creating default.")
                                            createDefaultProfileAndLogin(userId, email)
                                        }
                                    } else {
                                        Log.e("LoginActivity", "Database error", dbTask.exception)
                                        Toast.makeText(this, "Connection Timeout. Please check your internet.", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Internal authentication error. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("LoginActivity", "Login failed", authTask.exception)
                        Toast.makeText(this, authTask.exception?.message ?: "Invalid email or password.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createDefaultProfileAndLogin(userId: String, email: String) {
        val newUser = mapOf(
            "uid" to userId,
            "email" to email,
            "fullName" to "User",
            "role" to "user",
            "bloodGroup" to "Unknown",
            "phone" to "",
            "address" to ""
        )
        
        database.getReference("Users").child(userId).setValue(newUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completeLogin(userId, email, "user")
                } else {
                    Toast.makeText(this, "Profile Creation Failed. Check database rules.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun completeLogin(userId: String, email: String, role: String) {
        if (role == "admin") {
            auth.signOut()
            Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save user details locally
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("email", email)
            putString("uid", userId)
            putString("role", role)
            apply()
        }
        
        Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, UserDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
