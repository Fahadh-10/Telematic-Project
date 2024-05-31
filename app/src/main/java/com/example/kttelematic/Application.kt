package com.example.kttelematic

import android.app.Application
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class Application : Application() {

    private val defaultTag = "default"
    private var reqQueue: RequestQueue? = null

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }

    fun <T> addToRequestQueue(req: Request<T>, tag: String? = null) {
        val socketTimeout = 30000 // 30 seconds. You can change it
        val policy = DefaultRetryPolicy(
            socketTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        req.retryPolicy = policy
        req.tag = if (tag.isNullOrEmpty()) defaultTag else tag
        getReqQueue().add(req)
    }

    private fun getReqQueue(): RequestQueue {
        if (reqQueue == null) {
            reqQueue = Volley.newRequestQueue(this.applicationContext)
        }
        return reqQueue!!
    }

    companion object {
        lateinit var appInstance: Application
    }
}