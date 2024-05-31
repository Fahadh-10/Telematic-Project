package com.example.kttelematic.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.kttelematic.Application
import com.example.kttelematic.R
import com.example.kttelematic.dao.UserDao
import com.example.kttelematic.databinding.ActivityLoginBinding
import com.example.kttelematic.helpers.Extensions.showToast
import com.example.kttelematic.helpers.NavKey
import com.example.kttelematic.managers.DataManager
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var flowFrom : String ?= null
    private var deviceId = ""

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        signUpServiceCall()
        readBundles()
        setupListeners()
    }

    /**
     * Reads and extracts data from the intent extras.
     * This function checks if the intent contains a specific key (NavKey.FLOW_FROM).
     * If the key is found, it reads the associated value and assigns it to the [flowFrom] variable.
     * @see NavKey
     */
    private fun readBundles() {
        if (intent.hasExtra(NavKey.FLOW_FROM)) {
            flowFrom = intent.getStringExtra(NavKey.FLOW_FROM)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (flowFrom == null) {
            finishAffinity()
        } else {
            finish()
        }

    }

    /**
     * Sets up listeners for UI elements such as buttons.
     * Handles the click events for sign-in and sign-up actions.
     */
    private fun setupListeners() {
        binding.signInACB.setOnClickListener {
            if (validateFields() != null) {
                Toast.makeText(this@LoginActivity, validateFields(), Toast.LENGTH_SHORT).show()
            } else {
                if (isUserAdded()) {
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                } else {
                    this@LoginActivity.showToast(getString(R.string.user_name_is_invalid))
                }
            }
        }

        binding.signupTV.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }
    }

    /**
     * Validates the input fields for user name and password.
     * @return A string containing an error message if validation fails, otherwise null.
     */
    private fun validateFields(): String? {
        return when {
            binding.userNameET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_user_name)

            binding.passwordET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_passwprd)

            else -> null
        }
    }

    /**
     * Checks if the user exists in the database by validating the credentials.
     * @return True if the user exists, false otherwise.
     */
    private fun isUserAdded(): Boolean {
        val userList = UserDao.fetchUser()
        val currentUser =
            ArrayList(userList.filter { it.email == binding.userNameET.text.toString() && it.password == binding.passwordET.text.toString() })
       if (currentUser.isNotEmpty()) {
           PrefManager.setUserData(this@LoginActivity , currentUser.firstOrNull())
           val user = PrefManager.getUserData(this@LoginActivity) ?: User()
           user.isLogOut = false
           UserDao.saveOrUpdateUser(user)
           Log.e("STATUS" , UserDao.fetchUserBasedEmail(user.email ?: "").toString())
       }
        return currentUser.isNotEmpty()
    }


    private fun signUpServiceCall() {
        val params = HashMap<String, Any>()
        params["deviceId"] = deviceId
        DataManager.signUpServiceCall(params,
            this.application as Application, this,
            object : DataManager.ApiCallback {
                override fun onSuccess(response: String) {
                    Toast.makeText(this@LoginActivity, "Success", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(message: String) {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}