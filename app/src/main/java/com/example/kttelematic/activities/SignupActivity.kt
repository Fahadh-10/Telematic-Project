package com.example.kttelematic.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kttelematic.R
import com.example.kttelematic.dao.UserDao
import com.example.kttelematic.databinding.ActivitySignupBinding
import com.example.kttelematic.helpers.Utils
import com.example.kttelematic.models.User
import java.util.UUID

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    /**
     * Sets up listeners for UI elements in the signup activity.
     *
     * This function attaches click listeners to the back icon and the signup button.
     * Clicking the back icon finishes the activity and navigates back to the previous screen.
     * Clicking the signup button validates the input fields for user registration.
     * If validation fails, an error message is displayed using a toast.
     * If validation succeeds, the addUser() function is called to add the user to the database.
     */
    private fun setupListeners() {

        binding.backIconIV.setOnClickListener {
            finish()
        }

        binding.signupACB.setOnClickListener {
            if (validateFields() != null) {
                Toast.makeText(this@SignupActivity, validateFields(), Toast.LENGTH_SHORT).show()
            } else {
                val exitingUser = UserDao.fetchUserBasedEmail(binding.userMailET.text.toString())
                if (exitingUser != null) {
                    Utils.showAlert(
                        this@SignupActivity,
                        getString(R.string.existing_user),
                        getString(R.string.already_registered)
                    )
                } else {
                    addUser()
                }
            }
        }
    }

    /**
     * Validates the input fields for user registration.
     *
     * This function checks if the required fields for user registration are filled with valid data.
     * It validates the first name, last name, email, phone number, and password fields.
     * If any field is empty or contains invalid data, an error message corresponding to that field is returned.
     * If all fields are valid, null is returned, indicating successful validation.
     *
     * @return A String containing an error message if any field is invalid, or null if all fields are valid.
     */
    private fun validateFields(): String? {
        return when {
            binding.firstNameET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_first_name)

            binding.latNameET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_last_name)

            binding.userMailET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_email)

            binding.userPhoneNumberET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_phome_number)

            binding.userPhoneNumberET.text.toString().trim()
                .length < 10 -> getString(R.string.enter_your_valid_phone_number)

            binding.passwordET.text.toString().trim()
                .isEmpty() -> getString(R.string.enter_your_passwprd)

            !Utils.isEmailValid(binding.userMailET.text.toString()) ->
                getString(R.string.enter_valid_email)

            !Utils.isPasswordValid(binding.passwordET.text.toString()) ->
                getString(R.string.enter_valid_password)

            else -> null
        }
    }

    /**
     * Adds a new user to the database.
     *
     * This function creates a new User object and populates it with the data entered by the user
     * in the registration form. It then saves or updates the user details in the database using UserDao.
     * After adding the user, it fetches the updated user list from the database and logs the result.
     */
    private fun addUser() {
        val user = User()
        user.id = UUID.randomUUID().toString()
        user.firstName = binding.firstNameET.text.toString()
        user.lastName = binding.latNameET.text.toString()
        user.email = binding.userMailET.text.toString()
        user.phoneNumber = binding.userPhoneNumberET.text.toString()
        user.password = binding.passwordET.text.toString()
        user.phoneNUmberCC = 91
        UserDao.saveOrUpdateUser(user)
        val userList = UserDao.fetchUser()
        if (user.id?.isNotEmpty() == true) {
            showSuccessAlertDialog()
        }
        Log.e("USER", userList.toString())
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
    private fun showSuccessAlertDialog() {
        val builder = AlertDialog.Builder(this@SignupActivity)
        builder.setTitle(getString(R.string.registered))
        builder.setMessage(getString(R.string.account_registered_successfully))
        builder.setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        builder.show()
    }
}