package com.example.myapplication.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper

/**
 * Profile Fragment
 */
class ProfileFragment : Fragment() {

    // View components
    private lateinit var avatarImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var themeCard: CardView
    private lateinit var themeValueTextView: TextView
    private lateinit var languageCard: CardView
    private lateinit var languageValueTextView: TextView
    private lateinit var logoutButton: Button
    
    // Database helper
    private lateinit var dbHelper: DatabaseHelper
    
    // Current user ID
    private var currentUserId = 1 // Default user ID, should be obtained from login state in real app

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 从arguments中获取用户ID
        arguments?.let {
            currentUserId = it.getInt("USER_ID", 1)
        }
        
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize database helper
        dbHelper = DatabaseHelper(requireContext())
        
        // Initialize views
        initViews(view)
        
        // Load user data
        loadUserData()
        
        // Setup listeners
        setupListeners()
    }
    
    /**
     * Initialize view components
     */
    private fun initViews(view: View) {
        avatarImageView = view.findViewById(R.id.iv_avatar)
        usernameTextView = view.findViewById(R.id.tv_username)
        emailTextView = view.findViewById(R.id.tv_email)
        bioTextView = view.findViewById(R.id.tv_bio)
        editProfileButton = view.findViewById(R.id.btn_edit_profile)
        themeCard = view.findViewById(R.id.card_theme)
        themeValueTextView = view.findViewById(R.id.tv_theme_value)
        languageCard = view.findViewById(R.id.card_language)
        languageValueTextView = view.findViewById(R.id.tv_language_value)
        logoutButton = view.findViewById(R.id.btn_logout)
    }
    
    /**
     * Load user data
     */
    private fun loadUserData() {
        // Load user data from SharedPreferences or database
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("user_email", "user@example.com") ?: "user@example.com"
        
        // Set email
        emailTextView.text = email
        
        // Other data uses default values for now
    }
    
    /**
     * Setup listeners
     */
    private fun setupListeners() {
        // Edit profile button
        editProfileButton.setOnClickListener {
            Toast.makeText(context, "Edit profile feature not implemented yet", Toast.LENGTH_SHORT).show()
        }
        
        // Theme settings
        themeCard.setOnClickListener {
            val themes = arrayOf("Light", "Dark", "System Default")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Theme")
                .setItems(themes) { _, which ->
                    themeValueTextView.text = themes[which]
                    Toast.makeText(context, "Selected ${themes[which]} theme", Toast.LENGTH_SHORT).show()
                    
                    // Save settings
                    val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("theme", themes[which]).apply()
                }
                .show()
        }
        
        // Language settings
        languageCard.setOnClickListener {
            val languages = arrayOf("Chinese", "English")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Language")
                .setItems(languages) { _, which ->
                    languageValueTextView.text = languages[which]
                    Toast.makeText(context, "Selected ${languages[which]}", Toast.LENGTH_SHORT).show()
                    
                    // Save settings
                    val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("language", languages[which]).apply()
                }
                .show()
        }
        
        // Logout
        logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Confirm") { _, _ ->
                    // Clear login state
                    val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
                    
                    // Return to login page
                    try {
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        requireActivity().finish()
                    } catch (e: Exception) {
                        // Show toast if LoginActivity not found
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
} 