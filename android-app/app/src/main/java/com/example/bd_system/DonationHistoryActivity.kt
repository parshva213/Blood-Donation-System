package com.example.bd_system

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bd_system.databinding.ActivityDonationHistoryBinding
import com.example.bd_system.databinding.DialogAddDonationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Donation(
    val id: String = "",
    val donorId: String = "",
    val date: String = "",
    val hospital: String = "",
    val units: Int = 0,
    val notes: String = "",
    val status: String = "Verified",
    val isDeleted: Int = 0
)

class DonationHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: DonationAdapter
    private val donationList = mutableListOf<Donation>()
    private val DB_URL = "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(DB_URL)

        setupSidebar()
        setupRecyclerView()
        loadDonationHistory()

        binding.btnAddDonation.setOnClickListener {
            showAddDonationDialog()
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

    private fun setupRecyclerView() {
        binding.rvDonationHistory.layoutManager = LinearLayoutManager(this)
        adapter = DonationAdapter(donationList)
        binding.rvDonationHistory.adapter = adapter
    }

    private fun loadDonationHistory() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("Donation_History").orderByChild("donorId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    donationList.clear()
                    for (child in snapshot.children) {
                        val donation = child.getValue(Donation::class.java)
                        if (donation != null && donation.isDeleted == 0) {
                            donationList.add(donation)
                        }
                    }
                    donationList.reverse() // Newest first
                    
                    if (donationList.isEmpty()) {
                        binding.tvEmptyHistory.visibility = View.VISIBLE
                        binding.rvDonationHistory.visibility = View.GONE
                    } else {
                        binding.tvEmptyHistory.visibility = View.GONE
                        binding.rvDonationHistory.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DonationHistoryActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showAddDonationDialog() {
        val dialogBinding = DialogAddDonationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSubmitDonation.setOnClickListener {
            val hospital = dialogBinding.etHospital.text.toString().trim()
            val unitsStr = dialogBinding.etUnits.text.toString().trim()
            val notes = dialogBinding.etNotes.text.toString().trim()

            if (hospital.isEmpty() || unitsStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null) {
                val donorId = user.uid
                val units = unitsStr.toInt()
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val historyRef = database.getReference("Donation_History").push()
                
                val donation = Donation(
                    id = historyRef.key ?: "",
                    donorId = donorId,
                    date = date,
                    hospital = hospital,
                    units = units,
                    notes = notes,
                    status = "Verified",
                    isDeleted = 0
                )
                
                historyRef.setValue(donation).addOnSuccessListener {
                    Toast.makeText(this, "Donation added successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to add donation", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
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
                R.id.nav_dashboard -> startActivity(Intent(this, UserDashboardActivity::class.java))
                R.id.nav_donate -> { /* Already here */ }
                R.id.nav_request -> startActivity(Intent(this, MyRequestsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_change_password -> startActivity(Intent(this, ChangePasswordActivity::class.java))
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

    private class DonationAdapter(private val list: List<Donation>) : RecyclerView.Adapter<DonationAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val donation = list[position]
            holder.tvTitle.text = donation.hospital
            holder.tvSubtitle.text = "${donation.units} Units"
            holder.tvDate.text = donation.date
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }
    }
}
