package com.example.cinemate.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
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
    private lateinit var prefManager: PrefManager // Untuk menyimpan status login dan data pengguna

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this)

        // Cek jika sudah login
        if (prefManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setupLoginButton()
        setupRegisterLink()
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim() // Ambil email dari input
            val password = binding.etPassword.text.toString().trim() // Ambil password dari input

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Panggil API untuk mendapatkan semua pengguna
            RetrofitInstance.api.getUsers().enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful) {
                        val users = response.body()
                        // Mencari pengguna yang cocok dengan email
                        val user = users?.find { it.email == email }

                        if (user != null) {
                            // Cek apakah user memiliki newPassword
                            val hasNewPassword = user.newPassword != null && user.newPassword.isNotEmpty()

                            // Jika user memiliki newPassword, gunakan newPassword untuk login
                            if (hasNewPassword) {
                                if (password == user.newPassword) {
                                    // Simpan data pengguna ke SharedPreferences
                                    prefManager.apply {
                                        setLoggedIn(true)
                                        saveEmail(user.email) // Simpan email pengguna
                                        saveUsername(user.username) // Simpan username pengguna
                                        user.dob?.let { it1 -> saveDob(it1) } // Simpan DOB pengguna
                                        savePassword(user.newPassword) // Simpan newPassword
                                        user._id?.let { it1 -> saveUserId(it1) } // Simpan ID pengguna
                                    }

                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Email atau password salah", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Jika tidak ada newPassword, gunakan password yang ada
                                if (password == user.password) {
                                    // Simpan data pengguna ke SharedPreferences
                                    prefManager.apply {
                                        setLoggedIn(true)
                                        saveEmail(user.email) // Simpan email pengguna
                                        saveUsername(user.username) // Simpan username pengguna
                                        user.dob?.let { it1 -> saveDob(it1) } // Simpan DOB pengguna
                                        savePassword(user.password) // Simpan password
                                        user._id?.let { it1 -> saveUserId(it1) } // Simpan ID pengguna
                                    }

                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    Toast.makeText(this@LoginActivity, "Berhasil login!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Email atau password salah", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Email atau password salah", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupRegisterLink() {
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
