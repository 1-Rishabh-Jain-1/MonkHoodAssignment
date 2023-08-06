package com.rishabh.monkhoodassignment.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rishabh.monkhoodassignment.databinding.ItemUserInfoBinding
import com.rishabh.monkhoodassignment.models.Users
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.rishabh.monkhoodassignment.UpdateUser
import com.rishabh.monkhoodassignment.mvvm.ViewModel


class UserInfoAdapter: RecyclerView.Adapter<ViewHolder>() {
    var userList = listOf<Users>()
    lateinit var binding : ItemUserInfoBinding
    val viewModel = ViewModel()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemUserInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = userList[position]
        Glide.with(holder.itemView.context).load(currentItem.imgProfile).signature(ObjectKey
            (System.currentTimeMillis())).into(holder.imgProfile)

        holder.textViewName.text = currentItem.name
        holder.textViewEmail.text = currentItem.email
        holder.textViewPhone.text = currentItem.phone.toString()
        holder.textViewDob.text = currentItem.dob

        holder.imgEdit.setOnClickListener {
            val updateIntent = Intent(holder.itemView.context, UpdateUser::class.java)
            updateIntent.putExtra("userId", currentItem.userId)
            holder.itemView.context.startActivity(updateIntent)
        }

        holder.imgDelete.setOnClickListener {
            viewModel.deleteUserFromFirebase(currentItem.userId)
            viewModel.deleteUserFromSharedPreferences(currentItem.userId)
            userList = userList.filterNot {
                it == userList[position]
            }
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, userList.size)
        }
    }

    fun setList(list: List<Users>) {
        val result = DiffUtil.calculateDiff(DiffCallBack(userList, list))
        userList = list
        result.dispatchUpdatesTo(this)
    }
}

class ViewHolder(val binding: ItemUserInfoBinding): RecyclerView.ViewHolder(binding.root) {
    val imgProfile = binding.imgProfile
    val imgEdit = binding.imgEdit
    val imgDelete = binding.imgDelete
    val textViewName = binding.textViewName
    val textViewEmail = binding.textViewEmail
    val textViewPhone = binding.textViewPhone
    val textViewDob = binding.textViewDOB
}

class DiffCallBack(
    private val listOld : List<Users>,
    private val listNew : List<Users>
): DiffUtil.Callback() {

    override fun getOldListSize(): Int = listOld.size

    override fun getNewListSize(): Int = listNew.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listOld[oldItemPosition] == listNew[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listOld[oldItemPosition] == listNew[newItemPosition]
    }

}