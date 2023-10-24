package com.muham.petv01.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R

class ForumPostRecyclerViewAdapter(private val itemList: List<ItemForPost>) :
    RecyclerView.Adapter<ForumPostRecyclerViewAdapter.MyViewHolder>(){
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userPhotoImageView: ImageView = itemView.findViewById(R.id.userPhotoImageView)
        val postUserName: TextView = itemView.findViewById(R.id.postUsernameTextView)
        val postTimeTextView: TextView = itemView.findViewById(R.id.postTimeTextView)
        val postTitleTextView: TextView = itemView.findViewById(R.id.postTitleTextView)
        val postContentTextView: TextView = itemView.findViewById(R.id.postContentTextView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ForumPostRecyclerViewAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.forumpost_cell, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ForumPostRecyclerViewAdapter.MyViewHolder,
        position: Int
    ) {
        val currentItem = itemList[position]
        holder.postUserName.text = currentItem.userName
        holder.postTimeTextView.text = currentItem.time
        holder.postTitleTextView.text = currentItem.title
        holder.postContentTextView.text = currentItem.content

    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}