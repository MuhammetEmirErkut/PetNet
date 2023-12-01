package com.muham.petv01.Adapters

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.*
import com.google.firebase.ktx.Firebase
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R

class ForumPostRecyclerViewAdapter(private val itemList: List<ItemForPost>) :
    RecyclerView.Adapter<ForumPostRecyclerViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userPhotoImageView: ImageView = itemView.findViewById(R.id.userPhotoImageView)
        val postUserName: TextView = itemView.findViewById(R.id.postUsernameTextView)
        val postTimeTextView: TextView = itemView.findViewById(R.id.postTimeTextView)
        val postTitleTextView: TextView = itemView.findViewById(R.id.postTitleTextView)
        val postContentTextView: TextView = itemView.findViewById(R.id.postContentTextView)
        val likeNumberTextView: TextView = itemView.findViewById(R.id.likeNumberTextView)
        val likePostButton: ImageView = itemView.findViewById(R.id.likePostButton)
        var auth: FirebaseAuth = FirebaseAuth.getInstance() // FirebaseAuth örneğini burada doğru bir şekilde tanımlayın
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
        holder.likeNumberTextView.text = currentItem.like.toString()

        // Set the initial state of the like button based on likedByCurrentUser
        holder.likePostButton.setImageResource(
            if (currentItem.likedByCurrentUser) R.drawable.likeapost else R.drawable.likepost
        )

        holder.likePostButton.setOnClickListener {
            // Get the ID of the current post
            val postId = currentItem.documentId

            // Check if the current user has already liked the post
            if (!currentItem.likedByCurrentUser) {
                // If not liked, update the UI and add the like to the post in Firestore
                holder.likePostButton.setImageResource(R.drawable.likeapost) // Change to filled like icon
                currentItem.likedByCurrentUser = true

                // Add the current user's ID to the likes list in Firestore
                likePost(postId, holder.auth, holder.likeNumberTextView)
            } else {
                // If already liked, update the UI and remove the like from the post in Firestore
                holder.likePostButton.setImageResource(R.drawable.likepost) // Change to outline like icon
                currentItem.likedByCurrentUser = false

                // Remove the current user's ID from the likes list in Firestore
                unlikePost(postId, holder.auth, holder.likeNumberTextView)
            }
        }
    }



    private fun likePost(postId: String, auth: FirebaseAuth, likeNumberTextView: TextView) {
        val postReference = Firebase.firestore.collection("forum").document(postId)

        // Update the "likes" field by adding the current user's ID
        postReference.update("likes", FieldValue.arrayUnion(auth.currentUser?.uid))
            .addOnSuccessListener {
                Log.d(TAG, "Post liked successfully")

                // Beğeni sayısını arttır ve TextView'i güncelle
                val currentLikes = likeNumberTextView.text.toString().toInt()
                val newLikes = currentLikes + 1
                likeNumberTextView.text = newLikes.toString()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error liking post", e)
            }
    }


    // Function to handle unliking a post
    private fun unlikePost(postId: String, auth: FirebaseAuth, likeNumberTextView: TextView) {
        val postReference = Firebase.firestore.collection("forum").document(postId)

        // Update the "likes" field by removing the current user's ID
        postReference.update("likes", FieldValue.arrayRemove(auth.currentUser?.uid))
            .addOnSuccessListener {
                Log.d(TAG, "Post unliked successfully")

                // Beğeni sayısını azalt ve TextView'i güncelle
                val currentLikes = likeNumberTextView.text.toString().toInt()
                val newLikes = if (currentLikes > 0) currentLikes - 1 else 0
                likeNumberTextView.text = newLikes.toString()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error unliking post", e)
            }
    }


    override fun getItemCount(): Int {
        return itemList.size
    }
}
