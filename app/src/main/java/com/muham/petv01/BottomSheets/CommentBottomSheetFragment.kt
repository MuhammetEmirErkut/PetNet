package com.muham.petv01.BottomSheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muham.petv01.Adapters.CommentRecyclerViewAdapter
import com.muham.petv01.Inheritance.Comment
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R


class CommentBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var commentEditText: EditText
    private lateinit var commentSendImageView: ImageView
    companion object {
        const val TAG = "CommentBottomSheetFragment"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forumcomment_bottomsheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        commentEditText = view.findViewById(R.id.commentEditText)
        commentSendImageView = view.findViewById(R.id.commentSendImageView)


        setupSendButton()
        val postId = arguments?.getString("postId")
        val postReference = postId?.let { Firebase.firestore.collection("forum").document(it) }

        if (postReference != null) {
            loadComments(postReference)
        }
    }

    private fun addComment(postId: String, commentContent: String) {
        val userId = auth.currentUser?.uid // Firebase Authentication ile userId alınır

        if (userId != null) {
            val postReference = Firebase.firestore.collection("forum").document(postId)

            // Kullanıcı adını "Persons" belgesinden al
            val userReference = Firebase.firestore.collection("Persons").document(userId)
            userReference.get()
                .addOnSuccessListener { userDocumentSnapshot ->
                    if (userDocumentSnapshot.exists()) {
                        val userName = "${userDocumentSnapshot.getString("firstName")} ${userDocumentSnapshot.getString("lastName")}"
                        val comment = Comment(
                            userId = userId,
                            userName = userName,
                            content = commentContent
                        )

                        postReference.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    // Belge var, yorum ekleyebilirsiniz
                                    postReference.update("comments", FieldValue.arrayUnion(comment))
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Comment added successfully")

                                            // Yorum eklendikten sonra isteğe bağlı olarak UI'yi güncelleyebilirsiniz
                                            // Örneğin, yorum sayısını artırabilir veya RecyclerView'i güncelleyebilirsiniz
                                            loadComments(postReference)

                                            commentEditText.text.clear()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error adding comment", e)
                                        }
                                } else {
                                    Log.e(TAG, "Belge bulunamadı: $postId")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Belge okuma hatası", e)
                            }
                    } else {
                        Log.e(TAG, "Kullanıcı bulunamadı: $userId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Kullanıcı belgesi okuma hatası", e)
                }
        } else {
            Log.w(TAG, "User not authenticated")
            // Kullanıcı oturum açmamışsa gerekli işlemleri yapabilirsiniz
        }
    }

    private fun loadComments(postReference: DocumentReference) {
        postReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val post = documentSnapshot.toObject(ItemForPost::class.java)

                    // Commentleri al
                    val comments = post?.comments ?: emptyList()

                    // RecyclerView için adapter ve layoutManager'ı ayarla
                    val commentRecyclerView = view?.findViewById<RecyclerView>(R.id.commentRecyclerView)
                    val commentAdapter = CommentRecyclerViewAdapter(comments)
                    val commentLayoutManager = LinearLayoutManager(context)

                    // Adapter ve LayoutManager'ı iç içe RecyclerView'e ata
                    commentRecyclerView?.adapter = commentAdapter
                    commentRecyclerView?.layoutManager = commentLayoutManager
                }
            }
    }


    private fun setupSendButton() {
        commentSendImageView.setOnClickListener {
            val commentContent = commentEditText.text.toString().trim()

            if (commentContent.isNotEmpty()) {
                val postId = arguments?.getString("postId") // İlgili postun ID'sini almalısınız

                // Yorumu Firebase'e ekle
                if (postId != null) {
                    addComment(postId, commentContent)
                }
                // Yorum eklendikten sonra, isteğe bağlı olarak UI veya diğer işlemleri gerçekleştirebilirsiniz
            }
        }
    }
}
