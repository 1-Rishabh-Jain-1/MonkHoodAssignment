package com.rishabh.monkhoodassignment.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.rishabh.monkhoodassignment.UpdateUser
import com.rishabh.monkhoodassignment.databinding.ItemUserInfoBinding
import com.rishabh.monkhoodassignment.models.Users
import com.rishabh.monkhoodassignment.mvvm.ViewModel

class UserInfoAdapter: RecyclerView.Adapter<UserViewHolder>() {

    val vm = ViewModel()
    var alluserslist = listOf<Users>()
    lateinit var binding : ItemUserInfoBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        binding = ItemUserInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return alluserslist.size
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentuser = alluserslist[position]
        Glide.with(holder.itemView.context).load(currentuser.imgProfile).signature(ObjectKey(System.currentTimeMillis())).into(holder.imgProfile)

        holder.itemName.text = currentuser.name
        holder.itemEmail.text = currentuser.email
        holder.itemPhone.text = currentuser.phone.toString()
        holder.itemDob.text = currentuser.DOB
        //holder.imgEdit.listener : intent launch, only pass user id
        holder.imgEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context,UpdateUser::class.java)
            intent.putExtra("UUID",currentuser.userId)
            holder.itemView.context.startActivity(intent)
        }

        holder.imgDelete.setOnClickListener {

            vm.deleteUserFromFirebase(currentuser.userId!!)
            vm.deleteUserFromSharedPreferences(currentuser.userId!!)
            alluserslist = alluserslist.filterNot { it == alluserslist[position] }
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, alluserslist.size)

        }

    }

    fun setList(list: List<Users>) {
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(alluserslist, list))
        alluserslist = list
        diffResult.dispatchUpdatesTo(this)
    }

}

class UserViewHolder(val binding: ItemUserInfoBinding) : RecyclerView.ViewHolder(binding.root) {
    val imgProfile: ImageView = binding.imgProfile
    val imgEdit : ImageView = binding.imgEdit
    val imgDelete : ImageView = binding.imgDelete
    val itemName: TextView = binding.textViewName
    val itemEmail: TextView = binding.textViewEmail
    val itemPhone: TextView = binding.textViewPhone
    val itemDob: TextView = binding.textViewDOB
}

class MyDiffCallback(
    private val oldList : List<Users>,
    private val newList : List<Users>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}