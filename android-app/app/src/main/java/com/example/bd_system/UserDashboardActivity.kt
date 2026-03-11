package com.example.bd_system

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.bd_system.databinding.ActivityUserDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val DB_URL = "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(DB_URL)

        setupSidebar()
        loadUserData()

        binding.cardMyRequests.setOnClickListener {
            startActivity(Intent(this, MyRequestsActivity::class.java))
        }

        binding.cardDonationHistory.setOnClickListener {
            startActivity(Intent(this, DonationHistoryActivity::class.java))
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
                    // Already here
                }
                R.id.nav_donate -> {
                    startActivity(Intent(this, DonationHistoryActivity::class.java))
                }
                R.id.nav_request -> {
                    startActivity(Intent(this, MyRequestsActivity::class.java))
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
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

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        database.getReference("Users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fullName = snapshot.child("fullName").getValue(String::class.java) ?: ""
                val bloodGroup = snapshot.child("bloodGroup").getValue(String::class.java) ?: ""
                
                binding.tvHelloUser.text = "Hello, $fullName!"
                binding.tvUserBloodGroup.text = "Blood Group: $bloodGroup"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Load Counts (Example structure: Donation_History/userId and Requests/userId)
        database.getReference("Donation_History").orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalUnits = 0
                    for (child in snapshot.children) {
                        totalUnits += child.child("units").getValue(Int::class.java) ?: 0
                    }
                    binding.tvDonationCount.text = totalUnits.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        database.getReference("Requests").orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var activeCount = 0
                    for (child in snapshot.children) {
                        // Count all requests for this user, or you can filter by "Pending" if preferred
                        activeCount++
                    }
                    binding.tvRequestCount.text = activeCount.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
