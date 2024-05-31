package com.example.kttelematic.managers

import android.app.Activity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.kttelematic.Application
import com.example.kttelematic.models.BaseModel
import com.google.gson.Gson
import org.json.JSONObject

object DataManager {

    private fun request(
        method: Int,
        url: String,
        params: Map<String, Any>?,
        application: Application,
        activity: Activity?,
        callback: ApiCallback?
    ) {
        val BASE_URL = "http://51.20.116.228:3007"
        val jsonObjRequest = object : JsonObjectRequest(
            method, BASE_URL + url, if (params != null) JSONObject(params) else null,
            Response.Listener { response ->
                Log.d("", "Response: $response")
                Log.d("", "--------------------------------------------------")
                if (response.toString().isEmpty()) {
                    callback?.onSuccess(response.toString())
                } else {
                    val apiResponse =
                        Gson().fromJson(response.toString(), BaseModel::class.java)
                    if (apiResponse.status.isNullOrEmpty() || apiResponse.status == "success") {
                        callback?.onSuccess(response.toString())
                    } else {
                        callback?.onFailure(apiResponse.status)
                    }
                }
            },
            Response.ErrorListener { error ->
                if (error.toString().contains("Failed to connect")) {
                    callback?.onFailure("No Internet Connectivity!")
                } else {
                    var dataError: String = ""
                    if (error.networkResponse != null) {
                        dataError = error.networkResponse.data.decodeToString()
                    }
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        if (activity != null) {

                        } else {
                            callback?.onFailure("Try again, unable to connect")
                        }
                    } else {
                        try {
                            val apiResponse = Gson().fromJson(dataError, BaseModel::class.java)
                            callback?.onFailure(apiResponse.status)
                        } catch (e: Exception) {
                            callback?.onFailure(e.message.toString())
                        }
                    }
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["authorization"] = "Bearer " + PrefManager.getAccessToken(application)
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        application.addToRequestQueue(jsonObjRequest)
    }

    fun signUpServiceCall(
        params: Map<String, Any>?,
        application: Application,
        activity: Activity,
        callback: ApiCallback?
    ) {
        request(Request.Method.POST, "/api/auth/genrateToken", params, application, activity, callback)
    }

    interface ApiCallback {
        fun onSuccess(response: String)
        fun onFailure(message: String)

    }
}