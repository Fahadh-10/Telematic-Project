package com.example.kttelematic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kttelematic.databinding.LoggedUserItemBinding
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.User

class LoggedUserAdapter(var loggedUserList: ArrayList<User>) :
    RecyclerView.Adapter<LoggedUserAdapter.LoggedUserVH>() {

    private var onItemClickListener: OnItemClickListener? = null
    lateinit var context: Context
    private var isFirstTime = false

    interface OnItemClickListener {
        fun onItemClick(position: Int, user: User)
    }

    fun setOnItemClickListeners(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    class LoggedUserVH(mBinding: LoggedUserItemBinding) : RecyclerView.ViewHolder(mBinding.root) {
        val binding: LoggedUserItemBinding = mBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggedUserVH {
        context = parent.context
        return LoggedUserVH(
            LoggedUserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return loggedUserList.size
    }

    override fun onBindViewHolder(holder: LoggedUserVH, position: Int) {
        val user = loggedUserList[position]
        holder.binding.userNameTV.text = user.firstName.plus(" ").plus(user.lastName)
        holder.binding.profileIV.visibility =
            if (user.isSelected == true && isFirstTime) VISIBLE else GONE
        holder.binding.profileIV.visibility =
            if (user.id == PrefManager.getUserData(context)?.id && !isFirstTime) VISIBLE else GONE


        holder.binding.parentView.setOnClickListener {
            isFirstTime = true
            onItemClickListener?.onItemClick(position, user)
        }
    }

}