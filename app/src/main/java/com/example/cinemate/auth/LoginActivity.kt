package com.example.cinemate.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemate.ui.MainActivity
import com.example.cinemate.api.RetrofitInstance
import com.example.cinemate.databinding.ActivityLoginBinding
import com.example.cinemate.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefManager: PrefManager // For storing login status and user data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this)

        // Check if already logged in
        if (prefManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setupLoginButton()
        setupRegisterLink()
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim() // Get email from input
            val password = binding.etPassword.text.toString().trim() // Get password from input

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show the ProgressBar and dimming overlay
            binding.progressBar.visibility = View.VISIBLE
            binding.dimOverlay.visibility = View.VISIBLE

            // Call API to get all users
            RetrofitInstance.api.getUsers().enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    // Hide the ProgressBar and dimming overlay
                    binding.progressBar.visibility = View.GONE
                    binding.dimOverlay.visibility = View.GONE

                    if (response.isSuccessful) {
                        val users = response.body()
                        // Find user matching the email
                        val user = users?.find { it.email == email }

                        if (user != null) {
                            // Check if user has a newPassword
                            val hasNewPassword = user.newPassword != null && user.newPassword.isNotEmpty()

                            // If user has a newPassword, use it for login
                            if (hasNewPassword) {
                                if (password == user.newPassword) {
                                    // Save user data to SharedPreferences
                                    saveUserData(user)
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Email or password is incorrect", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // If no newPassword, use the existing password
                                if (password == user.password) {
                                    // Save user data to SharedPreferences
                                    saveUserData(user)
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Email or password is incorrect", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Email or password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    // Hide the ProgressBar and dimming overlay
                    binding.progressBar.visibility = View.GONE
                    binding.dimOverlay.visibility = View.GONE

                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveUserData(user: User) {
        prefManager.apply {
            setLoggedIn(true)
            saveEmail(user.email) // Save user email
            saveUsername(user.username) // Save user username
            user.dob?.let { saveDob(it) } // Save user DOB
            savePassword(user.newPassword ?: user.password) // Save password
            user._id?.let { saveUserId(it) } // Save user ID
        }
    }

    private fun setupRegisterLink() {
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
