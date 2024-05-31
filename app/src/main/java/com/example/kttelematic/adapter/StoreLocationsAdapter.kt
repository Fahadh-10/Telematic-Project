package com.example.kttelematic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kttelematic.R
import com.example.kttelematic.databinding.StoreLocationItemBinding
import com.example.kttelematic.helpers.Utils
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.Addresses

class StoreLocationsAdapter(var addressList: ArrayList<Addresses>) :
    RecyclerView.Adapter<StoreLocationsAdapter.StoreLocationsVH>() {
    var context: Context? = null
    private var isFirstTime: Boolean = false
    private var onItemClickListeners: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(address: Addresses, position: Int)
    }

    fun setOnItemClickListeners(onItemClickListener: OnItemClickListener) {
        this.onItemClickListeners = onItemClickListener
    }

    class StoreLocationsVH(mBinding: StoreLocationItemBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        val binding: StoreLocationItemBinding = mBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreLocationsVH {
        context = parent.context
        return StoreLocationsVH(
            StoreLocationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: StoreLocationsVH, position: Int) {
        val address = addressList[position]
        holder.binding.locationTV.text =
            context?.getString(R.string.location)?.plus("${position + 1}")
        holder.binding.fullAddressTV.text = address.address
        holder.binding.dateTimeTV.text = Utils.formatDateString(address.createdAt.toString())
        holder.binding.profileIV.visibility =
            if (address.isSelected == true && isFirstTime) View.VISIBLE else View.GONE
        holder.binding.profileIV.visibility =
            if (address.id == context?.let { PrefManager.getUserData(it)?.id } && !isFirstTime) View.VISIBLE else View.GONE

        holder.binding.parentView.setOnClickListener {
            isFirstTime = true
            onItemClickListeners?.onItemClick(address, position)
        }

    }
}