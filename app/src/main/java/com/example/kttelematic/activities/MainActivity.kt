package com.example.kttelematic.activities

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kttelematic.R
import com.example.kttelematic.databinding.ActivityMainBinding
import com.example.kttelematic.helpers.ConstantValues.MAP_KEY
import com.example.kttelematic.managers.PrefManager
import com.google.android.libraries.places.api.Places
import io.realm.Realm
import io.realm.RealmConfiguration

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build())
        Places.initialize(this, MAP_KEY)
        binding.lottieAnimationView.playAnimation()
        setupListeners()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    /**
     * Sets up listeners for UI elements such as buttons.
     * Handles the click events for sign-in and sign-up actions.
     */
    private fun setupListeners() {
        binding.lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                if (PrefManager.getUserData(this@MainActivity) == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onAnimationCancel(p0: Animator) = Unit

            override fun onAnimationRepeat(p0: Animator) {}

        })
    }
}