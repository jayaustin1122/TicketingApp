package com.example.smartticketing.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartticketing.databinding.ItemViolationsBinding
import com.example.smartticketing.model.ViolationItem

class ViolationAdapter(private val violationList: MutableList<ViolationItem>,
                       private val onViolationRemoved: (Float) -> Unit
    ) : RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {

    inner class ViolationViewHolder(val binding: ItemViolationsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViolationViewHolder {
        val binding = ItemViolationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViolationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        val violation = violationList[position]
        holder.binding.violationCode.setText(violation.code)
        holder.binding.violationName.setText(violation.name)
        holder.binding.violationAmount.setText(violation.amount.toString())
        holder.binding.delete.setOnClickListener {
            remove(violation)
        }
    }

    override fun getItemCount(): Int = violationList.size

    fun addViolation(violationItem: ViolationItem) {
        violationList.add(violationItem)
        notifyItemInserted(violationList.size - 1)
    }
    fun remove(violationItem: ViolationItem) {
        val position = violationList.indexOf(violationItem)
        if (position != -1) {
            // Remove commas from the string and then convert it to a float
            val removedAmount = violationList[position].amount.replace(",", "").toFloat()
            violationList.removeAt(position)
            notifyItemRemoved(position)
            onViolationRemoved(removedAmount)
        }
    }


}
