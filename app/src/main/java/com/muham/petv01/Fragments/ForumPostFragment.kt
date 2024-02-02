package com.muham.petv01.Fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muham.petv01.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ForumPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ForumPostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var postTitleEditText: EditText
    private lateinit var postContentEditText: EditText
    private lateinit var postSendButton: Button
    private lateinit var forumPostBackButton: ImageView


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.forum_postadd_layout, container, false)

        postTitleEditText = view.findViewById(R.id.post_title_edittext)
        postContentEditText = view.findViewById(R.id.post_content_edittext)
        postSendButton = view.findViewById(R.id.postSendButton)
        forumPostBackButton = view.findViewById(R.id.forumpost_backButton)



        auth = FirebaseAuth.getInstance() // auth nesnesini burada başlatın

        // Inside the postSendButton.setOnClickListener block
        postSendButton.setOnClickListener {
            val title = postTitleEditText.text.toString()
            val content = postContentEditText.text.toString()

            val db = Firebase.firestore
            val forumCollection = db.collection("forum")

            // Retrieve user information from the Persons collection
            val userDocRef = db.collection("Persons").document(auth.currentUser?.uid ?: "")
            userDocRef.get()
                .addOnSuccessListener { userSnapshot ->
                    if (userSnapshot.exists()) {
                        val firstName = userSnapshot.getString("firstName") ?: ""
                        val lastName = userSnapshot.getString("lastName") ?: ""

                        // Combine first name and last name to create a username
                        val username = "$firstName $lastName"
                        val userPhoto = userSnapshot.getString("userPhoto") ?: ""
                        // Now, you can use the 'username' in your post
                        val newForumPost = hashMapOf(
                            "title" to title,
                            "content" to content,
                            "author" to auth.currentUser?.uid,
                            "username" to username, // Include the username in the post
                            "timestamp" to FieldValue.serverTimestamp(),
                            "likes" to arrayListOf<String>(),
                            "saves" to arrayListOf<String>(),
                            "userPhoto" to userPhoto
                        )

                        // Add the new post to the 'forum' collection
                        forumCollection.add(newForumPost)
                            .addOnSuccessListener { documentReference ->
                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                                parentFragmentManager.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting user document", e)
                }



        }

        forumPostBackButton.setOnClickListener {
            Log.d(TAG, "forumPostBackButton clicked")
            parentFragmentManager.popBackStack()
        }
        return view
    }



    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top)
        } else {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val slideOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top)
        view?.startAnimation(slideOutAnimation)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ForumPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ForumPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}