package com.example.bd_system

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.bd_system.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var originalProfile: UserProfile? = null
    private val DB_URL = "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(DB_URL)

        setupSidebar()
        setupBloodGroupDropdown()
        loadUserProfile()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkForChanges()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etFullName.addTextChangedListener(textWatcher)
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPhone.addTextChangedListener(textWatcher)
        binding.etBloodGroup.addTextChangedListener(textWatcher)
        binding.etAddress.addTextChangedListener(textWatcher)

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupSidebar() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                }
                R.id.nav_donate -> {
                    startActivity(Intent(this, DonationHistoryActivity::class.java))
                }
                R.id.nav_request -> {
                    startActivity(Intent(this, MyRequestsActivity::class.java))
                }
                R.id.nav_profile -> {
                    // Already here
                }
                R.id.nav_change_password -> {
                    startActivity(Intent(this, ChangePasswordActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        
        binding.btnNavLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBloodGroupDropdown() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bloodGroups)
        binding.etBloodGroup.setAdapter(adapter)
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalProfile = snapshot.getValue(UserProfile::class.java)
                originalProfile?.let {
                    binding.etFullName.setText(it.fullName)
                    binding.etEmail.setText(it.email)
                    binding.etPhone.setText(it.phone)
                    binding.etBloodGroup.setText(it.bloodGroup, false)
                    binding.etAddress.setText(it.address)
                }
                binding.btnUpdateProfile.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkForChanges() {
        originalProfile?.let {
            val hasChanged = binding.etFullName.text.toString() != it.fullName ||
                    binding.etEmail.text.toString() != it.email ||
                    binding.etPhone.text.toString() != it.phone ||
                    binding.etBloodGroup.text.toString() != it.bloodGroup ||
                    binding.etAddress.text.toString() != it.address

            binding.btnUpdateProfile.visibility = if (hasChanged) View.VISIBLE else View.GONE
        }
    }

    private fun updateProfile() {
        val userId = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "fullName" to binding.etFullName.text.toString(),
            "email" to binding.etEmail.text.toString(),
            "phone" to binding.etPhone.text.toString(),
            "bloodGroup" to binding.etBloodGroup.text.toString(),
            "address" to binding.etAddress.text.toString()
        )

        database.getReference("Users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                loadUserProfile()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
            }
    }
}
