package com.example.cinemate.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.cinemate.R
import com.example.cinemate.auth.LoginActivity
import com.example.cinemate.auth.PrefManager
import com.example.cinemate.databinding.FragmentProfileBinding
import com.example.cinemate.data.model.User
import com.example.cinemate.api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefManager: PrefManager
    private var originalUsername: String? = null
    private var originalEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi PrefManager
        prefManager = PrefManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Tampilkan data pengguna
        displayUserData()

        // Setup tombol edit
        setupEditButtons()

        // Setup tombol logout
        setupLogoutButton()

        // Setup tombol hapus akun
        setupDeleteAccountButton()

        // Setup tombol ganti password
        setupChangePasswordButton()

        // Setup tombol ganti bahasa
        changeLangugage()

        return binding.root
    }

    private fun displayUserData() {
        val email = prefManager.getEmail()
        val username = prefManager.getUsername()

        // Tampilkan data di UI
        binding.tvFullName.text = username ?: "N/A"
        binding.etUsername.setText(username ?: "N/A")
        binding.etEmail.setText(email ?: "N/A")

        // Simpan nilai asli untuk reset
        originalUsername = username
        originalEmail = email
    }

    private fun setupEditButtons() {
        // Edit username
        binding.ivEditUsername.setOnClickListener {
            toggleEditText(binding.etUsername, binding.ivEditUsername)
        }

        // Edit email
        binding.ivEditEmail.setOnClickListener {
            toggleEditText(binding.etEmail, binding.ivEditEmail)
        }
    }

    private fun toggleEditText(editText: EditText, icon: ImageView) {
        val isEnabled = !editText.isEnabled
        editText.isEnabled = isEnabled
        if (isEnabled) {
            editText.requestFocus()
            icon.setImageResource(R.drawable.icon_save)
        } else {
            icon.setImageResource(R.drawable.icon_edit)
            val field = if (editText.id == R.id.et_username) "username" else "email"
            showSaveConfirmationDialog(editText.text.toString(), field)
        }
    }

    private fun setupChangePasswordButton() {
        binding.llChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showChangePasswordDialog() {
        // Inflate dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etOldPassword = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.et_confirm_password)

        dialogView.findViewById<Button>(R.id.btn_change_password).setOnClickListener {
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Validasi input apakah kosong
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon isi semua informasi password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi apakah password lama benar
            val savedPassword = prefManager.getPassword()
            if (savedPassword != oldPassword) {
                Toast.makeText(requireContext(), "Password lama salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi password baru
            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Password baru tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            changePassword(oldPassword, newPassword)
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        val userId = prefManager.getUserId() ?: return

        // Panggil API untuk mengubah password pengguna
        RetrofitInstance.api.changePassword(userId, oldPassword, newPassword).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Update password di PrefManager
                    prefManager.savePassword(newPassword)
                    Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API Error", response.errorBody()?.string() ?: "Unknown error")
                    Toast.makeText(requireContext(), "Gagal mengubah password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun changeLangugage() {
        binding.llLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "This feature will be updated soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSaveConfirmationDialog(value: String, field: String) {
        // Inflate dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_update_account, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set dialog message
        dialogView.findViewById<TextView>(R.id.tv_confirmation_message).text = "Apakah Anda ingin menyimpan perubahan pada $field?"

        // Set button actions
        dialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            // Cek apakah ada perubahan
            if ((field == "username" && value == originalUsername) || (field == "email" && value == originalEmail)) {
                Toast.makeText(requireContext(), "Tidak ada perubahan untuk disimpan", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            // Simpan perubahan
            if (field == "username") {
                prefManager.saveUsername(value)
            } else if (field == "email") {
                prefManager.saveEmail(value)
            }
            updateUserData(value, field)
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            if (field == "username") {
                binding.etUsername.setText(originalUsername)
            } else if (field == "email") {
                binding.etEmail.setText(originalEmail)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUserData(value: String, field: String) {
        val userId = prefManager.getUserId() ?: return

        // Ambil dob dan password dari SharedPreferences
        val dob = prefManager.getDob()
        val password = prefManager.getPassword()

        if (dob == null || password == null) {
            Toast.makeText(requireContext(), "DOB atau Password tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val userUpdate = User(
            _id = userId,
            username = if (field == "username") value else binding.etUsername.text.toString(),
            email = if (field == "email") value else binding.etEmail.text.toString(),
            dob = dob,
            password = password
        )

        // Tampilkan ProgressBar
        binding.progressBar.visibility = View.VISIBLE
        // Jika progress bar tampil, atur tampila agar redup
        if (binding.progressBar.visibility == View.VISIBLE) {
            binding.root.children.forEach {
                if (it.id != binding.progressBar.id) {
                    it.alpha = 0.5f
                }
            }
        }

        // Panggil API untuk memperbarui data pengguna
        RetrofitInstance.api.updateUser(userId, userUpdate).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                // Sembunyikan ProgressBar dan kembalikan tampilan ke semula
                binding.progressBar.visibility = View.GONE
                binding.root.children.forEach {
                    it.alpha = 1f
                }

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Berhasil memperbarui data!", Toast.LENGTH_SHORT).show()
                    if (field == "username") {
                        binding.tvFullName.text = value
                    }
                } else {
                    Log.e("API Error", response.errorBody()?.string() ?: "Unknown error")
                    Toast.makeText(requireContext(), "Failed to update data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Sembunyikan ProgressBar
                binding.progressBar.visibility = View.GONE;

                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupLogoutButton() {
        binding.llLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_logout, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            prefManager.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        dialog.show()
    }

    private fun setupDeleteAccountButton() {
        binding.llDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }
    }

    private fun showDeleteAccountConfirmationDialog() {
        // Inflate dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete_account, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set button actions
        dialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            deleteUserAccount()
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteUserAccount() {
        val userId = prefManager.getUserId() ?: return

        // Panggil API untuk menghapus akun pengguna
        RetrofitInstance.api.deleteUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                    prefManager.logout() // Logout setelah penghapusan
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                } else {
                    Log.e("API Error", response.errorBody()?.string() ?: "Unknown error")
                    Toast.makeText(requireContext(), "Gagal menghapus akun", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
