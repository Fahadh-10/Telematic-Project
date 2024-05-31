package com.example.kttelematic.helpers

import android.content.Context
import android.widget.Toast

object Extensions {

    fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}