package com.example.kttelematic.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.kttelematic.R
import com.example.kttelematic.dao.UserDao
import com.example.kttelematic.databinding.ActivityProfileBinding
import com.example.kttelematic.helpers.ConstantValues.PROFILE_RESULT_CODE
import com.example.kttelematic.helpers.Utils
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.User
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupData()
        setupListeners()
    }

    private fun setupData() {
        binding.firstNameTV.text = PrefManager.getUserData(this@ProfileActivity)?.firstName ?: ""
        binding.latNameTV.text = PrefManager.getUserData(this@ProfileActivity)?.lastName ?: ""
        binding.userMailTV.text = PrefManager.getUserData(this@ProfileActivity)?.email ?: ""
        binding.userPhoneNumberTV.text =
            PrefManager.getUserData(this@ProfileActivity)?.phoneNumber ?: ""
    }

    private fun setupListeners() {
        binding.backIV.setOnClickListener {
            finish()
        }


        binding.logoutACB.setOnClickListener {
            showLogoutAlertDialog()
        }
    }

    /**
     * Show an alert dialog with a specified title and message.
     *
     * This function displays an alert dialog with the provided title and message. It includes a single "Ok" button
     * that dismisses the dialog when clicked.
     *
     * @param context The [Activity] context in which to display the alert dialog.
     * @param title The title of the alert dialog.
     * @param message The message or content of the alert dialog.
     */
    private fun showLogoutAlertDialog() {
        val builder = AlertDialog.Builder(this@ProfileActivity)
        builder.setTitle(getString(R.string.logout))
        builder.setMessage(getString(R.string.are_you_sure_logout))
        builder.setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
            updateUser()
        }
        builder.setNegativeButton("Cancel" , null)
        builder.show()
    }

    private fun updateUser() {
        val user = UserDao.fetchUserBasedEmail(PrefManager.getUserData(this@ProfileActivity)?.email ?: "") ?: User()
        user.isLogOut = true
        UserDao.saveOrUpdateUser(user)
        val userList = UserDao.fetchUser()
        if (user.id?.isNotEmpty() == true) {
            if (UserDao.userLoggedList().isNotEmpty()) {
                PrefManager.setUserData(this@ProfileActivity, UserDao.userLoggedList().firstOrNull())
                val intent = Intent()
                setResult(PROFILE_RESULT_CODE, intent)
                finish()

            } else {
                PrefManager.setUserData(this@ProfileActivity, null)
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            }
        }
        Log.e("USER", userList.toString())
    }

}