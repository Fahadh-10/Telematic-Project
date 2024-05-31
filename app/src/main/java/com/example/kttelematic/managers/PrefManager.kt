package com.example.kttelematic.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.kttelematic.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object PrefManager {

    private const val USER_DATA = "USER_DATA"
    private const val TOKEN = "TOKEN"

    interface Person {
        val name: String
        val age: Int
    }

    fun getPref(context: Context) : SharedPreferences {
        return context.getSharedPreferences("scanner" , 0)
    }

    fun getAccessToken(context: Context): String {
        getPref(context).getString(TOKEN,"").let {
            return it ?: ""
        }
    }

    fun setAccessToken(context: Context, token: String){
        with(getPref(context).edit()){
            putString(TOKEN, token)
            apply()
        }
    }

    fun setUserData(context: Context, userData: User?) {
        val userDataJson = Gson().toJson(userData)
        with(getPref(context).edit()) {
            putString(USER_DATA, userDataJson)
            apply()
        }
    }

    fun getUserData(context: Context): User? {
        getPref(context).getString(USER_DATA, null)?.let {
            val type: Type = object : TypeToken<User>() {}.type
            return Gson().fromJson(it, type)
        }
        return null
    }
}