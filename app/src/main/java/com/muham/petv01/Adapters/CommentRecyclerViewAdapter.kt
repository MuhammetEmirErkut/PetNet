package com.muham.petv01.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muham.petv01.Inheritance.Comment
import com.muham.petv01.R

class CommentRecyclerViewAdapter(private val commentList: List<com.muham.petv01.Inheritance.Comment>) :
    RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentUserName: TextView = itemView.findViewById(R.id.commentUserName)
        val commentContent: TextView = itemView.findViewById(R.id.commentContent)
        // Diğer gerekli görüntüleme öğeleri buraya ekleyebilirsiniz
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)

        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentComment = commentList[position]
        if (currentComment != null) {
            // Yorum verilerini UI elemanlarına yerleştir
            holder.commentUserName.text = currentComment.userName
            holder.commentContent.text = currentComment.content
        }

        // Diğer gerekli görüntüleme öğelerini güncelleyin

        // İhtiyaca göre event dinleyicilerini ekleyebilirsiniz
    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}
