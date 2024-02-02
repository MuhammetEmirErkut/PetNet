package com.muham.petv01.Adapters

import android.content.ContentValues.TAG
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.*
import com.google.firebase.ktx.Firebase
import com.muham.petv01.BottomSheets.CommentBottomSheetFragment
import com.muham.petv01.BottomSheets.PostMoreBottomSheetFragment
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
        val savePostImageView: ImageView = itemView.findViewById(R.id.savePostImageView)
        val commentPostImageView: ImageView = itemView.findViewById(R.id.commentPostImageView)
        val commentNumberTextView: TextView = itemView.findViewById(R.id.commentNumberTextView)
        val postMoreImageView: ImageView = itemView.findViewById(R.id.postMoreImageView)
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

        println(currentItem.userPhoto)
        val imageSource = when {
            currentItem.userPhoto.contains("B1") -> R.drawable.b1char
            currentItem.userPhoto.contains("B2") -> R.drawable.b2char
            currentItem.userPhoto.contains("G1") -> R.drawable.g1char
            currentItem.userPhoto.contains("G2") -> R.drawable.g2char
            else -> R.drawable.logo
        }

        holder.userPhotoImageView.setImageResource(imageSource)




        // Set the initial state of the like button based on likedByCurrentUser
        holder.likePostButton.setImageResource(
            if (currentItem.likedByCurrentUser) R.drawable.likeapost else R.drawable.likepost
        )

        holder.savePostImageView.setImageResource(
            if (currentItem.savedByCurrentUser) R.drawable.saveonn else R.drawable.postsave
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

        //Save Post Button

        holder.savePostImageView.setOnClickListener {
            val postId = currentItem.documentId

            if (!currentItem.savedByCurrentUser){

                holder.savePostImageView.setImageResource(R.drawable.saveonn)
                currentItem.savedByCurrentUser = true

                savePost(postId, holder.auth)
            } else{

                holder.savePostImageView.setImageResource(R.drawable.postsave)
                currentItem.savedByCurrentUser = false

                unSavePost(postId, holder.auth)
            }
        }

        //Comment Bottom Sheet Fragment

        holder.commentPostImageView.setOnClickListener {

            val commentBottomSheetFragment = CommentBottomSheetFragment()

            // Alttaki kod, tıklanan öğenin belge ID'sini alacaktır
            val postId = itemList[position].documentId
            val bundle = Bundle()
            bundle.putString("postId", postId)
            commentBottomSheetFragment.arguments = bundle

            commentBottomSheetFragment.show((holder.itemView.context as FragmentActivity).supportFragmentManager, commentBottomSheetFragment.tag)

        }
        // Post More Bottom Sheet

        holder.postMoreImageView.setOnClickListener {

            val postMoreBottomSheetFragment = PostMoreBottomSheetFragment()

            // Alttaki kod, tıklanan öğenin belge ID'sini alacaktır
            val postId = itemList[position].documentId
            val bundle = Bundle()
            bundle.putString("postId", postId)
            postMoreBottomSheetFragment.arguments = bundle

            postMoreBottomSheetFragment.show((holder.itemView.context as FragmentActivity).supportFragmentManager, postMoreBottomSheetFragment.tag)

        }

        val postId = itemList[position].documentId
        updateCommentCount(postId, holder.commentNumberTextView)

    }
    private fun updateCommentCount(postId: String, commentNumberTextView: TextView) {
        val postReference = Firebase.firestore.collection("forum").document(postId)

        postReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val post = documentSnapshot.toObject(ItemForPost::class.java)

                    // Comment sayısını kontrol et ve commentNumberTextView'a yerleştir
                    val commentCount = post?.comments?.size ?: 0
                    commentNumberTextView.text = commentCount.toString()

                    // Firebase Firestore'da comment sayısını güncelle
                    postReference.update("commentCount", commentCount)
                        .addOnSuccessListener {
                            Log.d(TAG, "Comment Count Successfully Updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating comment count", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting document", e)
            }
    }

    private fun savePost(postId: String, auth: FirebaseAuth) {
        val postReference = Firebase.firestore.collection("forum").document(postId)

        // Update the "likes" field by adding the current user's ID
        postReference.update("saves", FieldValue.arrayUnion(auth.currentUser?.uid))
            .addOnSuccessListener {
                Log.d(TAG, "Post saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving post", e)
            }
    }

    private fun unSavePost(postId: String, auth: FirebaseAuth) {
        val postReference = Firebase.firestore.collection("forum").document(postId)

        // Update the "likes" field by removing the current user's ID
        postReference.update("saves", FieldValue.arrayRemove(auth.currentUser?.uid))
            .addOnSuccessListener {
                Log.d(TAG, "Post unsaved successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error unsaving post", e)
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
