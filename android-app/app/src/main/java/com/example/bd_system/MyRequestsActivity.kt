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
import com.example.bd_system.databinding.ActivityMyRequestsBinding
import com.example.bd_system.databinding.DialogAddRequestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BloodRequest(
    val id: String = "",
    val userId: String = "",
    val patientName: String = "",
    val bloodGroup: String = "",
    val units: Int = 0,
    val hospital: String = "",
    val date: String = "",
    val contactNumber: String = "",
    val altContactNumber: String = "",
    val urgencyLevel: String = "Normal",
    val urgencyDetail: String = "",
    val status: String = "Pending",
    val isDeleted: Int = 0
)

class MyRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyRequestsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: RequestAdapter
    private val requestList = mutableListOf<BloodRequest>()
    private val DB_URL = "https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(DB_URL)

        setupSidebar()
        setupRecyclerView()
        loadMyRequests()

        binding.btnAddRequest.setOnClickListener {
            showAddRequestDialog()
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
        binding.rvMyRequests.layoutManager = LinearLayoutManager(this)
        adapter = RequestAdapter(requestList)
        binding.rvMyRequests.adapter = adapter
    }

    private fun loadMyRequests() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("Requests").orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestList.clear()
                    for (child in snapshot.children) {
                        val request = child.getValue(BloodRequest::class.java)
                        if (request != null && request.isDeleted == 0) {
                            requestList.add(request)
                        }
                    }
                    requestList.reverse() // Newest first
                    
                    if (requestList.isEmpty()) {
                        binding.tvEmptyRequests.visibility = View.VISIBLE
                        binding.rvMyRequests.visibility = View.GONE
                    } else {
                        binding.tvEmptyRequests.visibility = View.GONE
                        binding.rvMyRequests.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyRequestsActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showAddRequestDialog() {
        val dialogBinding = DialogAddRequestBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        val userId = auth.currentUser?.uid
        var userProfileName = ""
        var userProfileBloodGroup = ""
        var userProfilePhone = ""

        if (userId != null) {
            database.getReference("Users").child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userProfileName = snapshot.child("fullName").getValue(String::class.java) ?: ""
                    userProfileBloodGroup = snapshot.child("bloodGroup").getValue(String::class.java) ?: ""
                    userProfilePhone = snapshot.child("phone").getValue(String::class.java) ?: ""
                }
            }
        }

        dialogBinding.cbForMyself.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dialogBinding.tilPatientName.visibility = View.GONE
                dialogBinding.tilBloodGroup.visibility = View.GONE
                dialogBinding.tilContactNumber.visibility = View.GONE
            } else {
                dialogBinding.tilPatientName.visibility = View.VISIBLE
                dialogBinding.tilBloodGroup.visibility = View.VISIBLE
                dialogBinding.tilContactNumber.visibility = View.VISIBLE
            }
        }

        dialogBinding.rgUrgency.setOnCheckedChangeListener { _, checkedId ->
            // Show all detail fields once any urgency is selected
            dialogBinding.llRequestDetails.visibility = View.VISIBLE
            
            when (checkedId) {
                R.id.rbCritical, R.id.rbUrgent -> {
                    dialogBinding.cbForMyself.visibility = View.GONE
                    dialogBinding.cbForMyself.isChecked = false
                    dialogBinding.tilDays.visibility = View.GONE
                    dialogBinding.tilScheduledDate.visibility = View.GONE
                    
                    dialogBinding.tilPatientName.visibility = View.VISIBLE
                    dialogBinding.tilBloodGroup.visibility = View.VISIBLE
                    dialogBinding.tilContactNumber.visibility = View.VISIBLE
                }
                R.id.rbNormal -> {
                    dialogBinding.cbForMyself.visibility = View.VISIBLE
                    dialogBinding.tilDays.visibility = View.VISIBLE
                    dialogBinding.tilScheduledDate.visibility = View.GONE
                }
                R.id.rbScheduled -> {
                    dialogBinding.cbForMyself.visibility = View.VISIBLE
                    dialogBinding.tilDays.visibility = View.GONE
                    dialogBinding.tilScheduledDate.visibility = View.VISIBLE
                }
            }
        }

        dialogBinding.btnSubmitRequest.setOnClickListener {
            val isForMyself = dialogBinding.cbForMyself.visibility == View.VISIBLE && dialogBinding.cbForMyself.isChecked
            
            val patientName = if (isForMyself) userProfileName else dialogBinding.etPatientName.text.toString().trim()
            val bloodGroup = if (isForMyself) userProfileBloodGroup else dialogBinding.etBloodGroup.text.toString().trim().uppercase()
            val contactNumber = if (isForMyself) userProfilePhone else dialogBinding.etContactNumber.text.toString().trim()
            
            val unitsStr = dialogBinding.etUnits.text.toString().trim()
            val hospital = dialogBinding.etHospital.text.toString().trim()
            val altContactNumber = dialogBinding.etAltContactNumber.text.toString().trim()

            val selectedUrgencyId = dialogBinding.rgUrgency.checkedRadioButtonId
            if (selectedUrgencyId == -1) {
                Toast.makeText(this, "Please select urgency level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val urgencyLevel = when (selectedUrgencyId) {
                R.id.rbCritical -> "Critical"
                R.id.rbUrgent -> "Urgent"
                R.id.rbNormal -> "Normal"
                R.id.rbScheduled -> "Scheduled"
                else -> "Normal"
            }

            val urgencyDetail = when (selectedUrgencyId) {
                R.id.rbNormal -> dialogBinding.etDays.text.toString().trim()
                R.id.rbScheduled -> dialogBinding.etScheduledDate.text.toString().trim()
                else -> ""
            }

            if (patientName.isEmpty() || bloodGroup.isEmpty() || unitsStr.isEmpty() || hospital.isEmpty() || contactNumber.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (urgencyLevel == "Normal" && urgencyDetail.isEmpty()) {
                Toast.makeText(this, "Please specify the number of days", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (urgencyLevel == "Scheduled" && urgencyDetail.isEmpty()) {
                Toast.makeText(this, "Please specify the scheduled date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val units = unitsStr.toInt()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val requestRef = database.getReference("Requests").push()
                val request = BloodRequest(
                    id = requestRef.key ?: "",
                    userId = userId,
                    patientName = patientName,
                    bloodGroup = bloodGroup,
                    units = units,
                    hospital = hospital,
                    date = date,
                    contactNumber = contactNumber,
                    altContactNumber = altContactNumber,
                    urgencyLevel = urgencyLevel,
                    urgencyDetail = urgencyDetail,
                    status = "Pending",
                    isDeleted = 0
                )
                
                requestRef.setValue(request).addOnSuccessListener {
                    Toast.makeText(this, "Request submitted successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
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
                R.id.nav_donate -> startActivity(Intent(this, DonationHistoryActivity::class.java))
                R.id.nav_request -> { /* Already here */ }
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

    private class RequestAdapter(private val list: List<BloodRequest>) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val request = list[position]
            holder.tvBloodGroup.text = request.bloodGroup
            
            val urgencyDisplay = when(request.urgencyLevel) {
                "Critical" -> "🔴 Critical"
                "Urgent" -> "🟠 Urgent"
                "Normal" -> "🟡 Normal (${request.urgencyDetail} days)"
                "Scheduled" -> "🟢 Scheduled (${request.urgencyDetail})"
                else -> request.urgencyLevel
            }
            
            holder.tvUnits.text = "Patient: ${request.patientName} | $urgencyDisplay"
            holder.tvHospital.text = request.hospital
            holder.tvDate.text = request.date
            holder.tvStatus.text = request.status
            
            if (request.status == "Approved") {
                holder.tvStatus.setBackgroundResource(android.R.color.holo_green_light)
            } else if (request.status == "Rejected") {
                holder.tvStatus.setBackgroundResource(android.R.color.holo_red_light)
            } else {
                holder.tvStatus.setBackgroundResource(android.R.color.holo_orange_light)
            }
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvBloodGroup: TextView = view.findViewById(R.id.tvBloodGroup)
            val tvUnits: TextView = view.findViewById(R.id.tvUnits)
            val tvHospital: TextView = view.findViewById(R.id.tvHospital)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }
    }
}
