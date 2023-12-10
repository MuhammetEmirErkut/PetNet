package com.muham.petv01.BottomSheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.Adapters.CommentRecyclerViewAdapter
import com.muham.petv01.Adapters.ForumPostRecyclerViewAdapter
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R
import java.security.Timestamp
import java.util.HashMap

class PostMoreBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var reportReasonLinearLayout: LinearLayout
    private lateinit var reportReasonEditText: EditText
    private lateinit var sendReportTextView: TextView
    private lateinit var deletePostLayout: LinearLayout

    private val firestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var forumPostRecyclerViewAdapter: ForumPostRecyclerViewAdapter

    companion object {
        const val TAG = "PostMoreBottomSheetFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forumpostmore_bottomsheet_fragment, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        reportReasonLinearLayout = view.findViewById(R.id.reportReasonLinearLayout)
        reportReasonEditText = view.findViewById(R.id.reportReasonEditText)
        sendReportTextView = view.findViewById(R.id.sendReportTextView)
        //Delete Post
        deletePostLayout = view.findViewById(R.id.deleteThePost)

        // Set the initial visibility to GONE
        reportReasonLinearLayout.visibility = View.GONE

        // Find the view that triggers the visibility change (assuming it's a TextView with ID reportThePost)
        val reportThePost = view.findViewById<View>(R.id.reportThePost)

        // Set a click listener to toggle the visibility with animation
        reportThePost.setOnClickListener {
            if (reportReasonLinearLayout.visibility == View.VISIBLE) {
                // If currently visible, animate the collapse
                reportReasonLinearLayout.animate().alpha(0.0f).withEndAction {
                    reportReasonLinearLayout.visibility = View.GONE
                }
            } else {
                // If currently hidden, animate the expand
                reportReasonLinearLayout.visibility = View.VISIBLE
                reportReasonLinearLayout.alpha = 0.0f
                reportReasonLinearLayout.animate().alpha(1.0f)
            }
        }

        val postId = arguments?.getString("postId")
        val userId = auth.currentUser?.uid

        if (postId != null) {
            sendTheReport(postId)
        } else {
            Log.e(TAG, "postId is null")
        }
        //Delete Layout Visibility
        isPostBelongsToCurrentUser(postId, userId) { isBelongsToCurrentUser ->
            // Burada isBelongsToCurrentUser değerini kullanabilirsiniz
            if (isBelongsToCurrentUser) {
                deletePostLayout.visibility = View.VISIBLE

                deletePostLayout.setOnClickListener{
                    if (postId != null) {
                        deletePost(postId)
                        dismiss()
                    }
                    else
                        Log.d(TAG, "Post not Found")
                }
            } else {
                deletePostLayout.visibility = View.GONE
            }
        }



    }
    private fun deletePost(postId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Silme işlemini gerçekleştir
        firestore.collection("forum").document(postId)
            .delete()
            .addOnSuccessListener {
                // Silme başarılı
                Log.d(TAG, "Post deleted successfully")

            }
            .addOnFailureListener { e ->
                // Silme başarısız
                Log.e(TAG, "Error deleting post", e)
            }
    }


    private fun isPostBelongsToCurrentUser(postId: String?, userId: String?, onComplete: (Boolean) -> Unit) {
        postId?.let { postReference ->
            FirebaseFirestore.getInstance().collection("forum").document(postReference).get()
                .addOnSuccessListener { documentSnapshot ->
                    val postAuthorUid = documentSnapshot.getString("author")
                    val isBelongsToCurrentUser = postAuthorUid == userId
                    onComplete(isBelongsToCurrentUser)
                }
                .addOnFailureListener { e ->
                    // Hata durumunda buraya düşer
                    onComplete(false)
                }
        }
    }

    private fun sendTheReport(postId: String) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val postReference = firestore.collection("forum").document(postId)

            postReference.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        sendReportTextView.setOnClickListener {
                            // Get report reason from EditText
                            val reportReason = reportReasonEditText.text.toString()

                            if (reportReason.isBlank()) {
                                // Rapor nedeni boşsa uyarı göster
                                reportReasonEditText.error = "Please provide a reason for the report."
                                return@setOnClickListener
                            }

                            // Get post data
                            val postAuthor = documentSnapshot.getString("author") ?: ""
                            val postCommentCount = documentSnapshot.getLong("commentCount") ?: 0
                            val postContent = documentSnapshot.getString("content") ?: ""
                            val postTitle = documentSnapshot.getString("title") ?: ""
                            val postUsername = documentSnapshot.getString("username") ?: ""

                            // Create a map with post data
                            val postData = HashMap<String, Any>()
                            postData["author"] = postAuthor
                            postData["commentCount"] = postCommentCount
                            postData["content"] = postContent
                            postData["title"] = postTitle
                            postData["username"] = postUsername

                            // Create a map with report data
                            val reportData = HashMap<String, Any>()
                            reportData["postId"] = postId
                            reportData["post"] = postData
                            reportData["reportReason"] = reportReason

                            // Add the report to the 'ReportBox' collection
                            firestore.collection("ReportBox").add(reportData)
                                .addOnSuccessListener {
                                    // Report added successfully
                                    // You can add any further logic here

                                    // Animate the SendReportTextView to green
                                    sendReportTextView.animate().alpha(0.0f).withEndAction {
                                        sendReportTextView.setTextColor(resources.getColor(android.R.color.holo_green_light))
                                        sendReportTextView.text = "Thanks for your attention"

                                        // Animate the SendReportTextView back to its original state
                                        sendReportTextView.animate().alpha(1.0f).withEndAction {
                                            // Close the BottomSheetDialogFragment after a delay
                                            sendReportTextView.postDelayed({
                                                dismiss()
                                            }, 1000)
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Report's not sent", e)
                                }
                        }
                    } else {
                        Log.e(TAG, "Post does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting post data", e)
                }
        } else {
            Log.e(TAG, "UserId is null")
        }
    }

}
