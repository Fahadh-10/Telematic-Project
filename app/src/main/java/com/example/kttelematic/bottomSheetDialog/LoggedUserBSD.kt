package com.example.kttelematic.bottomSheetDialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kttelematic.activities.LoginActivity
import com.example.kttelematic.adapter.LoggedUserAdapter
import com.example.kttelematic.dao.UserDao
import com.example.kttelematic.databinding.LoggedUserBsdBinding
import com.example.kttelematic.helpers.FlowFrom
import com.example.kttelematic.helpers.NavKey
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.User
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoggedUserBSD(): BottomSheetDialogFragment() {

    private lateinit var binding : LoggedUserBsdBinding
    private lateinit var loggedUserADTR : LoggedUserAdapter
    lateinit var mContext : Context
    var callbacks : Callback ?= null

    interface Callback {
        fun onUserSelected()
    }

    fun setOnCallbacksListeners(callback: Callback) {
        this.callbacks = callback
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoggedUserBsdBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        loggedUserADTR = LoggedUserAdapter(ArrayList())
        setAdapter()
        setupListeners()
    }

    private fun setupListeners() {
        binding.addAccountACB.setOnClickListener {
            dismiss()
            val intent = Intent(mContext , LoginActivity::class.java)
            intent.putExtra(NavKey.FLOW_FROM , FlowFrom.DASHBOARD.name)
            startActivity(intent)
        }
    }

    private fun setAdapter() {
        val userList = UserDao.userLoggedList()
        if (userList.isNotEmpty()) {
            binding.userRV.visibility = VISIBLE
            binding.userRV.layoutManager = LinearLayoutManager(mContext , LinearLayoutManager.VERTICAL , false)
            loggedUserADTR = LoggedUserAdapter(UserDao.userLoggedList())
            binding.userRV.adapter = loggedUserADTR
        } else {
            binding.userRV.visibility = GONE
        }

        loggedUserADTR.setOnItemClickListeners(object : LoggedUserAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, user: User) {
                if (userList[position].isSelected == true) {
                    userList[position].isSelected = false
                    loggedUserADTR.loggedUserList =userList
                    loggedUserADTR.notifyDataSetChanged()
                } else {
                    loggedUserADTR.loggedUserList.forEach { it.isSelected = false }
                    userList[position].isSelected = true
                    loggedUserADTR.loggedUserList = userList
                    loggedUserADTR.notifyDataSetChanged()
                    PrefManager.setUserData(mContext , user)
                    callbacks?.onUserSelected()
                    dismiss()
                }

            }

        })

    }
}