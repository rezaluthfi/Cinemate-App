package com.example.cinemate.auth

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemate.api.RetrofitInstance
import com.example.cinemate.data.model.User
import com.example.cinemate.databinding.ActivityRegisterBinding
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val apiService = RetrofitInstance.api // Ensure this matches your API instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRegisterButton()
        setupLoginLink()
        setupDateOfBirthPicker()
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val username = binding.etUsername.text.toString().trim() // Get username from input
            val dob = binding.etDob.text.toString().trim() // Get date of birth from input
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Validate input
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(dob) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show the ProgressBar and dimming overlay
            binding.progressBar.visibility = View.VISIBLE
            binding.dimOverlay.visibility = View.VISIBLE

            // Generate random ID
            val generatedId = generateRandomId()

            // Create User object
            val newUser = User(
                _id = generatedId,
                email = email,
                username = username,
                dob = dob,
                password = password // Ensure not to store password directly
            )

            // Call API for registration
            apiService.registerUser(newUser).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    // Hide the ProgressBar and dimming overlay
                    binding.progressBar.visibility = View.GONE
                    binding.dimOverlay.visibility = View.GONE

                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                        finish() // Go back to login screen
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    // Hide the ProgressBar and dimming overlay
                    binding.progressBar.visibility = View.GONE
                    binding.dimOverlay.visibility = View.GONE

                    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupLoginLink() {
        binding.tvSignIn.setOnClickListener {
            finish() // Go back to login screen
        }
    }

    private fun setupDateOfBirthPicker() {
        binding.etDob.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                binding.etDob.setText(sdf.format(date))
            }
        }
    }

    // Function to generate a random hexadecimal ID of 24 characters
    private fun generateRandomId(length: Int = 24): String {
        val chars = "0123456789abcdef"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
