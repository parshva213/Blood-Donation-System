package com.example.bd_system

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.bd_system.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        
        // Apply saved theme
        val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for first time launch to show theme selection
        if (sharedPreferences.getBoolean("is_first_time", true)) {
            showThemeSelectionDialog()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("System Default", "Light Mode", "Dark Mode")
        val themeModes = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        )

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setItems(themes) { _, which ->
                val selectedMode = themeModes[which]
                sharedPreferences.edit().apply {
                    putInt("theme_mode", selectedMode)
                    putBoolean("is_first_time", false)
                    apply()
                }
                AppCompatDelegate.setDefaultNightMode(selectedMode)
                navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }
}
